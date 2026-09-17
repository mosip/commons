package io.mosip.kernel.core.util.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

/**
 * Checked exception thrown when Jackson JSON processing fails.
 * <p>
 * Contract: wraps {@code JsonProcessingException} from
 * {@link io.mosip.kernel.core.util.JsonUtils}. The single-argument constructor
 * does not set an error code.
 * </p>
 *
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
public class JsonProcessingException extends BaseCheckedException {
	private static final long serialVersionUID = 7784354823823721387L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    Jackson processing failure; may be null
	 */
	public JsonProcessingException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	/**
	 * Constructs an exception with only a message and no MOSIP error code.
	 *
	 * @param string human-readable description; may be null
	 */
	public JsonProcessingException(String string) {

	}

}
