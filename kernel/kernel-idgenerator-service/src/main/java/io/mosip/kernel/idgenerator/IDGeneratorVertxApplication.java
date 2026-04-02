package io.mosip.kernel.idgenerator;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.util.FileUtils;
import io.mosip.kernel.idgenerator.config.ConfigUrlsBuilder;
import io.mosip.kernel.idgenerator.config.HibernateDaoConfig;
import io.mosip.kernel.idgenerator.verticle.HttpServerVerticle;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;
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
import jakarta.annotation.PostConstruct;

/**
 * ID Generator Vertx Application
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
@SpringBootApplication
public class IDGeneratorVertxApplication {

	private static Vertx vertx;

	/**
	 * The field for Logger
	 * 
	 */
	private static Logger LOGGER;

	/**
	 * Server context path.
	 */
	@Value("${server.servlet.path}")
	private String contextPath;
	
	private static final long DEFAULT_VID_JOB_FREQUENCY = 10000L;

	private static final long DEFAULT_UIN_JOB_FREQUENCY=10000L;

	/**
	 * This method create or update swagger json for swagger ui after service start.
	 */
	@PostConstruct
	private void swaggerJSONFileUpdate() {
		try {
			TemplateManager templateManager;
			File swaggerJsonnFile = new File(VIDGeneratorConstant.SWAGGER_UI_JSON_PATH);
			templateManager = new TemplateManagerBuilderImpl().build();
			Map<String, Object> map = new HashMap<>();
			map.put("servletpath", contextPath);
			InputStream is = this.getClass().getClassLoader()
					.getResourceAsStream(VIDGeneratorConstant.SWAGGER_JSON_TEMPLATE);
			InputStream out = templateManager.merge(is, map);
			String merged = IOUtils.toString(out, StandardCharsets.UTF_8.name());
			FileUtils.writeStringToFile(swaggerJsonnFile, merged, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
	}

	/**
	 * main method for the application
	 * 
	 * @param args the argument
	 */
	public static void main(String[] args) {
		System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
		LOGGER = LoggerFactory.getLogger(IDGeneratorVertxApplication.class);

		CountDownLatch latch = new CountDownLatch(1);
		loadPropertiesFromConfigServer(latch);

		try {
			latch.await(); // Wait for config load + app startup
			LOGGER.info("✅ Configuration loaded and application started.");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error("❌ Main thread interrupted while waiting for app startup", e);
		}

		LOGGER.info("🟢 Main thread ends.");
	}

	/**
	 * This method retrieves and loads the configuration fetched from
	 * spring-config-server. If retrievation succeeds, then the local properties
	 * present are over-ridden. If retrievation fails, the local properties are used
	 * for running the application.
	 */
	private static void loadPropertiesFromConfigServer(CountDownLatch latch) {
		Vertx vertx = Vertx.vertx();
		try {
			List<ConfigStoreOptions> configStores = new ArrayList<>();
			List<String> configUrls = ConfigUrlsBuilder.getURLs();
			configUrls.forEach(url -> configStores.add(new ConfigStoreOptions()
					.setType(VIDGeneratorConstant.CONFIG_STORE_OPTIONS_TYPE)
					.setConfig(new JsonObject()
							.put(VIDGeneratorConstant.URL, url)
							.put(VIDGeneratorConstant.TIME_OUT, Long.parseLong(VIDGeneratorConstant.CONFIG_SERVER_FETCH_TIME_OUT)))));

			ConfigRetrieverOptions options = new ConfigRetrieverOptions();
			configStores.forEach(options::addStore);
			ConfigRetriever retriever = ConfigRetriever.create(vertx, options.setScanPeriod(0));

			LOGGER.info("🔁 Retrieving configuration from Spring Config Server...");

			retriever.getConfig(result -> {
				if (result.succeeded()) {
					JsonObject jsonObject = result.result();
					if (jsonObject != null) {
						jsonObject.forEach(entry ->
								System.setProperty(entry.getKey(), entry.getValue().toString()));
					}
					LOGGER.info("✅ Configuration loaded from Config Server.");
				} else {
					LOGGER.warn("⚠️ Failed to fetch config from server: " + result.cause().getMessage());
				}
				retriever.close();
				vertx.close();
				startApplication();
				latch.countDown();  // ✅ Allow main to continue
			});
		} catch (Exception e) {
			LOGGER.warn("❌ Error during config loading: " + e.getMessage());
			vertx.close();
			startApplication();
			latch.countDown();  // ✅ Allow main to continue
		}
	}

	/**
	 * This method sets the Application Context, deploys the verticles.
	 * 
	 * @throws InterruptedException
	 */
	private static void startApplication() {
		ApplicationContext context = new AnnotationConfigApplicationContext(HibernateDaoConfig.class);
		CompletableFuture.runAsync(() -> startVIDPoolRuntime(context));
		CompletableFuture.runAsync(() -> startUINPoolRuntime(context));

		// No need to block main thread if Vert.x is running with event loops
		LOGGER.info("✅ Application started and background workers running.");

		/*
		 * VertxOptions options = new VertxOptions(); options.setMetricsOptions(new
		 * MicrometerMetricsOptions() .setPrometheusOptions(new
		 * VertxPrometheusOptions().setEnabled(true)) .setEnabled(true));
		 * DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);
		 * vertx = Vertx.vertx(options); Verticle[] workerVerticles = { new
		 * VidPoolCheckerVerticle(context), new VidPopulatorVerticle(context), new
		 * VidExpiryVerticle(context), new VidIsolatorVerticle(context) };
		 * Stream.of(workerVerticles).forEach(verticle -> deploy(verticle,
		 * workerOptions, vertx)); vertx.setTimer(1000, handler -> initVIDPool());
		 * Verticle[] uinVerticles = { new UinGeneratorVerticle(context),new
		 * UinTransferVerticle(context)}; Stream.of(uinVerticles).forEach(verticle ->
		 * vertx.deployVerticle(verticle, stringAsyncResult -> { if
		 * (stringAsyncResult.succeeded()) { LOGGER.info("Successfully deployed: " +
		 * verticle.getClass().getSimpleName()); } else {
		 * LOGGER.info("Failed to deploy:" + verticle.getClass().getSimpleName() +
		 * "\nCause: " + stringAsyncResult.cause()); } })); vertx.setTimer(1000, handler
		 * -> initUINPool());
		 */
	}

	public static void startVIDPoolRuntime(ApplicationContext context) {
		VertxOptions options = new VertxOptions()
				.setMetricsOptions(new MicrometerMetricsOptions()
						.setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
						.setEnabled(true));
		vertx = Vertx.vertx(options);

		DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);
		Verticle[] vidVerticles = {
				new VidPoolCheckerVerticle(context),
				new VidPopulatorVerticle(context),
				new VidExpiryVerticle(context),
				new VidIsolatorVerticle(context)
		};
		Stream.of(vidVerticles).forEach(verticle -> deploy(verticle, workerOptions, vertx));

		// Timer-based initialization
		vertx.setTimer(1000, handler -> initVIDPool());
	}

	public static void startUINPoolRuntime(ApplicationContext context) {
		if (vertx == null) {
			VertxOptions options = new VertxOptions()
					.setMetricsOptions(new MicrometerMetricsOptions()
							.setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
							.setEnabled(true));
			vertx = Vertx.vertx(options);
		}

		Verticle[] uinVerticles = {
				new UinGeneratorVerticle(context),
				new UinTransferVerticle(context)
		};
		Stream.of(uinVerticles).forEach(verticle ->
				vertx.deployVerticle(verticle, result -> {
					if (result.succeeded()) {
						LOGGER.info("Successfully deployed: " + verticle.getClass().getSimpleName());
					} else {
						LOGGER.info("Failed to deploy: " + verticle.getClass().getSimpleName() +
								"\nCause: " + result.cause());
					}
				})
		);

		// Timer-based initialization
		vertx.setTimer(1000, handler -> initUINPool());
	}

	@PostConstruct
	private static void initVIDPool() {
		LOGGER.info("Service will be started after pooling vids..");
		EventBus eventBus = vertx.eventBus();
		LOGGER.info("eventBus deployer {}", eventBus);
		eventBus.publish(EventType.INITPOOL, EventType.INITPOOL);
	}

	@PostConstruct
	private static void initUINPool() {
		LOGGER.info("Service will be started after pooling vids..");
		EventBus eventBus = vertx.eventBus();
		LOGGER.info("eventBus deployer {}", eventBus);
		eventBus.publish(UinGeneratorConstant.UIN_GENERATOR_ADDRESS, UinGeneratorConstant.GENERATE_UIN);
	}


	private static void deploy(Verticle verticle, DeploymentOptions opts, Vertx vertx) {
		vertx.deployVerticle(verticle, opts, res -> {
			if (res.failed()) {
				LOGGER.info("Failed to deploy verticle " + verticle.getClass().getSimpleName() + " " + res.cause());
			} else if (res.succeeded()) {
				LOGGER.info("Deployed verticle " + verticle.getClass().getSimpleName());

			}
		});
	}
}