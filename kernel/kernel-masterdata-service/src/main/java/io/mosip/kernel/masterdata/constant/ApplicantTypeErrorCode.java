package io.mosip.kernel.masterdata.constant;

/**
 * Error code constants
 * 
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
public enum ApplicantTypeErrorCode {
	INVALID_REQUEST_EXCEPTION("KER-MSD-148", "One or more input parameter is invalid or does not exist"),
	NO_APPLICANT_FOUND_EXCEPTION("KER-MSD-147", "Applicant Type data does not exist"),
	APPLICANT_TYPE_FETCH_EXCEPTION("KER-MSD-149",
			"Error occurred while fetching Applicant Type-Document Category-Document Type Mapping details"),
	APPLICANT_TYPE_NOT_FOUND_EXCEPTION("KER-MSD-150", "Document Category- Document Type mapping not found");

	private final String errorCode;
	private final String errorMessage;

	private ApplicantTypeErrorCode(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}
}
