package io.mosip.kernel.core.crypto.constant;

/**
 * MOSIP error codes and messages for kernel cryptographic operations.
 * <p>
 * Contract: use {@link #getErrorCode()} and {@link #getErrorMessage()} when
 * constructing {@code io.mosip.kernel.core.crypto.exception} types. Message
 * text is English and may contain multi-line guidance.
 * </p>
 *
 * @see io.mosip.kernel.core.crypto.spi.CryptoCoreSpec
 */
public enum CryptoExceptionCodeConstants {
	/**
	 * Cryptographic key fails validation.
	 */
	INVALID_KEY_EXCEPTION("KER-FSE-001", "key is not valid"),
	/**
	 * Key length is not supported by the algorithm.
	 */
	INVALID_KEY_SIZE_EXCEPTION("KER-FSE-002", "key size is not valid"),
	/**
	 * Data length or RSA encoding parameters are invalid.
	 */
	INVALID_LENGTH_EXCEPTION("KER-FSE-003",
			"invalid parameters \n 1.if using plain rsa data is invalid \n  2.if using hybrid rsa use larger key with this encoding"),
	/**
	 * Input data length is outside the allowed range.
	 */
	INVALID_DATA_LENGTH_EXCEPTION("KER-FSE-013", "check input data length"),
	/**
	 * Key material is corrupted or unreadable.
	 */
	INVALID_KEY_CORRUPT_EXCEPTION("KER-FSE-004", "key is corrupted"),
	/**
	 * A public key was supplied where a private key is required.
	 */
	INVALID_ASYMMETRIC_PRIVATE_KEY_EXCEPTION("KER-FSE-005", "use private key instead of public"),
	/**
	 * A private key was supplied where a public key is required.
	 */
	INVALID_ASYMMETRIC_PUBLIC_KEY_EXCEPTION("KER-FSE-006", "use public key instead of private"),
	/**
	 * Plaintext or ciphertext fails validation.
	 */
	INVALID_DATA_EXCEPTION("KER-FSE-007", "data not valid"),
	/**
	 * Encrypted payload is corrupted.
	 */
	INVALID_ENCRYPTED_DATA_CORRUPT_EXCEPTION("KER-FSE-008", "encrypted data is corrupted"),
	/**
	 * Encrypted payload size is invalid.
	 */
	INVALID_DATA_SIZE_EXCEPTION("KER-FSE-009", "ecrypted data size is not valid"),
	/**
	 * Data argument is null.
	 */
	NULL_DATA_EXCEPTION("KER-FSE-010", "data is null"),
	/**
	 * MOSIP security method argument is null.
	 */
	NULL_METHOD_EXCEPTION("KER-FSE-014", "mosip security method is null"),
	/**
	 * Requested cryptographic algorithm is unavailable.
	 */
	NO_SUCH_ALGORITHM_EXCEPTION("KER-FSE-011", "no such algorithm"),
	/**
	 * Key argument is null.
	 */
	NULL_KEY_EXCEPTION("KER-FSE-012", "key is null");

	/**
	 * MOSIP error code such as {@code KER-FSE-001}.
	 */
	private final String errorCode;

	/**
	 * Human-readable error message.
	 */
	private final String errorMessage;

	/**
	 * Binds the constant to its error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null default message
	 */
	private CryptoExceptionCodeConstants(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns the MOSIP error code.
	 *
	 * @return never-null error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the human-readable error message.
	 *
	 * @return never-null message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
