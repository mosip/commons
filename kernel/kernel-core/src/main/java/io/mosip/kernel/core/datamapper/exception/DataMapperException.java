package io.mosip.kernel.core.datamapper.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when bean mapping fails.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.datamapper.spi.DataMapper}
 * implementations on type mismatch, missing properties, or converter errors.
 * </p>
 *
 * @author Neha
 * @since 1.0.0
 */
public class DataMapperException extends BaseUncheckedException {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public DataMapperException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public DataMapperException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception with a detail message only.
	 *
	 * @param errorMessage never-null human-readable description
	 */
	public DataMapperException(String errorMessage) {
		super(errorMessage);
	}

}
