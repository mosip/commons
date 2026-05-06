package io.mosip.kernel.uingenerator.verticle;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import io.mosip.kernel.uingenerator.constant.UINHealthConstants;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.uingenerator.generator.UinProcesser;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Verticle instance for Uin Generator
 * 
 * @author Dharmesh Khandelwal
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
public class UinGeneratorVerticle extends AbstractVerticle {

	/**
	 * The field for logger
	 */
	 private static final Logger LOGGER = LoggerFactory.getLogger(UinGeneratorVerticle.class);
	 
	 private static final long DEFAULT_UIN_POOL_CHECK_INTERVAL_SECONDS = 900L;

	/**
	 * Field for UinProcesser
	 */
	private UinProcesser uinProcesser;
	
	private Environment environment;

	/**
	 * Initialize beans
	 * 
	 * @param context context
	 */
	public UinGeneratorVerticle(final ApplicationContext context) {
		uinProcesser = (UinProcesser) context.getBean("uinProcesser");
		environment = context.getBean(Environment.class);
	}

	private AtomicBoolean locked = new AtomicBoolean(false);

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.vertx.core.AbstractVerticle#start()
	 */
	@Override
	public void start() {
		scheduleUinPoolCheck();

		vertx.eventBus().consumer(UinGeneratorConstant.UIN_GENERATOR_ADDRESS, receivedMessage -> {
			if (receivedMessage.body().equals(UinGeneratorConstant.GENERATE_UIN) && !locked.get()) {
				vertx.executeBlocking(future -> {
					locked.set(true);
					if (uinProcesser.shouldGenerateUins()) {
						uinProcesser.generateUins();
					}
					future.complete();
				}, result -> {
					locked.set(false);
					if (result.succeeded()) {
						LOGGER.info("Generated and persisted uins lock set to false");
					} else {
						LOGGER.error("Uin Genaration failed", result.cause());
					}
				});
			} else {
				LOGGER.info("Generated and persisted uins lock is true.");
			}
			receivedMessage.reply(UINHealthConstants.ACTIVE);
		});
	}

	/**
	 * Schedules periodic UIN pool checks based on configured interval.
	 */
	private void scheduleUinPoolCheck() {
		Long intervalSeconds = environment.getProperty("kernel.uin.pool-check-interval-seconds", Long.class);
		long periodSeconds = intervalSeconds != null ? intervalSeconds : DEFAULT_UIN_POOL_CHECK_INTERVAL_SECONDS;
		if (periodSeconds > 0) {
			long periodMs = periodSeconds * 1000;
			vertx.setPeriodic(periodMs, timerId -> {
				LOGGER.info("Running scheduled UIN pool check job");
				vertx.eventBus().publish(UinGeneratorConstant.UIN_GENERATOR_ADDRESS, UinGeneratorConstant.GENERATE_UIN);
			});
			LOGGER.info("UIN pool checker runs every {} seconds", periodSeconds);
		} else {
			LOGGER.warn(
					"kernel.uin.pool-check-interval-seconds is {}; scheduled UIN pool checks are disabled",
					periodSeconds);
		}
	}
}