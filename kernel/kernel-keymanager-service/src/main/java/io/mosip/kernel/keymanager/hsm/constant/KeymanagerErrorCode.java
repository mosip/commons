package io.mosip.kernel.keymanager.hsm.constant;

/**
 * Error Code for Softhsm Keystore
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public enum KeymanagerErrorCode {
	INVALID_CONFIG_FILE("KER-KMA-001", "Config file invalid"),

	NO_SUCH_SECURITY_PROVIDER("KER-KMA-002", "No such security provider"),

	KEYSTORE_PROCESSING_ERROR("KER-KMA-003", "Error occured in processing Keystore: "),

	NO_SUCH_ALIAS("KER-KMA-004", "No such alias: "),

	CERTIFICATE_PROCESSING_ERROR("KER-KMA-005", "Error occured while processing exception: "),

	NOT_VALID_PKCS11_STORE_TYPE("KER-KMA-007", "Keystore loaded as offline, Retrivial of Private is not allowed."),
	
	KEYSTORE_NOT_INSTANTIATED("KER-KMA-006", "Keystore not instantiated error.");

	/**
	 * The error code
	 */
	private final String errorCode;
	/**
	 * The error message
	 */
	private final String errorMessage;

	/**
	 * Constructor to set error code and message
	 * 
	 * @param errorCode    the error code
	 * @param errorMessage the error message
	 */
	private KeymanagerErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Function to get error code
	 * 
	 * @return {@link #errorCode}
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Function to get the error message
	 * 
	 * @return {@link #errorMessage}
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
