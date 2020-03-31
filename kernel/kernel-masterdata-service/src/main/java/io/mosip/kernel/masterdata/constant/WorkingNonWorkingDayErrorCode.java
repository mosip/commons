package io.mosip.kernel.masterdata.constant;

public enum WorkingNonWorkingDayErrorCode {

	REGISTRATION_CENTER_NOT_FOUND("KER-MSD-802", "Center ID is invalid"),
	WORKING_DAY_TABLE_NOT_ACCESSIBLE("KER-MSD-800",
			"Error occurred while fetching Registration Center Working day details"),
	WEEK_DAY_DATA_FOUND_EXCEPTION("KER-WKDS-002", "No week day found"),
	WORKING_DAY_DATA_FOUND_EXCEPTION("KER-WKDS-003", "No working/non working day data found");

	private final String errorCode;
	private final String errorMessage;

	private WorkingNonWorkingDayErrorCode(final String errorCode, final String errorMessage) {
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
