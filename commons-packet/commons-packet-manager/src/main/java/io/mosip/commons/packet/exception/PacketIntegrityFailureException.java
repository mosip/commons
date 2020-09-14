package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class PacketIntegrityFailureException extends BaseCheckedException {

    /** Serializable version Id. */
    private static final long serialVersionUID = 1L;

    public PacketIntegrityFailureException() {
        super(PacketUtilityErrorCodes.INTEGRITY_FAILURE.getErrorCode(),
                PacketUtilityErrorCodes.INTEGRITY_FAILURE.getErrorMessage());
    }

    public PacketIntegrityFailureException(Throwable t) {
        super(PacketUtilityErrorCodes.INTEGRITY_FAILURE.getErrorCode(),
                PacketUtilityErrorCodes.INTEGRITY_FAILURE.getErrorMessage(), t);
    }

    /**
     * @param message
     *            Message providing the specific context of the error.
     * @param cause
     *            Throwable cause for the specific exception
     */
    public PacketIntegrityFailureException(String message, Throwable cause) {
        super(PacketUtilityErrorCodes.INTEGRITY_FAILURE.getErrorCode(), message, cause);

    }

    public PacketIntegrityFailureException(String errorMessage) {
        super(PacketUtilityErrorCodes.INTEGRITY_FAILURE.getErrorCode(), errorMessage);
    }
}
