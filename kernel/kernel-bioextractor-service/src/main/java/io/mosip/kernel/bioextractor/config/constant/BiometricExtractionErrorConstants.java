package io.mosip.kernel.bioextractor.config.constant;

public enum BiometricExtractionErrorConstants {
	
	UNKNOWN_ERROR("KER_BIE_001", "Unknown Error"),
	MISSING_INPUT_PARAMETER("KER_BIE-002", "Missing Input Parameter - %s"),
	INVALID_INPUT_PARAMETER("KER_BIE-003", "Invalid Input Parameter - %s"),
	DOWNLOAD_BIOMETRICS_ERROR("KER_BIE-004", "Error in downloading biometrics"),
	DECRYPTION_ERROR("KER_BIE-005", "Decryption error"),
	INVALID_CBEFF("KER_BIE-006", "Invalid CBEFF"),
	;
	
	private final String errorCode;
	private final String errorMessage;

	private BiometricExtractionErrorConstants(String errorCode, String errorMessage) {
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
