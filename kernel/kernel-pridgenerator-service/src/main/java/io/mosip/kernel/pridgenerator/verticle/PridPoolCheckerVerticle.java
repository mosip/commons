package io.mosip.kernel.pridgenerator.verticle;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import io.mosip.kernel.pridgenerator.constant.EventType;
import io.mosip.kernel.pridgenerator.constant.PRIDHealthConstants;
import io.mosip.kernel.pridgenerator.constant.PridLifecycleStatus;
import io.mosip.kernel.pridgenerator.service.PridService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class PridPoolCheckerVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(PridPoolCheckerVerticle.class);

	private PridService pridService;

	private Environment environment;

	private long threshold;

	private ApplicationContext appContext;

	public PridPoolCheckerVerticle(final ApplicationContext context) {
		this.appContext = context;
		this.pridService = this.appContext.getBean(PridService.class);
		this.environment = this.appContext.getBean(Environment.class);
	this.threshold = Optional.ofNullable(environment.getProperty("mosip.kernel.prid.min-unused-threshold", Long.class))
            .orElseThrow(() -> new IllegalStateException("Property 'mosip.kernel.prid.min-unused-threshold' is missing"));

	}

	private AtomicBoolean locked = new AtomicBoolean(false);

	@Override
	public void start(Future<Void> startFuture) {
	    EventBus eventBus = vertx.eventBus();
	    DeliveryOptions deliveryOptions = createDeliveryOptions();

	    setupCheckPoolConsumer(eventBus, deliveryOptions);
	    setupInitPoolConsumer(eventBus, deliveryOptions);
	}

	private DeliveryOptions createDeliveryOptions() {
	    DeliveryOptions deliveryOptions = new DeliveryOptions();
	    deliveryOptions.setSendTimeout(environment.getProperty("mosip.kernel.prid.pool-population-timeout", Long.class));
	    return deliveryOptions;
	}

	private void setupCheckPoolConsumer(EventBus eventBus, DeliveryOptions deliveryOptions) {
	    MessageConsumer<String> checkPoolConsumer = eventBus.consumer(EventType.CHECKPOOL);
	    checkPoolConsumer.handler(handler -> {
	        long noOfFreeprids = pridService.fetchPridCount(PridLifecycleStatus.AVAILABLE);
	        LOGGER.info("no of prid free present are {}", noOfFreeprids);

	        if (noOfFreeprids < threshold && !locked.get()) {
	            handlePoolGeneration(eventBus, deliveryOptions, noOfFreeprids, () -> LOGGER.info("population of pool done"));
	        } else {
	            LOGGER.info("event type is send {} eventBus{}", handler.isSend(), eventBus);
	            LOGGER.info("locked generation");
	        }
	        handler.reply(PRIDHealthConstants.ACTIVE);
	    });
	}

	private void setupInitPoolConsumer(EventBus eventBus, DeliveryOptions deliveryOptions) {
	    MessageConsumer<String> initPoolConsumer = eventBus.consumer(EventType.INITPOOL);
	    initPoolConsumer.handler(initPoolHandler -> {
	        long start = System.currentTimeMillis();
	        long noOfFreeprids = pridService.fetchPridCount(PridLifecycleStatus.AVAILABLE);
	        LOGGER.info("no of prid free present are {}", noOfFreeprids);
	        LOGGER.info("value of threshold is {} and lock is {}", threshold, locked.get());

	        boolean isEligibleForPool = noOfFreeprids < threshold && !locked.get();
	        LOGGER.info("is eligible for pool {}", isEligibleForPool);

	        if (isEligibleForPool) {
	            handlePoolGeneration(eventBus, deliveryOptions, noOfFreeprids, () -> {
	                deployHttpVerticle(start);
	                LOGGER.info("population of init pool done");
	            });
	        } else {
	            deployHttpVerticle(start);
	        }
	    });
	}


	private void handlePoolGeneration(EventBus eventBus, DeliveryOptions deliveryOptions, long noOfFreeprids, Runnable onSuccess) {
	    locked.set(true);
	    eventBus.request(EventType.GENERATEPOOL, noOfFreeprids, deliveryOptions, replyHandler -> {
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
		Verticle httpVerticle = new PridFetcherVerticle(appContext);
		DeploymentOptions opts = new DeploymentOptions();
		vertx.deployVerticle(httpVerticle, opts, res -> {
			if (res.failed()) {
				LOGGER.info("Failed to deploy verticle " + httpVerticle.getClass().getSimpleName() + " " + res.cause());
			} else if (res.succeeded()) {
				LOGGER.info("population of pool is done starting fetcher verticle");
				LOGGER.info("Starting pridgenerator service... ");
				LOGGER.info("service took {} ms to pool and start", (System.currentTimeMillis() - start));
				LOGGER.info("Deployed verticle " + httpVerticle.getClass().getSimpleName());
			}
		});

	}
}
