package io.mosip.kernel.ridgenerator.router;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.authmanager.authadapter.spi.VertxAuthenticationProvider;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.ridgenerator.constant.RidGeneratorExceptionConstant;
import io.mosip.kernel.ridgenerator.dto.RidGeneratorResponseDto;
import io.mosip.kernel.ridgenerator.exception.EmptyInputException;
import io.mosip.kernel.ridgenerator.exception.InputLengthException;
import io.mosip.kernel.ridgenerator.exception.RidException;
import io.mosip.kernel.ridgenerator.service.RidGeneratorService;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x router for {@code GET /v1/ridgenerator/generate/rid/{centerid}/{machineid}}.
 * <p>
 * Requires {@code REGISTRATION_PROCESSOR} when an auth adapter is on the classpath.
 * Success and MOSIP errors are HTTP 200 JSON {@code ResponseWrapper} bodies.
 * </p>
 */
@Component
public class RidFetcherRouter {

	/**
	 * Shared Vert.x worker pool size ({@code mosip.kernel.rid.get_executor_pool}).
	 */
	@Value("${mosip.kernel.rid.get_executor_pool:400}")
	private int workerExecutorPool;

	private static final Logger LOGGER = LoggerFactory.getLogger(RidFetcherRouter.class);

	@Autowired
	private RidGeneratorService<RidGeneratorResponseDto> ridGeneratorService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired(required = false)
	private VertxAuthenticationProvider authHandler;

	/**
	 * Builds the RID generate GET route.
	 *
	 * @param vertx Vert.x instance used for the worker executor
	 * @return configured router
	 */
	public Router createRouter(Vertx vertx) {
		LOGGER.info("RID worker executor pool {}", workerExecutorPool);
		Router router = Router.router(vertx);
		if (authHandler != null) {
			authHandler.addAuthFilter(router, "/generate/rid/:centerid/:machineid", HttpMethod.GET,
					"REGISTRATION_PROCESSOR");
		}
		router.get("/generate/rid/:centerid/:machineid").handler(routingContext -> {
			routingContext.response().headers().add("Content-Type", "application/json");
			ResponseWrapper<RidGeneratorResponseDto> reswrp = new ResponseWrapper<>();
			WorkerExecutor executor = vertx.createSharedWorkerExecutor("get-rid", workerExecutorPool);
			executor.executeBlocking(blockingCodeHandler -> {
				try {
					String centerId = routingContext.pathParam("centerid");
					String machineId = routingContext.pathParam("machineid");
					RidGeneratorResponseDto response = ridGeneratorService.generateRid(centerId.trim(),
							machineId.trim());
					String timestamp = DateUtils.getUTCCurrentDateTimeString();
					reswrp.setResponsetime(DateUtils.convertUTCToLocalDateTime(timestamp));
					reswrp.setResponse(response);
					reswrp.setErrors(null);
					blockingCodeHandler.complete();
				} catch (EmptyInputException | InputLengthException | RidException exception) {
					ServiceError error = new ServiceError(exception.getErrorCode(), exception.getMessage());
					setError(routingContext, error, blockingCodeHandler);
				} catch (Exception exception) {
					ServiceError error = new ServiceError(
							RidGeneratorExceptionConstant.RID_FETCH_EXCEPTION.getErrorCode(), exception.getMessage());
					setError(routingContext, error, blockingCodeHandler);
				}
			}, false, resultHandler -> {
				if (resultHandler.succeeded()) {
					try {
						routingContext.response().end(objectMapper.writeValueAsString(reswrp));
					} catch (JsonProcessingException exception) {
						ExceptionUtils.logRootCause(exception);
						ServiceError error = new ServiceError(
								RidGeneratorExceptionConstant.RID_FETCH_EXCEPTION.getErrorCode(),
								exception.getMessage());
						setError(routingContext, error, null);
					}
				}
			});
		});
		return router;
	}

	/**
	 * Writes HTTP 200 MOSIP error JSON and optionally fails the worker promise.
	 *
	 * @param routingContext      current request
	 * @param error               MOSIP service error
	 * @param blockingCodeHandler worker promise to fail; may be {@code null}
	 */
	private void setError(RoutingContext routingContext, ServiceError error, Promise<Object> blockingCodeHandler) {
		ResponseWrapper<ServiceError> errorResponse = new ResponseWrapper<>();
		errorResponse.getErrors().add(error);
		objectMapper.registerModule(new JavaTimeModule());
		JsonNode reqNode;
		if (routingContext.getBodyAsJson() != null) {
			try {
				reqNode = objectMapper.readTree(routingContext.getBodyAsJson().toString());
				errorResponse.setId(reqNode.path("id").asText());
				errorResponse.setVersion(reqNode.path("version").asText());
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}
		try {
			routingContext.response().setStatusCode(200).end(objectMapper.writeValueAsString(errorResponse));
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage());
		}
		LOGGER.error(error.getMessage());
		if (blockingCodeHandler != null) {
			blockingCodeHandler.fail(error.getMessage());
		}
	}
}
