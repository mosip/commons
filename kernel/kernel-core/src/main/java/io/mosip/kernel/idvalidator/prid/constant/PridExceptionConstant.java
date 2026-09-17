package io.mosip.kernel.idvalidator.prid.constant;

/**
 * 
 * @author M1037462
 *
 *         since 1.0.0
 */
public enum PridExceptionConstant {

	/** PRID is {@code null} or empty. */
	PRID_VAL_INVALID_NULL("KER-IDV-101", "PRID should not be empty or null."),
	/** PRID contains a forbidden sequence or repeating block. */
	PRID_VAL_ILLEGAL_SEQUENCE_REPEATATIVE("KER-IDV-102",
			"PRID should not contain any sequential and repeated block of number for 2 or more than two digits"),
	/** PRID length does not match configuration. */
	PRID_VAL_ILLEGAL_LENGTH("KER-IDV-103", "PRID length should be as configured digit."),
	/** PRID contains a non-digit character. */
	PRID_VAL_INVALID_DIGITS("KER-IDV-104", "PRID should not contain any alphanumeric characters."),
	/** Checksum digit does not match. */
	PRID_VAL_ILLEGAL_CHECKSUM("KER-IDV-105", "PRID should match checksum."),
	/** PRID starts with {@code 0} or {@code 1}. */
	PRID_VAL_INVALID_ZERO_ONE("KER-IDV-106", "PRID should not contain Zero or One as first Digit."),
	/** A configured limit is less than or equal to zero. */
	PRID_VAL_INVALID_VALUE("KER-IDV-107",
			"Prid length or sequence limit or repeating limit or repeating block limit should not be less than or equals to zero");

	/**
	 * This variable holds the error code.
	 */
	private String errorCode;

	/**
	 * This variable holds the error message.
	 */
	private String errorMessage;

	/**
	 * Constructor for VIDErrorConstants Enum.
	 * 
	 * @param errorCode    the error code.
	 * @param errorMessage the error message.
	 */
	PridExceptionConstant(String errorCode, String errorMessage) {
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
