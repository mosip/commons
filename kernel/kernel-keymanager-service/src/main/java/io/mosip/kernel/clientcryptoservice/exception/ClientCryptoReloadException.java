package io.mosip.kernel.clientcryptoservice.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;


/**
 * @author Anusha Sunkada
 * @since 1.2.0
 */
public class ClientCryptoReloadException extends BaseCheckedException {

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
    public ClientCryptoReloadException(String errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

}
