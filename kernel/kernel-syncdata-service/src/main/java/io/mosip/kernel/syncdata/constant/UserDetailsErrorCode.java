package io.mosip.kernel.syncdata.constant;

public enum UserDetailsErrorCode {

	USER_DETAILS_FETCH_EXCEPTION("KER-SNC-301", "Error occured while fetching User Details"),
	USER_DETAILS_PARSE_ERROR("KER-SNC-302", "Error occured while parsing"),
	USER_SALT_FETCH_EXCEPTION("KER-SNC-303", "Error occured while fetching User Salt"),
	USER_SALT_NOT_FOUND_FOUND("KER-SNC-304", "Error occured while parsing");

	private final String errorCode;
	private final String errorMessage;

	private UserDetailsErrorCode(final String errorCode, final String errorMessage) {
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
