package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class GetBiometricException extends BaseUncheckedException {
    
    public GetBiometricException() {
        super(PacketUtilityErrorCodes.DOCUMENT_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.DOCUMENT_EXCEPTION.getErrorMessage());
    }

    public GetBiometricException(String errorCode, String message) {
        super(errorCode, message);
    }

    public GetBiometricException(String message) {
        super(PacketUtilityErrorCodes.DOCUMENT_EXCEPTION.getErrorCode(),
                message);
    }

    public GetBiometricException(Throwable e) {
        super(PacketUtilityErrorCodes.DOCUMENT_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.DOCUMENT_EXCEPTION.getErrorMessage(), e);
    }

    public GetBiometricException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.DOCUMENT_EXCEPTION.getErrorCode(), errorMessage, t);
    }
}
