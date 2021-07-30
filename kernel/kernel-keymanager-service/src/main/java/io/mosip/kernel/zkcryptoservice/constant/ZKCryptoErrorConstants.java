package io.mosip.kernel.zkcryptoservice.constant;

/**
 * This ENUM provides all the constant identified for ZKCryptoManager errors.
 * 
 * @author Mahammed Taheer
 * @version 1.2.0-rc1-SNAPSHOT
 *
 */
public enum ZKCryptoErrorConstants {

    RANDOM_KEY_CIPHER_FAILED("KER-ZKC-001", "Failed to Encrypt/Decrypt Random Key."),
    
    NO_UNIQUE_ALIAS("KER-ZKC-002", "No unique alias is found."),

    EMPTY_DATA_ERROR("KER-ZKC-003", "Data attributes Empty."),

    DATA_CIPHER_OPS_ERROR("KER-ZKC-004", "Data Encryption/Decryption Error."),

	KEY_DERIVATION_ERROR("KER-ZKC-005", "Error Key derivation."),

	INVALID_ENCRYPTED_RANDOM_KEY("KER-ZKC-006", "Invalid encrypted random Key.");

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
	private ZKCryptoErrorConstants(String errorCode, String errorMessage) {
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
