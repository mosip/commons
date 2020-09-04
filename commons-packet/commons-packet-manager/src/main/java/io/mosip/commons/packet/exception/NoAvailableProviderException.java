package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class NoAvailableProviderException extends BaseUncheckedException {

    public NoAvailableProviderException() {
        super(PacketUtilityErrorCodes.NO_AVAILABLE_PROVIDER.getErrorCode(),
                PacketUtilityErrorCodes.NO_AVAILABLE_PROVIDER.getErrorMessage());
    }

    public NoAvailableProviderException(String message) {
        super(PacketUtilityErrorCodes.NO_AVAILABLE_PROVIDER.getErrorCode(),
                message);
    }

    public NoAvailableProviderException(Throwable e) {
        super(PacketUtilityErrorCodes.NO_AVAILABLE_PROVIDER.getErrorCode(),
                PacketUtilityErrorCodes.NO_AVAILABLE_PROVIDER.getErrorMessage(), e);
    }

    public NoAvailableProviderException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.NO_AVAILABLE_PROVIDER.getErrorCode(), errorMessage, t);
    }
}
