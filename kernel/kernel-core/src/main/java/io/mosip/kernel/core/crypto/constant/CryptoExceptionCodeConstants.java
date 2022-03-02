/*
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */

package io.mosip.kernel.core.crypto.constant;


public enum CryptoExceptionCodeConstants {
	INVALID_KEY_EXCEPTION("KER-FSE-001", "key is not valid"),
	INVALID_KEY_SIZE_EXCEPTION("KER-FSE-002", "key size is not valid"),
	INVALID_LENGTH_EXCEPTION("KER-FSE-003",
			"invalid parameters \n 1.if using plain rsa data is invalid \n  2.if using hybrid rsa use larger key with this encoding"),
	INVALID_DATA_LENGTH_EXCEPTION("KER-FSE-013", "check input data length"),
	INVALID_KEY_CORRUPT_EXCEPTION("KER-FSE-004", "key is corrupted"),
	INVALID_ASYMMETRIC_PRIVATE_KEY_EXCEPTION("KER-FSE-005", "use private key instead of public"),
	INVALID_ASYMMETRIC_PUBLIC_KEY_EXCEPTION("KER-FSE-006", "use public key instead of private"),
	INVALID_DATA_EXCEPTION("KER-FSE-007", "data not valid"),
	INVALID_ENCRYPTED_DATA_CORRUPT_EXCEPTION("KER-FSE-008", "encrypted data is corrupted"),
	INVALID_DATA_SIZE_EXCEPTION("KER-FSE-009", "ecrypted data size is not valid"),
	NULL_DATA_EXCEPTION("KER-FSE-010", "data is null"),
	NULL_METHOD_EXCEPTION("KER-FSE-014", "mosip security method is null"),
	NO_SUCH_ALGORITHM_EXCEPTION("KER-FSE-011", "no such algorithm"),
	NULL_KEY_EXCEPTION("KER-FSE-012", "key is null");

	/**
	 * Constant {@link Enum} errorCode
	 */
	private final String errorCode;

	/**
	 * Constant {@link Enum} errorMessage
	 */
	private final String errorMessage;

	/**
	 * Constructor for this class
	 * 
	 * @param value set {@link Enum} value
	 */
	private CryptoExceptionCodeConstants(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for errorCode
	 * 
	 * @return get errorCode value
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for errorMessage
	 * 
	 * @return get errorMessage value
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
