package io.mosip.kernel.idgenerator.verticle;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.idgenerator.config.AccessLogHandler;
import io.mosip.kernel.idgenerator.config.UinServiceHealthCheckerhandler;
import io.mosip.kernel.idgenerator.config.UinServiceRouter;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.vidgenerator.constant.EventType;
import io.mosip.kernel.vidgenerator.constant.VIDGeneratorConstant;
import io.mosip.kernel.vidgenerator.router.VidFetcherRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.micrometer.PrometheusScrapingHandler;

/**
 * Http Verticle for fetching UIN and VID
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */

public class HttpServerVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

	private Environment environment;

	/**
	 * Field for UinGeneratorRouter
	 */
	private VidFetcherRouter vidFetcherRouter;

	private UinServiceRouter uinServiceRouter;

	/**
	 * Initialize beans
	 * 
	 * @param context context
	 */
	public HttpServerVerticle(final ApplicationContext context) {
		vidFetcherRouter = (VidFetcherRouter) context.getBean("vidFetcherRouter");
		uinServiceRouter = (UinServiceRouter) context.getBean("uinServiceRouter");
		environment = context.getEnvironment();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.vertx.core.AbstractVerticle#start(io.vertx.core.Future)
	 */
	@Override
	public void start(Future<Void> future) {
		HttpServer httpServer = vertx.createHttpServer();

		// Parent router so that global options can be applied to it in future
		Router parentRouter = Router.router(vertx);
		AccessLogHandler accessLogHandler = new AccessLogHandler();
		parentRouter.route().handler(routingContext -> {
			accessLogHandler(routingContext,accessLogHandler);
		});
		Router metricRouter = Router.router(vertx);
		// giving the root to parent router
		parentRouter.route().consumes(VIDGeneratorConstant.APPLICATION_JSON)
				.produces(VIDGeneratorConstant.APPLICATION_JSON);
		Router healthCheckRouter = Router.router(vertx);
		UinServiceHealthCheckerhandler healthCheckHandler = new UinServiceHealthCheckerhandler(vertx, null,
				new ObjectMapper(), environment);
		healthCheckRouter.get(UinGeneratorConstant.HEALTH_ENDPOINT)
				.handler(healthCheckHandler);
		healthCheckHandler.register("db", healthCheckHandler::databaseHealthChecker);
		healthCheckHandler.register("diskspace", healthCheckHandler::dispSpaceHealthChecker);
		healthCheckHandler.register("idgenerator", f -> healthCheckHandler.verticleHealthHandler(f, vertx));

		metricRouter.route("/metrics").handler(PrometheusScrapingHandler.create());

		// mount all the routers to parent router
		parentRouter.mountSubRouter(
				environment.getProperty(VIDGeneratorConstant.SERVER_SERVLET_PATH) + VIDGeneratorConstant.VVID,
				vidFetcherRouter.createRouter(vertx));
		parentRouter.mountSubRouter(
				environment.getProperty(VIDGeneratorConstant.SERVER_SERVLET_PATH) + UinGeneratorConstant.VUIN,
				uinServiceRouter.createRouter(vertx));
		parentRouter.mountSubRouter(environment.getProperty(VIDGeneratorConstant.SERVER_SERVLET_PATH), healthCheckRouter);
		parentRouter.mountSubRouter(environment.getProperty(VIDGeneratorConstant.SERVER_SERVLET_PATH), metricRouter);

		httpServer.requestHandler(parentRouter);
		httpServer.listen(Integer.parseInt(environment.getProperty(VIDGeneratorConstant.SERVER_PORT)), result -> {
			if (result.succeeded()) {
				LOGGER.debug("vid fetcher verticle deployed");
				vertx.eventBus().publish(EventType.CHECKPOOL, EventType.CHECKPOOL);
				future.complete();
			} else if (result.failed()) {
				LOGGER.error("vid fetcher verticle deployment failed with cause ", result.cause());
				future.fail(result.cause());
			}
		});
	}

	private void accessLogHandler(final RoutingContext context, AccessLogHandler accessLogHandler) {

		long startMillis = System.currentTimeMillis();

		context.addBodyEndHandler(x -> accessLogHandler.log(context, startMillis));

		context.next();

	}

	

}
