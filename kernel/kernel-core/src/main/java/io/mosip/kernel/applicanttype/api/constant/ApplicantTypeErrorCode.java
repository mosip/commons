package io.mosip.kernel.applicanttype.api.constant;

/**
 * Error codes and messages for applicant-type evaluation.
 * <p>
 * Codes are returned by the MVEL script or thrown as
 * {@link io.mosip.kernel.core.applicanttype.exception.InvalidApplicantArgumentException}.
 * </p>
 *
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
public enum ApplicantTypeErrorCode {

	/** Query attributes are missing or not accepted by the applicant-type script. */
	INVALID_QUERY_EXCEPTION("KER-MSD-147", "Invalid query passed for applicant type"),
	/** Date of birth string could not be parsed. */
	INVALID_DATE_STRING_EXCEPTION("KER-MSD-148", "Date string can not be parsed"),
	/** Date of birth is later than the current date. */
	INVALID_DATE_DOB_EXCEED_EXCEPTION("KER-MSD-151", "DOB cannot exceed current date");
	
	/** MOSIP error code (for example {@code KER-MSD-147}). */
	private final String errorCode;
	/** Human-readable description paired with {@link #errorCode}. */
	private final String errorMessage;
	
	/**
	 * Creates an error constant.
	 *
	 * @param errorCode    MOSIP error code
	 * @param errorMessage human-readable description
	 */
	private ApplicantTypeErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Returns the MOSIP error code.
	 *
	 * @return error code such as {@code KER-MSD-147}
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the human-readable error description.
	 *
	 * @return error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}