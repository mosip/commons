package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class TagCreationException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TagCreationException() {
        super(PacketUtilityErrorCodes.TAGGING_FAILED.getErrorCode(),
                PacketUtilityErrorCodes.TAGGING_FAILED.getErrorMessage());
    }

	public TagCreationException(String message) {
        super(PacketUtilityErrorCodes.TAGGING_FAILED.getErrorCode(),
                message);
    }

	public TagCreationException(String errorCode, String message) {
        super(errorCode, message);
    }

	public TagCreationException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.TAGGING_FAILED.getErrorCode(), errorMessage, t);
    }
}
