package io.mosip.kernel.clientcrypto.constant;

public enum ClientCryptoErrorConstants {

    TPM_REQUIRED("KER-CC-001", "TPM INSTANCE IS REQUIRED"),
    INITIALIZATION_ERROR("KER-CC-002", "FAILED TO INITIALIZE CC INSTANCE"),
    CONTEXT_RELOAD_REQUIRED("KER-CC-003", "Restart / reload context"),
    CRYPTO_FAILED("KER-CC-004", "Failed crypto operation"),
    TPM_REQUIRED_FLAG_NOT_SET("KER-CC-005", "TPM required flag not set");

    /**
     * The error code.
     */
    private final String errorCode;

    /**
     * The error message.
     */
    private final String errorMessage;

    /**
     * @param errorCode    The error code to be set.
     * @param errorMessage The error message to be set.
     */
    private ClientCryptoErrorConstants(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * @return The error code.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @return The error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
