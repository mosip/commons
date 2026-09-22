package io.mosip.kernel.core.util.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

/**
 * Checked exception thrown when JSON text cannot be parsed.
 * <p>
 * Contract: wraps Jackson parse failures from
 * {@link io.mosip.kernel.core.util.JsonUtils}. Callers must handle or declare
 * this exception.
 * </p>
 *
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
public class JsonParseException extends BaseCheckedException {
	private static final long serialVersionUID = 7469054823823721387L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    Jackson parse failure; may be null
	 */
	public JsonParseException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

}
