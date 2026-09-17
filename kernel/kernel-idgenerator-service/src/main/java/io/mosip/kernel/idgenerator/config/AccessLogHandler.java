package io.mosip.kernel.idgenerator.config;

import io.mosip.kernel.core.util.DateUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * Writes a JSON access log line after a Vert.x HTTP request completes.
 */
public class AccessLogHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogHandler.class);
	
	/**
	 * Logs method, path, remote host, status, and elapsed time for {@code context}.
	 *
	 * @param context       completed routing context
	 * @param startTSmillis request start time in epoch milliseconds
	 */
	public void log(final RoutingContext context, long startTSmillis) {

		final HttpServerRequest request = context.request();
		final HttpServerResponse response = context.response();

		JsonObject jsonValues = new JsonObject()
				.put("@timestamp", DateUtils.getCurrentDateTimeString())
				.put("level", "ACCESS")
				.put("timeTaken", System.currentTimeMillis() - startTSmillis)
				.put("statusCode", response.getStatusCode())
				.put("req.method", request.method().name())
				.put("req.requestURI", request.path())
				.put("req.remoteHost", request.remoteAddress().host());

		LOGGER.info(jsonValues);
	}
}

