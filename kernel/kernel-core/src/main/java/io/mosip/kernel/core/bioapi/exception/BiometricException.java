package io.mosip.kernel.core.bioapi.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

/**
 * Checked exception thrown when a biometric API operation fails.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.bioapi.spi.IBioApi}
 * implementations for invalid input, quality-check, or matching failures.
 * Prefer {@link io.mosip.kernel.core.bioapi.constant.BioApiErrorConstant} for
 * {@code errorCode} and {@code errorMessage}.
 * </p>
 *
 * @see io.mosip.kernel.core.bioapi.constant.BioApiErrorConstant
 * @author Sanjay Murali
 */
public class BiometricException extends BaseCheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -9125558120446752522L;

	/**
	 * Constructs a biometric exception with a root cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public BiometricException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	/**
	 * Constructs a biometric exception without a root cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public BiometricException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

}
