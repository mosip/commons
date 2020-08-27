package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class GetAllMetaInfoException extends BaseUncheckedException {

    public GetAllMetaInfoException() {
        super(PacketUtilityErrorCodes.GET_ALL_METAINFO_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.GET_ALL_METAINFO_EXCEPTION.getErrorMessage());
    }

    public GetAllMetaInfoException(String message) {
        super(PacketUtilityErrorCodes.GET_ALL_METAINFO_EXCEPTION.getErrorCode(),
                message);
    }

    public GetAllMetaInfoException(String errorCode, String message) {
        super(errorCode, message);
    }

    public GetAllMetaInfoException(Throwable e) {
        super(PacketUtilityErrorCodes.GET_ALL_METAINFO_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.GET_ALL_METAINFO_EXCEPTION.getErrorMessage(), e);
    }

    public GetAllMetaInfoException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.GET_ALL_METAINFO_EXCEPTION.getErrorCode(), errorMessage, t);
    }
}
