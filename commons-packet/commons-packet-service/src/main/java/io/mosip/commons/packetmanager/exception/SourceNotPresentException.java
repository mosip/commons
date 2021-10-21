package io.mosip.commons.packetmanager.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class SourceNotPresentException extends BaseUncheckedException {
    public SourceNotPresentException() {
        super(PacketUtilityErrorCodes.SOURCE_NOT_PRESENT.getErrorCode(),
                PacketUtilityErrorCodes.SOURCE_NOT_PRESENT.getErrorMessage());
    }

    public SourceNotPresentException(String message) {
        super(PacketUtilityErrorCodes.SOURCE_NOT_PRESENT.getErrorCode(),
                message);
    }

    public SourceNotPresentException(Throwable e) {
        super(PacketUtilityErrorCodes.SOURCE_NOT_PRESENT.getErrorCode(),
                PacketUtilityErrorCodes.SOURCE_NOT_PRESENT.getErrorMessage(), e);
    }

    public SourceNotPresentException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.SOURCE_NOT_PRESENT.getErrorCode(), errorMessage, t);
    }
}
