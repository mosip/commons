package io.mosip.kernel.partnercertservice.constant;

/**
 * This ENUM provides all the constant identified for PartnerCertManager errors.
 * 
 * @author Mahammed Taheer
 * @version 1.1.5.3
 *
 */
public enum PartnerCertManagerErrorConstants {

    INVALID_CERTIFICATE("KER-PCM-001", "Invalid Certificate uploaded."),
	
	CERTIFICATE_THUMBPRINT_ERROR("KER-PCM-002", "Error in generating Certificate Thumbprint."),

	CERTIFICATE_EXIST_ERROR("KER-PCM-003", "Certificate already exists in store."),

	CERTIFICATE_DATES_NOT_VALID("KER-PCM-004", "Certificate Dates are not valid."),

	ROOT_CA_NOT_FOUND("KER-PCM-005", "Root CA Certificate not found."),

	ROOT_INTER_CA_NOT_FOUND("KER-PCM-006", "Root CA/Intermediate CA Certificates not found."),

	INVALID_CERT_VERSION("KER-PCM-007", "Certificate version not supported."),

	PARTNER_ORG_NOT_MATCH("KER-PCM-008", "Partner Organization Name not Matched."),

	NO_UNIQUE_ALIAS("KER-PCM-009", "No Unique Alias found."),

	INVALID_CERTIFICATE_ID("KER-PCM-010", "Invalid Partner Certificate ID."),

	INVALID_PARTNER_DOMAIN("KER-PCM-011", "Invalid Partner Domain."),

	PARTNER_CERT_ID_NOT_FOUND("KER-PCM-012", "Partner Certificate not found for the given ID."),

	CERT_KEY_NOT_ALLOWED("KER-PCM-013", "Partner Certificate Key Size is less than allowed size."),

	CERT_SIGNATURE_ALGO_NOT_ALLOWED("KER-PCM-014", "Partner Certificate Signature algorithm not supported."),
    ;

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
	private PartnerCertManagerErrorConstants(String errorCode, String errorMessage) {
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
