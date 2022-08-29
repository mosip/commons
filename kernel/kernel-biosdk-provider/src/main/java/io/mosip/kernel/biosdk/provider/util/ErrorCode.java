package io.mosip.kernel.biosdk.provider.util;

public enum ErrorCode {
	
	NO_PROVIDERS("BIO_SDK_001", "No Biometric provider API implementations found"),
	SDK_INITIALIZATION_FAILED("BIO_SDK_002", "Failed to initialize %s due to %s"),
	NO_CONSTRUCTOR_FOUND("BIO_SDK_003", "Constructor not found for %s with args %s"),
	NO_SDK_CONFIG("BIO_SDK_004", "SDK Configurations not found"),
	INVALID_SDK_VERSION("BIO_SDK_005", "Configured SDK version is different"),
	UNSUPPORTED_OPERATION("BIO_SDK_006", "Unsupported Operation"),
	SDK_REGISTRY_EMPTY("BIO_SDK_007", "SDK provider registry is empty!");
	
	
	private String errorCode;
	private String errorMessage;
	
	ErrorCode(String errorCode, String errorMessage) {
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
