package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * PacketDecryptionFailureException class.
 *
 * @author Jyoti Prakash Nayak
 */
public class PacketDecryptionFailureException extends BaseUncheckedException {

	/** Serializable version Id. */
	private static final long serialVersionUID = 1L;

	public PacketDecryptionFailureException() {
		super(PacketUtilityErrorCodes.PACKET_DECRYPTION_FAILURE_EXCEPTION.getErrorCode(),
				PacketUtilityErrorCodes.PACKET_DECRYPTION_FAILURE_EXCEPTION.getErrorMessage());
	}

	public PacketDecryptionFailureException(Throwable t) {
		super(PacketUtilityErrorCodes.PACKET_DECRYPTION_FAILURE_EXCEPTION.getErrorCode(),
				PacketUtilityErrorCodes.PACKET_DECRYPTION_FAILURE_EXCEPTION.getErrorMessage(), t);
	}

	/**
	 * @param message
	 *            Message providing the specific context of the error.
	 * @param cause
	 *            Throwable cause for the specific exception
	 */
	public PacketDecryptionFailureException(String message, Throwable cause) {
		super(PacketUtilityErrorCodes.PACKET_DECRYPTION_FAILURE_EXCEPTION.getErrorCode(), message, cause);

	}

	public PacketDecryptionFailureException(String errorMessage) {
		super(PacketUtilityErrorCodes.PACKET_DECRYPTION_FAILURE_EXCEPTION.getErrorCode(), errorMessage);
	}

}