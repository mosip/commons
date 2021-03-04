package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class TagDeletionException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TagDeletionException() {
        super(PacketUtilityErrorCodes.DELETE_TAGGING_FAILED.getErrorCode(),
                PacketUtilityErrorCodes.DELETE_TAGGING_FAILED.getErrorMessage());
    }

	public TagDeletionException(String message) {
        super(PacketUtilityErrorCodes.DELETE_TAGGING_FAILED.getErrorCode(),
                message);
    }

	public TagDeletionException(String errorCode, String message) {
        super(errorCode, message);
    }

	public TagDeletionException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.DELETE_TAGGING_FAILED.getErrorCode(), errorMessage, t);
    }
}
