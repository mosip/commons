package io.mosip.commons.packet.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

import static io.mosip.commons.packet.constants.PacketUtilityErrorCodes.PACKET_VALIDATION_FAILED;

public class PacketValidationFailureException extends BaseUncheckedException {

    public PacketValidationFailureException(String message, Throwable t) {
        super(PACKET_VALIDATION_FAILED.getErrorCode(), message, t);
    }

}
