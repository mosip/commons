package io.mosip.kernel.idgenerator.verticle;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.idgenerator.config.AccessLogHandler;
import io.mosip.kernel.idgenerator.config.UinServiceHealthCheckerhandler;
import io.mosip.kernel.idgenerator.config.UinServiceRouter;
import io.mosip.kernel.ridgenerator.router.RidFetcherRouter;
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
 * Vert.x HTTP server that mounts UIN, VID, and RID fetch routers.
 * <p>
 * Listens on {@code server.port}. After a successful bind it publishes
 * {@link EventType#CHECKPOOL} so VID pooling can start. Servlet paths default
 * to {@code /v1/idgenerator} and {@code /v1/ridgenerator}.
 * </p>
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

	private RidFetcherRouter ridFetcherRouter;

	/**
	 * Resolves fetch routers and the Spring environment from {@code context}.
	 *
	 * @param context Spring context created by {@code HibernateDaoConfig}
	 */
	public HttpServerVerticle(final ApplicationContext context) {
		vidFetcherRouter = (VidFetcherRouter) context.getBean("vidFetcherRouter");
		uinServiceRouter = (UinServiceRouter) context.getBean("uinServiceRouter");
		ridFetcherRouter = (RidFetcherRouter) context.getBean("ridFetcherRouter");
		environment = context.getEnvironment();
	}

	/**
	 * Starts the HTTP server and mounts VID, UIN, RID, health, and metrics routers.
	 *
	 * @param future completed when the server binds, or failed when listen fails
	 */
	@Override
	public void start(Future<Void> future) {
		HttpServer httpServer = vertx.createHttpServer();

		// Parent router so that global options can be applied to it in future
		Router parentRouter = Router.router(vertx);
		AccessLogHandler accessLogHandler = new AccessLogHandler();
		parentRouter.route().handler(routingContext -> {
			addAccessLogHandler(routingContext,accessLogHandler);
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

		String idPath = environment.getProperty(VIDGeneratorConstant.SERVER_SERVLET_PATH, "/v1/idgenerator");
		String ridPath = environment.getProperty("mosip.kernel.rid.servlet.path", "/v1/ridgenerator");

		// mount all the routers to parent router
		parentRouter.mountSubRouter(idPath + VIDGeneratorConstant.VVID, vidFetcherRouter.createRouter(vertx));
		parentRouter.mountSubRouter(idPath + UinGeneratorConstant.VUIN, uinServiceRouter.createRouter(vertx));
		parentRouter.mountSubRouter(ridPath, ridFetcherRouter.createRouter(vertx));
		parentRouter.mountSubRouter(idPath, healthCheckRouter);
		parentRouter.mountSubRouter(idPath, metricRouter);

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

	/**
	 * Registers an access-log callback that runs when the response body is fully written.
	 *
	 * @param context          current request
	 * @param accessLogHandler writer for the JSON access line
	 */
	private void addAccessLogHandler(final RoutingContext context, AccessLogHandler accessLogHandler) {
		long startMillis = System.currentTimeMillis();
		context.addBodyEndHandler(x -> accessLogHandler.log(context, startMillis));
		context.next();
	}
}