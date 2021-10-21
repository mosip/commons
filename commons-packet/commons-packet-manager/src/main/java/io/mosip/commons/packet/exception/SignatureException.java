package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class SignatureException extends BaseUncheckedException {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new file not found in destination exception.
	 */
	public SignatureException() {
		super();

	}


	public SignatureException(String errorMessage) {
		super(PacketUtilityErrorCodes.SIGNATURE_EXCEPTION.getErrorCode(), errorMessage);
	}


	public SignatureException(String message, Throwable cause) {
		super(PacketUtilityErrorCodes.SIGNATURE_EXCEPTION.getErrorCode() + EMPTY_SPACE, message, cause);

	}

	public SignatureException(Throwable t) {
		super(PacketUtilityErrorCodes.SIGNATURE_EXCEPTION.getErrorCode(),
				PacketUtilityErrorCodes.SIGNATURE_EXCEPTION.getErrorMessage(), t);
	}
}
