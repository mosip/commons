
package io.mosip.kernel.core.applicanttype.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

/**
 * Checked exception thrown when applicant-type resolution receives a null,
 * empty, or otherwise invalid attribute map.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.applicanttype.spi.ApplicantType}
 * when the caller omits required demographic keys or supplies null values.
 * Callers should treat this as a client-input error, not a system failure.
 * </p>
 *
 * @see io.mosip.kernel.core.exception.BaseCheckedException
 * @see io.mosip.kernel.core.applicanttype.spi.ApplicantType
 *
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
public class InvalidApplicantArgumentException extends BaseCheckedException {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 2785372588639412708L;

	/**
	 * Constructor to initialize handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public InvalidApplicantArgumentException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    the specified cause
	 */
	public InvalidApplicantArgumentException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}
