package io.mosip.kernel.keymanagerservice.constant;

/**
 * This ENUM provides all the constant identified for Keymanager Service errors.
 * 
 * @author Dharmesh Khandelwal
 * @version 1.0.0
 *
 */
public enum KeymanagerErrorConstant {

	VALIDITY_CHECK_FAIL("KER-KMS-001", "Certificate is not valid"),

	APPLICATIONID_NOT_VALID("KER-KMS-002", "ApplicationId not found in Key Policy"),

	NO_UNIQUE_ALIAS("KER-KMS-003", "No unique alias is found"),

	NO_SUCH_ALGORITHM_EXCEPTION("KER-KMS-004", "No Such algorithm is supported"),

	INVALID_REQUEST("KER-KMS-005", "Invalid request"),

	DATE_TIME_PARSE_EXCEPTION("KER-KMS-006", "timestamp should be in ISO 8601 format yyyy-MM-ddTHH::mm:ss.SSSZ"),

	CRYPTO_EXCEPTION("KER-KMS-007", "Exception occured in cypto library: "),

	KEY_STORE_EXCEPTION("KER-KMS-008", "Service is  not able to store sign certificate"),

	INVALID_RESPONSE_TYPE("KER-KMS-009", "Invalid Response Object Type."),

	REFERENCE_ID_NOT_SUPPORTED("KER-KMS-010", "Reference Id Not Supported for the Application ID."),

	ROOT_KEY_NOT_FOUND("KER-KMS-011", "Root Key not available to sign."),

	KEY_GENERATION_NOT_DONE("KER-KMS-012", "Key Generation Process is not completed."),

	CERTIFICATE_PARSING_ERROR("KER-KMS-013", "Certificate Parsing Error."),

	KEY_NOT_MATCHING("KER-KMS-014", "Certificate Key Not Matching with stored Key."),

	UPLOAD_NOT_ALLOWED("KER-KMS-015", "Upload of certificate will not be allowed to update other domain certificate."),

	GENERATION_NOT_ALLOWED("KER-KMS-016", "Not allowed to generate new key pair for other domains."),

	CERTIFICATE_NOT_FOUND("KER-KMS-017", "Certificate Not found in keystore table."),

	DECRYPTION_NOT_ALLOWED("KER-KMS-018", "Not Allowed to perform decryption with other domain key."),

	SYMMETRIC_KEY_DECRYPTION_FAILED("KER-KMS-019", "Not able to decrypt Symmetric Key using the Private Key."),

	NOT_VALID_SIGNATURE_KEY("KER-KMS-020", "Signing operation for allowed for the provided application id & reference id."),

	REVOKE_NOT_ALLOWED("KER-KMS-021", "Key Revocation not allowed."),

	INTERNAL_SERVER_ERROR("KER-KMS-500", "Internal server error");

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
	private KeymanagerErrorConstant(String errorCode, String errorMessage) {
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
