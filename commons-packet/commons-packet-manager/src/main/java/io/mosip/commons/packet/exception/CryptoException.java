package io.mosip.commons.packet.exception;

import io.mosip.commons.packet.constants.PacketUtilityErrorCodes;
import io.mosip.kernel.core.exception.BaseUncheckedException;

public class CryptoException extends BaseUncheckedException {

    public CryptoException() {
        super(PacketUtilityErrorCodes.CRYPTO_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.CRYPTO_EXCEPTION.getErrorMessage());
    }

    public CryptoException(String message) {
        super(PacketUtilityErrorCodes.CRYPTO_EXCEPTION.getErrorCode(),
                message);
    }

    public CryptoException(Throwable e) {
        super(PacketUtilityErrorCodes.CRYPTO_EXCEPTION.getErrorCode(),
                PacketUtilityErrorCodes.CRYPTO_EXCEPTION.getErrorMessage(), e);
    }

    public CryptoException(String errorMessage, Throwable t) {
        super(PacketUtilityErrorCodes.CRYPTO_EXCEPTION.getErrorCode(), errorMessage, t);
    }
}
