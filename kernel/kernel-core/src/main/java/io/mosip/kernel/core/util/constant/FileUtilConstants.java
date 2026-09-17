package io.mosip.kernel.core.util.constant;

/**
 * MOSIP error codes and messages for {@link io.mosip.kernel.core.util.FileUtils}.
 * <p>
 * Contract: use {@link #getErrorCode()} and {@link #getMessage()} when wrapping
 * file I/O failures. Public fields duplicate the getters for historical
 * callers.
 * </p>
 *
 * @author Priya Soni
 */
public enum FileUtilConstants {
	/**
	 * Destination file already exists.
	 */
	FILE_EXISTS_ERROR_CODE("KER-UTL-001", "File already exists"),
	/**
	 * Source or destination file was not found.
	 */
	FILE_NOT_FOUND_ERROR_CODE("KER-UTL-002", "File Not Found"),
	/**
	 * Illegal path or charset argument.
	 */
	ILLEGAL_ARGUMENT_ERROR_CODE("KER-UTL-003", "Illegal Argument passed"),
	/**
	 * Interrupted or failed file I/O.
	 */
	IO_ERROR_CODE("KER-UTL-004", "Interrupted IO Operation"),
	/**
	 * Null file or stream argument.
	 */
	NULL_POINTER_ERROR_CODE("KER-UTL-005", "Null Reference found"),
	/**
	 * Requested charset is not supported.
	 */
	UNSUPPORTED_CHARSET_ERROR_CODE("KER-UTL-006", "No support available for the requested charset"),
	/**
	 * Character encoding is not supported.
	 */
	UNSUPPORTED_ENCODING_ERROR_CODE("KER-UTL-007", "The Character Encoding is not supported");

	/**
	 * MOSIP error code such as {@code KER-UTL-001}.
	 */
	public final String errorCode;

	/**
	 * Human-readable exception message.
	 */
	public final String message;

	/**
	 * Binds the constant to its error code and message.
	 *
	 * @param errorCode never-null MOSIP error code
	 * @param message   never-null default message
	 */
	FileUtilConstants(final String errorCode, final String message) {
		this.errorCode = errorCode;
		this.message = message;
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
	 * Returns the human-readable message.
	 *
	 * @return never-null message
	 */
	public String getMessage() {
		return message;
	}

}
