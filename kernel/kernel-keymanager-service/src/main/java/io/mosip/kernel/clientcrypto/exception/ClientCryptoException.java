package io.mosip.kernel.clientcrypto.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * @author Anusha Sunkada
 * @since 1.1.2
 *
 */
public class ClientCryptoException extends BaseUncheckedException  {

    /**
     * Generated serial version id
     */
    private static final long serialVersionUID = 8621530697947108810L;

    /**
     * Constructor the initialize Handler exception
     *
     * @param errorCode    The errorcode for this exception
     * @param errorMessage The error message for this exception
     */
    public ClientCryptoException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * @param errorCode    The errorcode for this exception
     * @param errorMessage The error message for this exception
     * @param rootCause    cause of the error occoured
     */
    public ClientCryptoException(String errorCode, String errorMessage, Throwable rootCause) {
        super(errorCode, errorMessage, rootCause);
    }
}
