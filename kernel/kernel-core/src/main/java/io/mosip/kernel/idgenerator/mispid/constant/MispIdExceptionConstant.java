package io.mosip.kernel.idgenerator.mispid.constant;

/**
 * Exception constants for MISPID generator.
 * 
 * @author Ritesh Sinha
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
public enum MispIdExceptionConstant {

	/** Fetch of the last MISPID failed. */
	MISPID_FETCH_EXCEPTION("KER-MSP-001", "Error Occur While Fetching Id"),

	/** Insert of the next MISPID failed. */
	MISPID_INSERTION_EXCEPTION("KER-MSP-002", "Error Occur While Inserting Id");

	/**
	 * The error code.
	 */
	private String errorCode;

	/**
	 * The error message.
	 */
	private String errorMessage;

	/**
	 * Constructor for MispIdExceptionConstant.
	 * 
	 * @param errorCode    the errorCode.
	 * @param errorMessage the errorMessage.
	 */
	MispIdExceptionConstant(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for error code.
	 * 
	 * @return the error code.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for error message.
	 * 
	 * @return the error message.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
