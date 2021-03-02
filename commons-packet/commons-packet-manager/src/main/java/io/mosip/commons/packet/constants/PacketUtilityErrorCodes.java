package io.mosip.commons.packet.constants;

public enum PacketUtilityErrorCodes {

	UNKNOWN_RESOURCE_EXCEPTION("KER-PUT-001",
			"Unknown resource provided"),
	FILE_NOT_FOUND_IN_DESTINATION("KER-PUT-002", "Unable to Find File in Destination Folder"),
	PACKET_DECRYPTION_FAILURE_EXCEPTION("KER-PUT-003", "Packet decryption failed"),
	API_NOT_ACCESSIBLE_EXCEPTION("KER-PUT-005", "API not accessible"),
	SYS_IO_EXCEPTION("KER-PUT-004", "Unable to Find File in Destination Folder"),
	GET_ALL_IDENTITY_EXCEPTION("KER-PUT-005", "Unable to fetch identity json from all sub packets"),
	NO_AVAILABLE_PROVIDER("KER-PUT-006", "No available provider for given source and process"),
	BIOMETRIC_FIELDNAME_NOT_FOUND("KER-PUT-007", "Biometric fieldname is not present inside packet"),
	GET_ALL_METAINFO_EXCEPTION("KER-PUT-008", "Unable to fetch meta information from all sub packets"),
	ZIP_PARSING_EXCEPTION("KER-PUT-009", "Unable to parse the zip"),
	SIGNATURE_EXCEPTION("KER-PUT-010", "Failed to generate digital signature"),
	DOCUMENT_EXCEPTION("KER-PUT-011", "Failed to get document"),
	BIOMETRIC_EXCEPTION("KER-PUT-012", "Unable to get biometric"),
	OS_ADAPTER_EXCEPTION("KER-PUT-013", "No Object store adapter found."),
	PACKET_KEEPER_GET_ERROR("KER-PUT-014", "Packet keeper exception occured."),
	PACKET_KEEPER_PUT_ERROR("KER-PUT-015", "Packet keeper exception occured."),
	PACKET_KEEPER_GETMETA_ERROR("KER-PUT-016", "Packet keeper exception occured."),
	PACKET_KEEPER_INTEGRITY_ERROR("KER-PUT-017", "Packet keeper exception occured."),
	CRYPTO_EXCEPTION("KER-PUT-018", "No available crypto service exception."),
	INTEGRITY_FAILURE("KER-PUT-019", "Packet Integrity check failed."),
	PACKET_VALIDATION_FAILED("KER-PUT-020", "Packet Validation exception occured."),
	TAGGING_FAILED("KER-PUT-021", "Failed to add tags."), 
	TAG_ALREADY_EXIST("KER-PUT-022", "Tag Already Exist."),
	GET_TAG_EXCEPTION("KER-PUT-023","Failed to get tags"),
	TAG_NOT_FOUND("KER-PUT-024", "Requested tag not present"),
	SOURCE_NOT_PRESENT("KER-PUT-025", "Source not present in request.");
	
	


	private final String errorCode;
	private final String errorMessage;

	private PacketUtilityErrorCodes(final String errorCode, final String errorMessage) {
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
