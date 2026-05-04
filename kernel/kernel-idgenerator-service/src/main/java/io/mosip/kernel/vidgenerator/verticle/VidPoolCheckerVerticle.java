package io.mosip.kernel.vidgenerator.verticle;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import io.mosip.kernel.idgenerator.verticle.HttpServerVerticle;
import io.mosip.kernel.vidgenerator.constant.EventType;
import io.mosip.kernel.vidgenerator.constant.VidLifecycleStatus;
import io.mosip.kernel.vidgenerator.service.VidService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class VidPoolCheckerVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(VidPoolCheckerVerticle.class);

	private static final long DEFAULT_POOL_CHECK_INTERVAL_MS = 180_000L;

	private VidService vidService;

	private Environment environment;

	private long threshold;

	private ApplicationContext context;

	public VidPoolCheckerVerticle(final ApplicationContext context) {
		this.context = context;
		this.vidService = this.context.getBean(VidService.class);
		this.environment = this.context.getBean(Environment.class);
		this.threshold = Objects.requireNonNullElse(environment.getProperty("mosip.kernel.vid.min-unused-threshold", Long.class), 0L);
	}

	private AtomicBoolean locked = new AtomicBoolean(false);

	@Override
	public void start(Future<Void> startFuture) {
		EventBus eventBus = vertx.eventBus();
		DeliveryOptions deliveryOptions = createPoolDeliveryOptions();

		Long intervalProperty = environment.getProperty("mosip.kernel.vid.pool-check-interval-ms", Long.class);
		long periodMs = intervalProperty != null ? intervalProperty : DEFAULT_POOL_CHECK_INTERVAL_MS;
		if (periodMs <= 0) {
			LOGGER.warn(
					"mosip.kernel.vid.pool-check-interval-ms is {}; scheduled VID pool checks are disabled",
					periodMs);
		} else {
			vertx.setPeriodic(periodMs, timerId -> runScheduledPoolCheck(eventBus, deliveryOptions));
			LOGGER.info("VID pool checker runs every {} ms", periodMs);
		}

		MessageConsumer<String> initPoolConsumer = eventBus.consumer(EventType.INITPOOL);
		initPoolConsumer.handler(initPoolHandler -> {
			long start = System.currentTimeMillis();
			runVidCountCheck(noOfFreeVids -> {
				LOGGER.info("no of vid free present are {}", noOfFreeVids);
				LOGGER.info("value of threshold is {} and lock is {}", threshold, locked.get());
				boolean isEligibleForPool = noOfFreeVids < threshold && !locked.get();
				LOGGER.info("is eligible for pool {}", isEligibleForPool);
				if (isEligibleForPool) {
					locked.set(true);
					eventBus.send(EventType.GENERATEPOOL, noOfFreeVids, deliveryOptions, replyHandler -> {
						if (replyHandler.succeeded()) {
							locked.set(false);
							deployHttpVerticle(start);
							LOGGER.info("population of init pool done");
						} else if (replyHandler.failed()) {
							locked.set(false);
							LOGGER.error("population failed with cause ", replyHandler.cause());
							initPoolHandler.fail(100, replyHandler.cause().getMessage());
						}
					});
				} else {
					deployHttpVerticle(start);
				}
			});
		});
		startFuture.complete();
	}

	private DeliveryOptions createPoolDeliveryOptions() {
		DeliveryOptions deliveryOptions = new DeliveryOptions();
		deliveryOptions.setSendTimeout(environment.getProperty("mosip.kernel.vid.pool-population-timeout", Long.class));
		return deliveryOptions;
	}

	private void runVidCountCheck(Consumer<Long> onCount) {
		vertx.executeBlocking(future -> {
			future.complete(vidService.fetchVidCount(VidLifecycleStatus.AVAILABLE));
		}, false, result -> {
			if (result.failed()) {
				LOGGER.error("failed to fetch vid count ", result.cause());
				return;
			}
			onCount.accept((Long) result.result());
		});
	}

	private void runScheduledPoolCheck(EventBus eventBus, DeliveryOptions deliveryOptions) {
		runVidCountCheck(noOfFreeVids -> {
			LOGGER.info("scheduled VID pool check: free vids {}", noOfFreeVids);
			if (noOfFreeVids < threshold && !locked.get()) {
				locked.set(true);
				eventBus.send(EventType.GENERATEPOOL, noOfFreeVids, deliveryOptions, replyHandler -> {
					if (replyHandler.succeeded()) {
						locked.set(false);
						LOGGER.info("population of pool done");
					} else if (replyHandler.failed()) {
						locked.set(false);
						LOGGER.error("population failed with cause ", replyHandler.cause());
					}
				});
			} else {
				LOGGER.debug("scheduled VID pool check skipped: threshold satisfied or generation locked");
			}
		});
	}

	private void deployHttpVerticle(long start) {
		Verticle httpVerticle = new HttpServerVerticle(context);
		DeploymentOptions opts = new DeploymentOptions();
		vertx.deployVerticle(httpVerticle, opts, res -> {
			if (res.failed()) {
				LOGGER.info("Failed to deploy verticle " + httpVerticle.getClass().getSimpleName() + " " + res.cause());
			} else if (res.succeeded()) {
				LOGGER.info("population of pool is done starting fetcher verticle");
				LOGGER.info("Starting vidgenerator service... ");
				LOGGER.info("service took {} ms to pool and start", (System.currentTimeMillis() - start));
				LOGGER.info("Deployed verticle " + httpVerticle.getClass().getSimpleName());
			}
		});

	}
}
