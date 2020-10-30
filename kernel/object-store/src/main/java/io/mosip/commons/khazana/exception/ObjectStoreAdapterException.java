package io.mosip.commons.khazana.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class ObjectStoreAdapterException extends BaseUncheckedException {

    public ObjectStoreAdapterException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ObjectStoreAdapterException(String errorCode, String message, Throwable e) {
        super(errorCode, message, e);
    }
}
