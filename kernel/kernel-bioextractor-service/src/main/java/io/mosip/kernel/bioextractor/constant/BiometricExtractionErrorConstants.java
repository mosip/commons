package io.mosip.kernel.bioextractor.constant;

public enum BiometricExtractionErrorConstants {
	
	UNKNOWN_ERROR("KER_BIE_001", "Unknown Error"),
	MISSING_INPUT_PARAMETER("KER_BIE-002", "Missing Input Parameter - %s"),
	INVALID_INPUT_PARAMETER("KER_BIE-003", "Invalid Input Parameter - %s"),
	DOWNLOAD_BIOMETRICS_ERROR("KER_BIE-004", "Error in downloading biometrics"),
	DECRYPTION_ERROR("KER_BIE-005", "Decryption error"),
	ENCRYPTION_ERROR("KER_BIE-006", "Encryption error"),
	INVALID_CBEFF("KER_BIE-007", "Invalid CBEFF"),
	TECHNICAL_ERROR("KER_BIE-008", "Technical Error in Biometric Extraction"),
	UPLOAD_BIOMETRICS_ERROR("KER_BIE-009", "Error in uploading extracted biometrics"),
	NOTIFY_IDREPO_ERROR("KER_BIE-009", "Error in notifying ID-Repository with Biometric Extraction Event"),

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
