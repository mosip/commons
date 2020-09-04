package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class FieldNameNotFoundException extends BaseUncheckedException {

    public FieldNameNotFoundException() {
        super(PacketUtilityErrorCodes.BIOMETRIC_FIELDNAME_NOT_FOUND.getErrorCode(),
                PacketUtilityErrorCodes.BIOMETRIC_FIELDNAME_NOT_FOUND.getErrorMessage());
    }

    public FieldNameNotFoundException(String message) {
        super(PacketUtilityErrorCodes.BIOMETRIC_FIELDNAME_NOT_FOUND.getErrorCode(),
                message);
    }

    public FieldNameNotFoundException(Throwable e) {
        super(PacketUtilityErrorCodes.BIOMETRIC_FIELDNAME_NOT_FOUND.getErrorCode(),
                PacketUtilityErrorCodes.BIOMETRIC_FIELDNAME_NOT_FOUND.getErrorMessage(), e);
    }

    public FieldNameNotFoundException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.BIOMETRIC_FIELDNAME_NOT_FOUND.getErrorCode(), errorMessage, t);
    }
}
