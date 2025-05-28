package io.mosip.kernel.uingenerator.verticle;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import io.mosip.kernel.uingenerator.constant.UinSchedulerConstants;
import io.mosip.kernel.uingenerator.service.UinService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class UinTransferVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(UinTransferVerticle.class);

	private UinService uinService;

	private Environment environment;

	public UinTransferVerticle(final ApplicationContext context) {
		this.environment = context.getBean(Environment.class);
		this.uinService = context.getBean(UinService.class);
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		vertx.deployVerticle(UinSchedulerConstants.CEYLON_SCHEDULER, this::schedulerResult);
	}

	public void schedulerResult(AsyncResult<String> result) {
		if (result.succeeded()) {
			LOGGER.debug("scheduler verticle deployment successfull");
			cronScheduling(vertx);
		} else if (result.failed()) {
			LOGGER.error("scheduler verticle deployment failed with cause ", result.cause());
		}
	}

	/**
	 * This method does the cron scheduling by fetchin cron expression from config
	 * server
	 *
	 * @param vertx the vertx
	 */
	private void cronScheduling(Vertx vertx) {

		EventBus eventBus = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eventBus.consumer(UinSchedulerConstants.NAME_VALUE);

		// handle chime event
		consumer.handler(message -> uinService.transferUin());

		JsonObject timer = new JsonObject()
				.put(UinSchedulerConstants.TYPE, environment.getProperty(UinSchedulerConstants.TYPE_VALUE))
				.put(UinSchedulerConstants.SECONDS, "0")
				.put(UinSchedulerConstants.MINUTES,"*")
				.put(UinSchedulerConstants.HOURS, "*")
				.put(UinSchedulerConstants.DAY_OF_MONTH, "*")
				.put(UinSchedulerConstants.MONTHS, "*")
				.put(UinSchedulerConstants.DAYS_OF_WEEK, "*");

		eventBus.send(UinSchedulerConstants.CHIME,
				new JsonObject().put(UinSchedulerConstants.OPERATION, UinSchedulerConstants.OPERATION_VALUE)
						.put(UinSchedulerConstants.NAME, UinSchedulerConstants.NAME_VALUE)
						.put(UinSchedulerConstants.DESCRIPTION, timer),
				res -> {
					if (res.succeeded()) {
						LOGGER.debug("VIDRevokerschedular started");
					} else if (res.failed()) {
						LOGGER.error("VIDRevokerschedular failed with cause ", res.cause());
						vertx.close();
					}
				});

	}
}
