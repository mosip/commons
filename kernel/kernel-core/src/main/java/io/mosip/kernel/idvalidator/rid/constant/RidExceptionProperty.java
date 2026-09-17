package io.mosip.kernel.idvalidator.rid.constant;

/**
 * This enum contains all exception properties that are required to validate
 * RID.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
public enum RidExceptionProperty {
	/** RID is not numeric. */
	INVALID_RID("KER-IDV-301", "Rid Must Be Numeric Only"),
	/** Center-id prefix does not match the expected value. */
	INVALID_CENTER_ID("KER-IDV-302", "Center Id Did Not Match"),
	/** Machine-id prefix does not match the expected value. */
	INVALID_MACHINE_ID("KER-IDV-303", "Machine Id Did Not Match"),
	/** RID length does not match configuration. */
	INVALID_RID_LENGTH("KER-IDV-304", "Rid Length Must Be "),
	/** Timestamp segment does not match {@link RidPropertyConstant#TIME_STAMP_REGEX}. */
	INVALID_RID_TIMESTAMP("KER-IDV-305", "Invalid Time Stamp Found"),
	/** Sequence segment length is wrong. */
	INVALID_RID_SEQ_LENGTH("KER-IDV-307", "Invalid sequence Found"),
	/** A configured length is less than or equal to zero. */
	INVALID_RIDLENGTH_OR_CENTERIDLENGTH_MACHINEIDLENGTH_TIMESTAMPLENGTH("KER-IDV-306",
			"Rid length or center id length or machine id length or sequence length or timestamp length should not be less than or equals to zero");

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
	RidExceptionProperty(String errorCode, String errorMessage) {
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
