package io.mosip.commons.khazana.constant;

public enum KhazanaErrorCodes {

    CONTAINER_NOT_PRESENT_IN_DESTINATION("COM-KZN-001", "Container not found."),
    ENCRYPTION_FAILURE("COM-KZN-002", "Packet Encryption Failed-Invalid Packet format"),
    OBJECT_STORE_NOT_ACCESSIBLE("COM-KZN-003", "Object store not accessible");


    private final String errorCode;
    private final String errorMessage;

    private KhazanaErrorCodes(final String errorCode, final String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
