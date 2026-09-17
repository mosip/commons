package io.mosip.kernel.core.idobjectvalidator.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant;

/**
 * Checked exception thrown when identity JSON cannot be read or converted.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator}
 * on parse or I/O failures. Prefer {@link IdObjectValidatorErrorConstant}.
 * </p>
 *
 * @author Manoj SP
 * @author Swati Raj
 * @since 1.0.0
 */
public class IdObjectIOException extends BaseCheckedException {

	/**
	 * Generated serialization ID.
	 */
	private static final long serialVersionUID = 795618868850353876L;

	/**
	 * Constructs the exception from an error constant.
	 *
	 * @param errorConstant never-null constant supplying code and message
	 */
	public IdObjectIOException(IdObjectValidatorErrorConstant errorConstant) {
		super(errorConstant.getErrorCode(), errorConstant.getMessage());
	}

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public IdObjectIOException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception from an error constant and cause.
	 *
	 * @param errorConstant never-null constant supplying code and message
	 * @param rootCause     underlying cause; may be null
	 */
	public IdObjectIOException(IdObjectValidatorErrorConstant errorConstant, Throwable rootCause) {
		super(errorConstant.getErrorCode(), errorConstant.getMessage(), rootCause);
	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public IdObjectIOException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
