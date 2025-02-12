package io.mosip.kernel.vidgenerator.verticle;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private VidService vidService;

	private Environment environment;

	private long threshold;

	private ApplicationContext applicationContext;

	public VidPoolCheckerVerticle(final ApplicationContext context) {
		this.applicationContext = context;
		this.vidService = this.applicationContext.getBean(VidService.class);
		this.environment = this.applicationContext.getBean(Environment.class);
		this.threshold = Optional.ofNullable(environment.getProperty("mosip.kernel.prid.min-unused-threshold", Long.class))
	            .orElseThrow(() -> new IllegalStateException("Property 'mosip.kernel.prid.min-unused-threshold' is missing"));
	
	}

	private AtomicBoolean locked = new AtomicBoolean(false);

	@Override
	public void start(Future<Void> startFuture) {
	    EventBus eventBus = vertx.eventBus();
	    DeliveryOptions deliveryOptions = new DeliveryOptions();
	    deliveryOptions.setSendTimeout(environment.getProperty("mosip.kernel.vid.pool-population-timeout", Long.class));

	    setupCheckPoolConsumer(eventBus, deliveryOptions);
	    setupInitPoolConsumer(eventBus, deliveryOptions);
	}

	private void setupCheckPoolConsumer(EventBus eventBus, DeliveryOptions deliveryOptions) {
	    MessageConsumer<String> checkPoolConsumer = eventBus.consumer(EventType.CHECKPOOL);
	    checkPoolConsumer.handler(handler -> {
	        long noOfFreeVids = vidService.fetchVidCount(VidLifecycleStatus.AVAILABLE);
	        LOGGER.info("no of vid free present are {}", noOfFreeVids);
	        if (shouldGeneratePool(noOfFreeVids)) {
	            generatePool(eventBus, deliveryOptions, noOfFreeVids, () -> LOGGER.info("population of pool done"));
	        } else {
	            LOGGER.info("event type is send {} eventBus{}", handler.isSend(), eventBus);
	            LOGGER.info("locked generation");
	        }
	    });
	}

	private void setupInitPoolConsumer(EventBus eventBus, DeliveryOptions deliveryOptions) {
	    MessageConsumer<String> initPoolConsumer = eventBus.consumer(EventType.INITPOOL);
	    initPoolConsumer.handler(initPoolHandler -> {
	        long start = System.currentTimeMillis();
	        long noOfFreeVids = vidService.fetchVidCount(VidLifecycleStatus.AVAILABLE);
	        LOGGER.info("no of vid free present are {}", noOfFreeVids);
	        LOGGER.info("value of threshold is {} and lock is {}", threshold, locked.get());
	        boolean isEligibleForPool = shouldGeneratePool(noOfFreeVids);
	        LOGGER.info("is eligible for pool {}", isEligibleForPool);
	        if (isEligibleForPool) {
	            generatePool(eventBus, deliveryOptions, noOfFreeVids, () -> {
	                deployHttpVerticle(start);
	                LOGGER.info("population of init pool done");
	            });
	        } else {
	            deployHttpVerticle(start);
	        }
	    });
	}

	private boolean shouldGeneratePool(long noOfFreeVids) {
	    return noOfFreeVids < threshold && !locked.get();
	}

	private void generatePool(EventBus eventBus, DeliveryOptions deliveryOptions, long noOfFreeVids, Runnable onSuccess) {
	    locked.set(true);
	    eventBus.request(EventType.GENERATEPOOL, noOfFreeVids, deliveryOptions, replyHandler -> {
	        if (replyHandler.succeeded()) {
	            locked.set(false);
	            onSuccess.run();
	        } else if (replyHandler.failed()) {
	            locked.set(false);
	            LOGGER.error("population failed with cause ", replyHandler.cause());
	        }
	    });
	}

	private void deployHttpVerticle(long start) {
		Verticle httpVerticle = new HttpServerVerticle(applicationContext);
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
