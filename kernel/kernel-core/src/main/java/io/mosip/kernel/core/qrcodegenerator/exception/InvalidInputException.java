package io.mosip.kernel.core.qrcodegenerator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when QR-code input is null, empty, or otherwise invalid.
 * <p>
 * Contract: raised before encoding when {@code data} or {@code version} fails
 * validation. Does not perform I/O.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class InvalidInputException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = -5350213197226295789L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public InvalidInputException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
