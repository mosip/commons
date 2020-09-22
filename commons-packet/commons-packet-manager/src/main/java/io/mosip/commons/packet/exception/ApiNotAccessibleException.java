package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class ApiNotAccessibleException extends BaseUncheckedException {

    public ApiNotAccessibleException() {
        super(PacketUtilityErrorCodes.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.API_NOT_ACCESSIBLE_EXCEPTION.getErrorMessage());
    }

    public ApiNotAccessibleException(String message) {
        super(PacketUtilityErrorCodes.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
                message);
    }

    public ApiNotAccessibleException(Throwable e) {
        super(PacketUtilityErrorCodes.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.API_NOT_ACCESSIBLE_EXCEPTION.getErrorMessage(), e);
    }

    public ApiNotAccessibleException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.API_NOT_ACCESSIBLE_EXCEPTION.getErrorCode(), errorMessage, t);
    }


}
