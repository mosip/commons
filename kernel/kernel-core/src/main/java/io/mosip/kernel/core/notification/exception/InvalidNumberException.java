package io.mosip.kernel.core.notification.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when an SMS destination number is invalid.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.notification.spi.SMSServiceProvider}
 * when {@code contactNumber} is null, empty, or fails format rules.
 * </p>
 *
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public class InvalidNumberException extends BaseUncheckedException {

	/**
	 * Generated ID.
	 */
	private static final long serialVersionUID = -6174268206879672695L;

	/**
	 * Constructs an invalid-number exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public InvalidNumberException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
