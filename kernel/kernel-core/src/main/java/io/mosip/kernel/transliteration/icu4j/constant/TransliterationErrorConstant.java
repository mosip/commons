package io.mosip.kernel.transliteration.icu4j.constant;

/**
 * Error codes and messages for ICU4J transliteration.
 *
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public enum TransliterationErrorConstant {

	/** ICU has no transliterator for the composed language id. */
	TRANSLITERATION_INVALID_ID("KER-TRL-001", "Transliteration not possible"),
	/** Requested language code is not supported. */
	TRANSLITERATION_INVALID_LANGUAGE_CODE("KER-TRL-002", "Language code not supported");

	/**
	 * The error code.
	 */
	private String errorCode;

	/**
	 * The error message.
	 * 
	 */
	private String errorMessage;

	/**
	 * Constructor for TransliterationErrorConstant.
	 * 
	 * @param errorCode    the error code.
	 * @param errorMessage the error message.
	 */
	TransliterationErrorConstant(String errorCode, String errorMessage) {
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
