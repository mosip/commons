package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class GetTagException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GetTagException() {
        super(PacketUtilityErrorCodes.TAGGING_FAILED.getErrorCode(),
                PacketUtilityErrorCodes.TAGGING_FAILED.getErrorMessage());
    }

	public GetTagException(String message) {
        super(PacketUtilityErrorCodes.TAGGING_FAILED.getErrorCode(),
                message);
    }

	public GetTagException(String errorCode, String message) {
        super(errorCode, message);
    }

	public GetTagException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.TAGGING_FAILED.getErrorCode(), errorMessage, t);
    }
}
