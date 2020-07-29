package io.mosip.kernel.bioextractor.config.constant;

public enum BiometricExtractionErrorConstants {
	
	UNKNOWN_ERROR("KER_BIE_001", "Unknown Error")
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
