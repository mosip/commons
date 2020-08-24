package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseCheckedException;

public class ZipParsingException extends BaseCheckedException {

    public ZipParsingException() {
        super(PacketUtilityErrorCodes.ZIP_PARSING_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.ZIP_PARSING_EXCEPTION.getErrorMessage());
    }

    public ZipParsingException(Throwable t) {
        super(PacketUtilityErrorCodes.ZIP_PARSING_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.ZIP_PARSING_EXCEPTION.getErrorMessage(), t);
    }

    /**
     * @param message
     *            Message providing the specific context of the error.
     * @param cause
     *            Throwable cause for the specific exception
     */
    public ZipParsingException(String message, Throwable cause) {
        super(PacketUtilityErrorCodes.ZIP_PARSING_EXCEPTION.getErrorCode(), message, cause);

    }

    public ZipParsingException(String errorMessage) {
        super(PacketUtilityErrorCodes.ZIP_PARSING_EXCEPTION.getErrorCode(), errorMessage);
    }
}
