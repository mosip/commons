package io.mosip.kernel.masterdata.constant;

/**
 * Constants for Request input related errors.
 * 
 * @author Bal Vikash Sharma
 * @since 1.0.0
 *
 */
public enum RequestErrorCode {

	REQUEST_DATA_NOT_VALID("KER-MSD-999", "Invalid request input"),
	REQUEST_INVALID_COLUMN("KER-MSD-319", "Invalid request input"),
	REQUEST_INVALID_SEC_LANG_ID("KER-MSD-999", "Invalid id passed for Secondary language"),
	INTERNAL_SERVER_ERROR("KER-MSD-500", "Internal server error"),
	ALREADY_ACTIVE_OR_INACTIVE("KER-MSD-998", "Already activated or deactivated"),
	REQUEST_INVALID_PRI_LANG_ID("KER-MSD-997", "Invalid id passed for Primary language"),
	REQUEST_ID_ALREADY_EXIST("KER-MSD-996", "Id already exists"),
	REQUEST_INVALID_SEC_LANG("KER-MSD-999",
			"Cannot create data in secondary language as data does not exist in primary language"),
	REQUEST_CODE_ALREADY_EXIST("KER-MSD-994", "Code already exists");

	private final String errorCode;
	private final String errorMessage;

	private RequestErrorCode(final String errorCode, final String errorMessage) {
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
