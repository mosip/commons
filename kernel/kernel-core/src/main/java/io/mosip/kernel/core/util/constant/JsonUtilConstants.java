package io.mosip.kernel.core.util.constant;

/**
 * MOSIP error codes and messages for {@link io.mosip.kernel.core.util.JsonUtils}.
 * <p>
 * Contract: use {@link #getErrorCode()} and {@link #getErrorMessage()} when
 * wrapping Jackson parse, mapping, or generation failures.
 * </p>
 *
 * @author Sidhant Agarwal
 */
public enum JsonUtilConstants {

	/**
	 * JSON source file was not found.
	 */
	MOSIP_IO_EXCEPTION_ERROR_CODE("KER-UTL-101", "File not found"),
	/**
	 * JSON could not be generated from the object graph.
	 */
	MOSIP_JSON_GENERATION_ERROR_CODE("KER-UTL-102", "Json not generated successfully"),
	/**
	 * JSON could not be mapped onto the target type.
	 */
	MOSIP_JSON_MAPPING_ERROR_CODE("KER-UTL-103", "Json mapping Exception"),
	/**
	 * JSON text is syntactically invalid.
	 */
	MOSIP_JSON_PARSE_ERROR_CODE("KER-UTL-104", "Json not parsed successfully"),
	/**
	 * JSON processing failed for another reason.
	 */
	MOSIP_JSON_PROCESSING_EXCEPTION("KER-UTL-105", "json not processed successfully");

	/**
	 * MOSIP error code such as {@code KER-UTL-101}.
	 */
	public final String errorCode;
	/**
	 * Human-readable error message.
	 */
	public final String errorMessage;

	/**
	 * Binds the constant to its error code and message.
	 *
	 * @param string1 never-null error code
	 * @param string2 never-null error message
	 */
	JsonUtilConstants(String string1, String string2) {
		this.errorCode = string1;
		this.errorMessage = string2;
	}

	/**
	 * Returns the MOSIP error code.
	 *
	 * @return never-null error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the human-readable error message.
	 *
	 * @return never-null message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}
