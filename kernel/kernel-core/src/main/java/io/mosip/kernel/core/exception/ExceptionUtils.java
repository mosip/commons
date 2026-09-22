package io.mosip.kernel.core.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

/**
 * Helpers for composing MOSIP exception messages, stack traces, and
 * {@link ServiceError} lists from HTTP JSON bodies.
 * <p>
 * Contract: this type is not instantiable. Methods are side-effect free except
 * {@link #logRootCause(Throwable)}, which writes to SLF4J. Call from exception
 * handlers and REST clients when unwrapping MOSIP error envelopes.
 * </p>
 *
 * @author Shashank Agrawal
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public final class ExceptionUtils {

	/**
	 * Logger used by {@link #logRootCause(Throwable)}.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

	/**
	 * Jackson mapper used to parse MOSIP error JSON; never null.
	 */
	private static  ObjectMapper objectMapper=JsonMapper.builder()
		    .addModule(new AfterburnerModule())
		    .build();
	
	/**
	 * Prevents instantiation of this utility class.
	 */
	private ExceptionUtils() {

	}

	/**
	 * Composes a detail message that appends the nested cause when present.
	 *
	 * @param message the outer exception message; may be null
	 * @param cause   the nested cause; may be null
	 * @return {@code message} unchanged when {@code cause} is null; otherwise a
	 *         never-null string that includes the cause
	 */
	public static String buildMessage(String message, Throwable cause) {
		if (cause != null) {
			StringBuilder sb = new StringBuilder();
			if (message != null) {
				sb.append(message).append("; ");
			}
			sb.append("\n");
			sb.append("nested exception is ").append(cause);
			return sb.toString();
		} else {
			return message;
		}
	}

	/**
	 * Renders the full stack trace of {@code throwable} as a string.
	 *
	 * @param throwable never-null exception whose stack to capture
	 * @return never-null stack-trace text
	 */
	public static String getStackTrace(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

	/**
	 * Parses a MOSIP HTTP response body and extracts the {@code errors} array.
	 * <p>
	 * Contract: returns an empty list when the body is not JSON, has no
	 * {@code errors} node, or parsing fails. Does not throw.
	 * </p>
	 *
	 * @param responseBody raw JSON response; may be null or empty
	 * @return never-null list of {@link ServiceError}; empty when none are found
	 */
	public static List<ServiceError> getServiceErrorList(String responseBody) {

		List<ServiceError> validationErrorsList = new ArrayList<>();

		try {
			JsonNode errorResponse = objectMapper.readTree(responseBody);

			if (errorResponse.has("errors")) {

				JsonNode errors = errorResponse.get("errors");

				Iterator<JsonNode> iter = errors.iterator();

				while (iter.hasNext()) {
					JsonNode parameterNode = iter.next();
					ServiceError serviceError = new ServiceError(getJsonValue(parameterNode, "errorCode"),
							getJsonValue(parameterNode, "message"));
					validationErrorsList.add(serviceError);
				}
			}
		} catch (Exception e) {
			// There is no Service error
		}

		return validationErrorsList;

	}

	/**
	 * Reads a JSON property as text, or null if the property is absent.
	 *
	 * @param node     never-null JSON object node
	 * @param propName never-null property name such as {@code errorCode}
	 * @return property text; null if missing
	 */
	private static String getJsonValue(JsonNode node, String propName) {
		if (node.get(propName) != null) {
			return node.get(propName).asText();
		}
		return null;
	}

	/**
	 * Logs the exception message at ERROR and the full stack at DEBUG.
	 *
	 * @param exception never-null throwable to log
	 */
	public static void logRootCause(Throwable exception) {
		logger.error("Exception Root Cause: {} ", exception.getMessage());
		logger.debug("Exception Root Cause:", exception);
	}
}
