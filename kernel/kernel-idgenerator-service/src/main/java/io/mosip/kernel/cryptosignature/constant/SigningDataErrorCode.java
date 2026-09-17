package io.mosip.kernel.cryptosignature.constant;

/**
 * MOSIP error codes used when signing ID-generator HTTP responses through keymanager.
 *
 * @author Srinivasan
 * @author Urvil Joshi
 *
 */
public enum SigningDataErrorCode {

	/**
	 * Failure while parsing the keymanager sign API response body.
	 */
	RESPONSE_PARSE_EXCEPTION("KER-SGN-100", "Error occured while parsing data"),
	/**
	 * Failure while calling the keymanager sign REST API.
	 */
	REST_CRYPTO_CLIENT_EXCEPTION("KER-SGN-101", "Error occured while calling an Sign API"),
	/**
	 * Failure while fetching a public key from keymanager.
	 */
	REST_KM_CLIENT_EXCEPTION("KER-SGN-102", "Error occured while fetching Public Key"),
	/**
	 * Local public-key validation APIs are not supported in this service.
	 */
	REST_NOT_SUPPORTED_EXCEPTION("KER-SGN-500", "Method Not Supported Exception.");

	/**
	 * MOSIP error code.
	 */
	private final String errorCode;
	/**
	 * Human-readable error message.
	 */
	private final String errorMessage;

	/**
	 * Instantiates an error code with its message.
	 *
	 * @param errorCode    the MOSIP error code
	 * @param errorMessage the error message
	 */
	private SigningDataErrorCode(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns the MOSIP error code.
	 *
	 * @return the error code
	 */
	public String getErrorCode() {
		return this.errorCode;
	}

	/**
	 * Returns the error message.
	 *
	 * @return the error message
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}
}
