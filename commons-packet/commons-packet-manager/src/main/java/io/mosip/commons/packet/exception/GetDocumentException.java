package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class GetDocumentException extends BaseUncheckedException {

    public GetDocumentException() {
        super(PacketUtilityErrorCodes.BIOMETRIC_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.BIOMETRIC_EXCEPTION.getErrorMessage());
    }

    public GetDocumentException(String message) {
        super(PacketUtilityErrorCodes.BIOMETRIC_EXCEPTION.getErrorCode(),
                message);
    }

    public GetDocumentException(Throwable e) {
        super(PacketUtilityErrorCodes.BIOMETRIC_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.BIOMETRIC_EXCEPTION.getErrorMessage(), e);
    }

    public GetDocumentException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.BIOMETRIC_EXCEPTION.getErrorCode(), errorMessage, t);
    }
}
