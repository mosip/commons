/*
 * 
 * 
 */
package io.mosip.kernel.core.datavalidator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a phone number fails validation.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.datavalidator.spi.PhoneValidator}
 * for null or malformed numbers.
 * </p>
 *
 * @author Megha Tanga
 * @since 1.0.0
 */
public class InvalidPhoneNumberException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = -3556229489431119187L;

	/**
	 * Constructs an invalid-phone exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public InvalidPhoneNumberException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
