package io.mosip.commons.khazana.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

public class ObjectStoreAdapterException extends BaseCheckedException {

    public ObjectStoreAdapterException(String errorCode, String message, Throwable t) {
        super(errorCode, message, t);
    }
}
