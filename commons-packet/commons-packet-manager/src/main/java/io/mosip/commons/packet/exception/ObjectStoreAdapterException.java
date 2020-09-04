package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class ObjectStoreAdapterException extends BaseUncheckedException {

    public ObjectStoreAdapterException() {
        super(PacketUtilityErrorCodes.OS_ADAPTER_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.OS_ADAPTER_EXCEPTION.getErrorMessage());
    }

    public ObjectStoreAdapterException(String message) {
        super(PacketUtilityErrorCodes.OS_ADAPTER_EXCEPTION.getErrorCode(),
                message);
    }

    public ObjectStoreAdapterException(Throwable e) {
        super(PacketUtilityErrorCodes.OS_ADAPTER_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.OS_ADAPTER_EXCEPTION.getErrorMessage(), e);
    }

    public ObjectStoreAdapterException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.OS_ADAPTER_EXCEPTION.getErrorCode(), errorMessage, t);
    }
}
