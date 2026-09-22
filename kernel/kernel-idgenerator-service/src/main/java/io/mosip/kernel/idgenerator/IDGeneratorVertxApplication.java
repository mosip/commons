package io.mosip.kernel.idgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import io.mosip.kernel.idgenerator.util.Utility;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.mosip.kernel.idgenerator.config.ConfigUrlsBuilder;
import io.mosip.kernel.idgenerator.config.HibernateDaoConfig;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.uingenerator.verticle.UinGeneratorVerticle;
import io.mosip.kernel.uingenerator.verticle.UinTransferVerticle;
import io.mosip.kernel.vidgenerator.constant.EventType;
import io.mosip.kernel.vidgenerator.constant.VIDGeneratorConstant;
import io.mosip.kernel.vidgenerator.verticle.VidExpiryVerticle;
import io.mosip.kernel.vidgenerator.verticle.VidIsolatorVerticle;
import io.mosip.kernel.vidgenerator.verticle.VidPoolCheckerVerticle;
import io.mosip.kernel.vidgenerator.verticle.VidPopulatorVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;

/**
 * Sole entry point for the ID generator HTTP service (RID + UIN/VID).
 * <p>
 * Loads configuration from Spring Cloud Config, deploys UIN/VID worker
 * verticles, and publishes pool-init events on the Vert.x event bus.
 * {@link HttpServerVerticle} serves {@code /v1/idgenerator} (UIN/VID) and
 * {@code /v1/ridgenerator} (RID).
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
@SpringBootApplication
public class IDGeneratorVertxApplication {

	private static Vertx vertx;

	/**
	 * Vert.x logger bound after the SLF4J delegate factory is installed.
	 * 
	 */
	private static Logger LOGGER;

	private static final long DEFAULT_VID_JOB_FREQUENCY = 10000L;

	private static final long DEFAULT_UIN_JOB_FREQUENCY=10000L;

	/**
	 * Publishes {@link EventType#INITPOOL} so {@code VidPoolCheckerVerticle} fills the VID pool.
	 */
	@PostConstruct
	private static void initVIDPool() {
		LOGGER.info("Service will be started after pooling vids..");
		EventBus eventBus = vertx.eventBus();
		LOGGER.info("eventBus deployer {}", eventBus);
		eventBus.publish(EventType.INITPOOL, EventType.INITPOOL);
	}

	/**
	 * Installs the Vert.x SLF4J log delegate and loads Cloud Config before deploying verticles.
	 *
	 * @param args unused command-line arguments
	 */
	public static void main(String[] args) {
		System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
		LOGGER = LoggerFactory.getLogger(IDGeneratorVertxApplication.class);
		loadPropertiesFromConfigServer();
	}

	/**
	 * Fetches properties from Spring Cloud Config and overlays them onto system properties.
	 * <p>
	 * On success or failure the local JVM properties remain as fallback and
	 * {@link #startApplication()} is invoked.
	 * </p>
	 */
	private static void loadPropertiesFromConfigServer() {
		Vertx vertx = Vertx.vertx();
		try {
			List<ConfigStoreOptions> configStores = new ArrayList<>();
			List<String> configUrls = ConfigUrlsBuilder.getURLs();
			configUrls.forEach(url -> configStores
					.add(new ConfigStoreOptions().setType(VIDGeneratorConstant.CONFIG_STORE_OPTIONS_TYPE)
							.setConfig(new JsonObject().put(VIDGeneratorConstant.URL, url).put(
									VIDGeneratorConstant.TIME_OUT,
									Long.parseLong(VIDGeneratorConstant.CONFIG_SERVER_FETCH_TIME_OUT)))));
			ConfigRetrieverOptions configRetrieverOptions = new ConfigRetrieverOptions();
			configStores.forEach(configRetrieverOptions::addStore);
			ConfigRetriever retriever = ConfigRetriever.create(vertx, configRetrieverOptions.setScanPeriod(0));
			LOGGER.info("Retrieving configuration from Spring-Config-Server");
			retriever.getConfig(json -> {
				if (json.succeeded()) {
					JsonObject jsonObject = json.result();
					if (jsonObject != null) {
						jsonObject.iterator().forEachRemaining(sourceValue -> System.setProperty(sourceValue.getKey(),
								sourceValue.getValue().toString()));
					}
					json.mapEmpty();
					retriever.close();
					vertx.close();
					startApplication();
				} else {
					LOGGER.warn(json.cause().getMessage() + "\n");
					json.otherwiseEmpty();
					retriever.close();
					vertx.close();
					startApplication();
				}
			});
		} catch (Exception exception) {
			LOGGER.warn(exception.getMessage() + "\n");
			vertx.close();
			startApplication();
		}
	}

	/**
	 * Builds the Hibernate Spring context and deploys VID and UIN worker verticles.
	 */
	private static void startApplication() {
		ApplicationContext context = new AnnotationConfigApplicationContext(HibernateDaoConfig.class);
		VertxOptions options = new VertxOptions();
		options.setMetricsOptions(new MicrometerMetricsOptions()
                .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
                .setEnabled(true));
		DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);
		vertx = Vertx.vertx(options);
		Verticle[] workerVerticles = { new VidPoolCheckerVerticle(context), new VidPopulatorVerticle(context),
				new VidExpiryVerticle(context), new VidIsolatorVerticle(context) };
		Stream.of(workerVerticles).forEach(verticle -> deploy(verticle, workerOptions, vertx));
		vertx.setTimer(getVidInitJobFrequency(), handler -> initVIDPool());
		Verticle[] uinVerticles = { new UinGeneratorVerticle(context),new UinTransferVerticle(context)};
		Stream.of(uinVerticles).forEach(verticle -> vertx.deployVerticle(verticle, stringAsyncResult -> {
			if (stringAsyncResult.succeeded()) {
				LOGGER.info("Successfully deployed: " + verticle.getClass().getSimpleName());
			} else {
				LOGGER.info("Failed to deploy:" + verticle.getClass().getSimpleName() + "\nCause: "
						+ stringAsyncResult.cause());
			}
		}));
		vertx.setTimer(getUinInitJobFrequency(), handler -> initUINPool());
	}

	/**
	 * Publishes {@link UinGeneratorConstant#GENERATE_UIN} on {@link UinGeneratorConstant#UIN_GENERATOR_ADDRESS}.
	 */
	@PostConstruct
	private static void initUINPool() {
		LOGGER.info("Service will be started after pooling vids..");
		EventBus eventBus = vertx.eventBus();
		LOGGER.info("eventBus deployer {}", eventBus);
		eventBus.publish(UinGeneratorConstant.UIN_GENERATOR_ADDRESS, UinGeneratorConstant.GENERATE_UIN);
	}


	/**
	 * Deploys {@code verticle} with {@code opts} and logs success or failure.
	 *
	 * @param verticle the verticle to deploy
	 * @param opts     deployment options (worker vs event-loop)
	 * @param vertx    Vert.x instance
	 */
	private static void deploy(Verticle verticle, DeploymentOptions opts, Vertx vertx) {
		vertx.deployVerticle(verticle, opts, res -> {
			if (res.failed()) {
				LOGGER.info("Failed to deploy verticle " + verticle.getClass().getSimpleName() + " " + res.cause());
			} else if (res.succeeded()) {
				LOGGER.info("Deployed verticle " + verticle.getClass().getSimpleName());

			}
		});
	}
	
	/**
	 * Reads {@code mosip.kernel.vid.init-job-frequency} or {@link #DEFAULT_VID_JOB_FREQUENCY}.
	 *
	 * @return delay in milliseconds before the first VID pool-init event
	 */
	private static long getVidInitJobFrequency() {
		return Utility.getLongProperty("mosip.kernel.vid.init-job-frequency", DEFAULT_VID_JOB_FREQUENCY);
	}

	/**
	 * Reads {@code mosip.kernel.uin.init-job-frequency} or {@link #DEFAULT_UIN_JOB_FREQUENCY}.
	 *
	 * @return delay in milliseconds before the first UIN pool-init event
	 */
	private static long getUinInitJobFrequency() {
		return Utility.getLongProperty("mosip.kernel.uin.init-job-frequency", DEFAULT_UIN_JOB_FREQUENCY);
	}
}