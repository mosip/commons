package io.mosip.kernel.vidgenerator.verticle;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import io.mosip.kernel.vidgenerator.constant.VidIsolatorSchedulerConstants;
import io.mosip.kernel.vidgenerator.service.VidService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class VidIsolatorVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(VidIsolatorVerticle.class);

	private VidService vidService;

	private Environment environment;

	private final AtomicBoolean isolationInProgress = new AtomicBoolean(false);

	public VidIsolatorVerticle(final ApplicationContext context) {
		this.environment = context.getBean(Environment.class);
		this.vidService = context.getBean(VidService.class);
	}

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		vertx.deployVerticle(VidIsolatorSchedulerConstants.CEYLON_SCHEDULER, this::schedulerResult);
	}

	public void schedulerResult(AsyncResult<String> result) {
		if (result.succeeded()) {
			LOGGER.info("VidIsolatorVerticle deployment successfull");
			cronScheduling(vertx);
		} else if (result.failed()) {
			LOGGER.error("VidIsolatorVerticle deployment failed with cause ", result.cause());
		}
	}

	/**
	 * This method does the cron scheduling by fetchin cron expression from config
	 * serverl
	 *
	 * @param vertx the vertx
	 */
	private void cronScheduling(Vertx vertx) {

		EventBus eventBus = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eventBus.consumer(VidIsolatorSchedulerConstants.NAME_VALUE);

		// handle chime event — single-flight: skip tick if previous run still in progress
		consumer.handler(message -> {
			if (!isolationInProgress.compareAndSet(false, true)) {
				LOGGER.info("VID isolation skipped: previous run still in progress; will run on next schedule ({})",
						VidIsolatorSchedulerConstants.NAME_VALUE);
				return;
			}
			vertx.executeBlocking(future -> {
				try {
					vidService.isolateAssignedVids();
					future.complete();
				} catch (Exception e) {
					future.fail(e);
				}
			}, false, result -> {
				isolationInProgress.set(false);
				if (result.failed()) {
					LOGGER.error("VID isolation failed", result.cause());
				}
			});
		});

		JsonObject timer = new JsonObject()
			.put(VidIsolatorSchedulerConstants.TYPE, environment.getProperty(VidIsolatorSchedulerConstants.TYPE_VALUE))
			.put(VidIsolatorSchedulerConstants.SECONDS, environment.getProperty(VidIsolatorSchedulerConstants.SECONDS_VALUE))
			.put(VidIsolatorSchedulerConstants.MINUTES, environment.getProperty(VidIsolatorSchedulerConstants.MINUTES_VALUE))
			.put(VidIsolatorSchedulerConstants.HOURS, environment.getProperty(VidIsolatorSchedulerConstants.HOURS_VALUE))
			.put(VidIsolatorSchedulerConstants.DAY_OF_MONTH,
				environment.getProperty(VidIsolatorSchedulerConstants.DAY_OF_MONTH_VALUE))
			.put(VidIsolatorSchedulerConstants.MONTHS, environment.getProperty(VidIsolatorSchedulerConstants.MONTHS_VALUE))
			.put(VidIsolatorSchedulerConstants.DAYS_OF_WEEK,
				environment.getProperty(VidIsolatorSchedulerConstants.DAYS_OF_WEEK_VALUE));

		eventBus.send(VidIsolatorSchedulerConstants.CHIME,
			new JsonObject().put(VidIsolatorSchedulerConstants.OPERATION, VidIsolatorSchedulerConstants.OPERATION_VALUE)
				.put(VidIsolatorSchedulerConstants.NAME, VidIsolatorSchedulerConstants.NAME_VALUE)
				.put(VidIsolatorSchedulerConstants.DESCRIPTION, timer),
			res -> {
				if (res.succeeded()) {
					LOGGER.info("VidIsolatorVerticle started");
				} else if (res.failed()) {
					LOGGER.error("VidIsolatorVerticle failed with cause ", res.cause());
					vertx.close();
				}
			}
		);
	}
}
