/*
 * 
 * 
 */
package io.mosip.kernel.core.datavalidator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when an email address fails validation.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.datavalidator.spi.EmailValidator}
 * for null or malformed addresses.
 * </p>
 *
 * @author Megha Tanga
 * @since 1.0.0
 */
public class InvalideEmailException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = -3556229489431119187L;

	/**
	 * Constructs an invalid-email exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public InvalideEmailException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
