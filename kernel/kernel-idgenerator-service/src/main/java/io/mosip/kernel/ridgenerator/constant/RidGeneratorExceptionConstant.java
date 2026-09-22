package io.mosip.kernel.ridgenerator.constant;

/**
 * This enum provide all the exception constants for rid generator.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
public enum RidGeneratorExceptionConstant {
	/**
	 * Center id or machine id was empty.
	 */
	EMPTY_INPUT_ERROR_CODE("KER-RIG-002", "Empty input entered"),
	/**
	 * Center id or machine id length does not match configured length.
	 */
	INPUT_LENGTH_ERROR_CODE("KER-RIG-003", "input length is not valid"),
	/**
	 * Configured timestamp length is not greater than zero.
	 */
	INVALID_CENTERID_OR_MACHINEID_TIMESTAMP_LENGTH("KER-RIG-004", "Timestamp length should be greater than zero"),
	/**
	 * Failure while reading the RID sequence row.
	 */
	RID_FETCH_EXCEPTION("KER-RIG-005", "Error occured while fetching rid"),
	/**
	 * Failure while inserting or updating the RID sequence row.
	 */
	RID_UPDATE_EXCEPTION("KER-RIG-006", "Error occured while storing rid"),
	/**
	 * Configured sequence length is not greater than zero.
	 */
	INVALID_SEQ_LENGTH_EXCEPTION("KER-RIG-007", "Sequence length should be greater than zero"),
	/**
	 * HTTP request body could not be read.
	 */
	HTTP_MESSAGE_NOT_READABLE("KER-RIG-999", ""),
	/**
	 * Unhandled runtime failure.
	 */
	RUNTIME_EXCEPTION("KER-RIG-500", "");

	/**
	 * The errorCode.
	 */
	public final String errorCode;

	/**
	 * The errorMessage.
	 */
	public final String errorMessage;

	/**
	 * The constructor to set exception errorCode and errorMessage.
	 * 
	 * @param errorCode    The error code to be set.
	 * @param errorMessage The error message to be set.
	 */
	private RidGeneratorExceptionConstant(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for errorCode.
	 * 
	 * @return the error code.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for errorMessage.
	 * 
	 * @return the error message.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
