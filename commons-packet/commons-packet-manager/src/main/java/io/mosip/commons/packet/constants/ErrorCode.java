package io.mosip.commons.packet.constants;

public enum ErrorCode {

	PACKET_ENCRYPT_ERROR("KER-KPM-001", "Failed to encrypt packet : "),
	JSON_PARSE_ERROR("KER-KPM-002", "Failed to parse json : "),
	ADD_ZIP_ENTRY_ERROR("KER-KPM-003", "Failed to add entry in zip : "),
	BIR_TO_XML_ERROR("KER-KPM-004", "Failed to generate XML from BIR : "),
	OBJECT_TO_JSON_ERROR("KER-KPM-005", "Failed to serialize object : "),
	PKT_ZIP_ERROR("KER-KPM-006", "Failed to create zip : "),
	INVALID_DATA_ERROR("KER-KPM-007", "Invalid input provided : "),
	AUDITS_REQUIRED("KER-KPM-008", "Registration audits is empty"),
	INITIALIZATION_ERROR("KER-KPM-009", "Packet creator not initialized"),
	PACKET_NOT_FOUND("KER-KPM-010", "Packet not present in packet store");

	ErrorCode(String errorCode, String errorMessage) {
		this.setErrorCode(errorCode);
		this.setErrorMessage(errorMessage);
	}

	private String errorCode;
	private String errorMessage;

	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
