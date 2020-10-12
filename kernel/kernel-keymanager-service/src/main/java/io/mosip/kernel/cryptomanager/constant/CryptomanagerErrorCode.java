/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.cryptomanager.constant;

/**
 * Error Constants for Crypto-Manager-Service
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
public enum CryptomanagerErrorCode {
	/**
	 * 
	 */
	NO_SUCH_ALGORITHM_EXCEPTION("KER-CRY-001", "No Such algorithm is supported"),
	/**
	 * 
	 */
	INVALID_SPEC_PUBLIC_KEY("KER-CRY-002", "public key is invalid"),
	/**
	 * 
	 */
	INVALID_DATA_WITHOUT_KEY_BREAKER("KER-CRY-003", "data sent to decrypt is without key splitter or invalid"),
	/**
	 * 
	 */
	INVALID_DATA("KER-CRY-003", " or not base64 encoded"),
	/**
	 * 
	 */
	INVALID_REQUEST("KER-CRY-004", "should not be null or empty"),
	/**
	 * 
	 */
	CANNOT_CONNECT_TO_KEYMANAGER_SERVICE("KER-CRY-005", "cannot connect to keymanager service or response is null"),
	/**
	 * 
	 */
	KEYMANAGER_SERVICE_ERROR("KER-CRY-006", "Keymanager Service has replied with following error"),
	/**
	 * 
	 */
	RESPONSE_PARSE_ERROR("KER-CRY-008", "Error occur while parsing response "),
	/**
	 * 
	 */
	DATE_TIME_PARSE_EXCEPTION("KER-CRY-007", "timestamp should be in ISO 8601 format yyyy-MM-ddTHH::mm:ss.SZ"),
	/**
	 * 
	 */
	HEX_DATA_PARSE_EXCEPTION("KER-CRY-009", "Invalid Hex Data"),

	CERTIFICATE_THUMBPRINT_ERROR("KER-CRY-010", "Error in generating Certificate Thumbprint."),

	INTERNAL_SERVER_ERROR("KER-CRY-500", "Internal server error");



	/**
	 * The errorCode
	 */
	private final String errorCode;
	/**
	 * The errorMessage
	 */
	private final String errorMessage;

	/**
	 * {@link CryptomanagerErrorCode} constructor
	 * 
	 * @param errorCode    error code
	 * @param errorMessage error message
	 */
	private CryptomanagerErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for errorCode
	 * 
	 * @return errorCode
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for errorMessage
	 * 
	 * @return errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
