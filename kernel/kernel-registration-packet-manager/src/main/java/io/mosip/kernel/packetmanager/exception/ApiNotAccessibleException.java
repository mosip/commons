package io.mosip.kernel.packetmanager.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.packetmanager.constants.PacketUtilityErrorCodes;

public class ApiNotAccessibleException extends BaseCheckedException {

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
