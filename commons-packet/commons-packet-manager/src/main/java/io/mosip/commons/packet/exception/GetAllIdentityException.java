package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class GetAllIdentityException extends BaseUncheckedException {

    public GetAllIdentityException() {
        super(PacketUtilityErrorCodes.GET_ALL_IDENTITY_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.GET_ALL_IDENTITY_EXCEPTION.getErrorMessage());
    }

    public GetAllIdentityException(String message) {
        super(PacketUtilityErrorCodes.GET_ALL_IDENTITY_EXCEPTION.getErrorCode(),
                message);
    }

    public GetAllIdentityException(String errorCode, String message) {
        super(errorCode, message);
    }

    public GetAllIdentityException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.GET_ALL_IDENTITY_EXCEPTION.getErrorCode(), errorMessage, t);
    }
}
