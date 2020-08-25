package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseCheckedException;

public class PacketKeeperException extends BaseCheckedException {

    /** Serializable version Id. */
    private static final long serialVersionUID = 1L;

    /**
     * @param errorMessage
     *            Message providing the specific context of the error.
     * @param cause
     *            Throwable cause for the specific exception
     */
    public PacketKeeperException(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);

    }

    public PacketKeeperException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
