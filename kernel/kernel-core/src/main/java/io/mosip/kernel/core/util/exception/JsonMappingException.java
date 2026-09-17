package io.mosip.kernel.core.util.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

/**
 * Checked exception thrown when Jackson cannot map JSON to a Java type.
 * <p>
 * Contract: wraps {@code com.fasterxml.jackson.databind.JsonMappingException}
 * from {@link io.mosip.kernel.core.util.JsonUtils}. Callers must handle or
 * declare this exception.
 * </p>
 *
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
public class JsonMappingException extends BaseCheckedException {
	private static final long serialVersionUID = 7464354673823721387L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    Jackson mapping failure; may be null
	 */
	public JsonMappingException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

}
