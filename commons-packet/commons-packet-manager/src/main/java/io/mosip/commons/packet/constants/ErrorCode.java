package io.mosip.commons.packet.constants;

public enum ErrorCode {
	
	
	JSON_PARSE_ERROR("COM-CPM-001", "Failed to parse json : "),
	SYSTEM_IO_ERROR("COM-CPM-002", "IO exception : ");
	
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
