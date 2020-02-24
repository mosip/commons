package io.mosip.kernel.masterdata.constant;

public enum ExceptionalHolidayErrorCode {

	EXCEPTIONAL_HOLIDAY_FETCH_EXCEPTION("KER-EHD-001", "Error occured while fetching Exceptional Holidays"),
	EXCEPTIONAL_HOLIDAY_NOTFOUND("KER-EHD-002", "Exceptional Holiday not found"),
	REGISTRATION_CENTER_NOT_FOUND("KER-MSD-802", "Center ID is invalid"),
	INVALIDE_LANGCODE("KER-MSD-999","Language Code is invalid");
	
	private final String errorCode;
	private final String errorMessage;

	private ExceptionalHolidayErrorCode(final String errorCode, final String errorMessage) {
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
