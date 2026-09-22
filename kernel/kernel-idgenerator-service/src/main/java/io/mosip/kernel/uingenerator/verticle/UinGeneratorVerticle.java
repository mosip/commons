package io.mosip.kernel.uingenerator.verticle;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;

import io.mosip.kernel.uingenerator.constant.UINHealthConstants;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.uingenerator.generator.UinProcesser;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Worker verticle that generates unused UINs when the pool is below threshold.
 * <p>
 * Consumes {@link UinGeneratorConstant#UIN_GENERATOR_ADDRESS} with body
 * {@link UinGeneratorConstant#GENERATE_UIN} and replies {@link UINHealthConstants#ACTIVE}.
 * </p>
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

	/**
	 * Field for UinProcesser
	 */
	private UinProcesser uinProcesser;

	/**
	 * Resolves {@link UinProcesser} from {@code context}.
	 *
	 * @param context Spring context
	 */
	public UinGeneratorVerticle(final ApplicationContext context) {
		uinProcesser = (UinProcesser) context.getBean("uinProcesser");
	}

	private AtomicBoolean locked = new AtomicBoolean(false);

	/**
	 * Registers the UIN_GENERATOR_ADDRESS consumer that generates UINs when the pool is low.
	 */
	@Override
	public void start() {
		vertx.eventBus().consumer(UinGeneratorConstant.UIN_GENERATOR_ADDRESS, receivedMessage -> {
			if (receivedMessage.body().equals(UinGeneratorConstant.GENERATE_UIN) && uinProcesser.shouldGenerateUins()
					&& !locked.get()) {
				vertx.executeBlocking(future -> {
					locked.set(true);
					uinProcesser.generateUins();
					future.complete();
				}, result -> {
					if (result.succeeded()) {
						locked.set(false);
						 LOGGER.info("Generated and persisted uins lock set to false");
					} else {

						 LOGGER.error("Uin Genaration failed", result.cause());
					}
				});
			}else {
				 LOGGER.info("Generated and persisted uins lock is true.");
			}
			receivedMessage.reply(UINHealthConstants.ACTIVE);
		});
	}
}