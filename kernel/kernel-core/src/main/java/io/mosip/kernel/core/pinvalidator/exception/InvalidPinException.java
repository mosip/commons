/*
 * 
 * 
 */
package io.mosip.kernel.core.pinvalidator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a MOSIP PIN fails validation.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.pinvalidator.spi.PinValidator}
 * for null, wrong length, or non-numeric PINs.
 * </p>
 *
 * @author Uday Kumar
 * @since 1.0.0
 */
public class InvalidPinException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an invalid-PIN exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public InvalidPinException(String errorCode, String errorMessage) {

		super(errorCode, errorMessage);

	}
}
