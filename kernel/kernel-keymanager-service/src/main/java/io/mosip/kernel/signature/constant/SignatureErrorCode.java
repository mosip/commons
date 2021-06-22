package io.mosip.kernel.signature.constant;

/**
 * Constants for CryptoSignaure
 * 
 * @author Uday Kumarl
 * @since 1.0.0
 *
 */
public enum SignatureErrorCode {
	REQUEST_DATA_NOT_VALID("KER-CSS-999", "Invalid request input"), 
	NOT_VALID("KER-CSS-101", "Validation Unsuccessful"),
	
	INVALID_INPUT("KER-JWS-102", "Data to sign is not valid."),

	INVALID_JSON("KER-JWS-103", "Data to sign is not valid JSON."),

	SIGN_ERROR("KER-JWS-104", "Error - Unable to sign the data."),

	VERIFY_ERROR("KER-JWS-105", "Error - Unable to verify the data."),

	INVALID_VERIFY_INPUT("KER-JWS-106", "Signature data to verify not valid."),

	CERT_NOT_VALID("KER-JWS-107", "Signature verification certificate not valid."),

	INTERNAL_SERVER_ERROR("KER-CSS-102", "Internal server error");

	private final String errorCode;
	private final String errorMessage;

	private SignatureErrorCode(final String errorCode, final String errorMessage) {
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
