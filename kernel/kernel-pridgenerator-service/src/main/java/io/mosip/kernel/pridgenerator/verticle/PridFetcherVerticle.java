package io.mosip.kernel.pridgenerator.verticle;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.idgenerator.config.UinServiceHealthCheckerhandler;
import io.mosip.kernel.pridgenerator.config.PridServiceHealthCheckerhandler;
import io.mosip.kernel.pridgenerator.constant.EventType;
import io.mosip.kernel.pridgenerator.constant.PRIDGeneratorConstant;
import io.mosip.kernel.pridgenerator.router.PridFetcherRouter;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.micrometer.PrometheusScrapingHandler;

public class PridFetcherVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(PridFetcherVerticle.class);

	private Environment environment;

	/**
	 * Field for PridGeneratorRouter
	 */
	private PridFetcherRouter pridFetcherRouter;

	// private AuthHandler authHandler;

	/**
	 * Initialize beans
	 * 
	 * @param context context
	 */
	public PridFetcherVerticle(final ApplicationContext context) {
		// authHandler = (AuthHandler) context.getBean("authHandler");
		pridFetcherRouter = (PridFetcherRouter) context.getBean("pridFetcherRouter");
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
		Router metricRouter = Router.router(vertx);
		metricRouter.route("/metrics").handler(PrometheusScrapingHandler.create());
		Router healthCheckRouter = Router.router(vertx);
		PridServiceHealthCheckerhandler healthCheckHandler = new PridServiceHealthCheckerhandler(vertx, null,
				new ObjectMapper(), environment);
		healthCheckRouter.get(PRIDGeneratorConstant.HEALTH_ENDPOINT)
				.handler(healthCheckHandler);
		healthCheckHandler.register("db", healthCheckHandler::databaseHealthChecker);
		healthCheckHandler.register("diskspace", healthCheckHandler::dispSpaceHealthChecker);
		healthCheckHandler.register("pridgenerator", f -> healthCheckHandler.verticleHealthHandler(f, vertx));
		// giving the root to parent router
		parentRouter.route().consumes(PRIDGeneratorConstant.APPLICATION_JSON)
				.produces(PRIDGeneratorConstant.APPLICATION_JSON);
		// mount all the routers to parent router
		parentRouter.mountSubRouter(environment.getProperty(PRIDGeneratorConstant.SERVER_SERVLET_PATH), metricRouter);
		parentRouter.mountSubRouter(environment.getProperty(PRIDGeneratorConstant.SERVER_SERVLET_PATH), healthCheckRouter);
		parentRouter.mountSubRouter(
				environment.getProperty(PRIDGeneratorConstant.SERVER_SERVLET_PATH) + PRIDGeneratorConstant.PRID,
				pridFetcherRouter.createRouter(vertx));

		httpServer.requestHandler(parentRouter);
		httpServer.listen(Integer.parseInt(environment.getProperty(PRIDGeneratorConstant.SERVER_PORT)), result -> {
			if (result.succeeded()) {
				LOGGER.debug("prid fetcher verticle deployed");
				vertx.eventBus().publish(EventType.CHECKPOOL, EventType.CHECKPOOL);
				future.complete();
			} else if (result.failed()) {
				LOGGER.error("prid fetcher verticle deployment failed with cause ", result.cause());
				future.fail(result.cause());
			}
		});
	}
}
