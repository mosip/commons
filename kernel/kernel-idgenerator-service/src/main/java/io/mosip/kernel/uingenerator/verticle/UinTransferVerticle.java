package io.mosip.kernel.uingenerator.verticle;

import java.util.concurrent.atomic.AtomicBoolean;

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

	private final AtomicBoolean transferInProgress = new AtomicBoolean(false);

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

		// handle chime event — single-flight: skip tick if a transfer is still running (next schedule will retry)
		consumer.handler(message -> {
			if (!transferInProgress.compareAndSet(false, true)) {
				LOGGER.info("UIN transfer skipped: previous transfer still in progress; will run on next schedule");
				return;
			}
			vertx.executeBlocking(future -> {
				try {
					uinService.transferUin();
					future.complete();
				} catch (Exception e) {
					future.fail(e);
				}
			}, false, result -> {
				transferInProgress.set(false);
				if (result.failed()) {
					LOGGER.error("UIN transfer failed", result.cause());
				}
			});
		});

		JsonObject timer = new JsonObject()
				.put(UinSchedulerConstants.TYPE, environment.getProperty(UinSchedulerConstants.TYPE_VALUE))
				.put(UinSchedulerConstants.SECONDS, environment.getProperty(UinSchedulerConstants.SECONDS_VALUE))
				.put(UinSchedulerConstants.MINUTES, environment.getProperty(UinSchedulerConstants.MINUTES_VALUE))
				.put(UinSchedulerConstants.HOURS, environment.getProperty(UinSchedulerConstants.HOURS_VALUE))
				.put(UinSchedulerConstants.DAY_OF_MONTH,
						environment.getProperty(UinSchedulerConstants.DAY_OF_MONTH_VALUE))
				.put(UinSchedulerConstants.MONTHS, environment.getProperty(UinSchedulerConstants.MONTHS_VALUE))
				.put(UinSchedulerConstants.DAYS_OF_WEEK,
						environment.getProperty(UinSchedulerConstants.DAYS_OF_WEEK_VALUE));

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
