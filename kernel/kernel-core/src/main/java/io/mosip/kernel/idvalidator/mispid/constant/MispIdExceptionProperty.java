package io.mosip.kernel.idvalidator.mispid.constant;

/**
 * This enum contains all exception properties that are required to validate
 * MISPID.
 * 
 * @author Ritesh Sinha
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
public enum MispIdExceptionProperty {
	/** MISPID length does not match {@code mosip.kernel.mispid.length}. */
	INVALID_MISPID_LENGTH("KER-IDV-401", "Mispid Length Must Be "),
	/** MISPID is {@code null} or empty. */
	INVALID_MISPID("KER-IDV-402", "Mispid cannot be null or empty");

	/**
	 * the errorCode.
	 */
	private String errorCode;
	/**
	 * the errorMessage.
	 */
	private String errorMessage;

	/**
	 * Constructor of RidExceptionProperty.
	 * 
	 * @param errorCode    the errorCode.
	 * @param errorMessage the errorMessage.
	 */
	MispIdExceptionProperty(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for errorCode
	 * 
	 * @return the errorCode.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for errorMessage
	 * 
	 * @return the errorMessage.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
