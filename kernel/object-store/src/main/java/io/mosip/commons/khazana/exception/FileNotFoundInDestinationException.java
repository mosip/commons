package io.mosip.commons.khazana.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class FileNotFoundInDestinationException extends BaseUncheckedException {

    public FileNotFoundInDestinationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public FileNotFoundInDestinationException(String errorCode, String errorMessage, Throwable t) {
        super(errorCode, errorMessage, t);
    }
}
