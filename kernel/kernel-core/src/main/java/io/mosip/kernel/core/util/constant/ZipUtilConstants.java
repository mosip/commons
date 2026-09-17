package io.mosip.kernel.core.util.constant;

/**
 * MOSIP error codes and messages for {@link io.mosip.kernel.core.util.ZipUtils}.
 * <p>
 * Contract: used for zip I/O, zip-bomb thresholds, and path-traversal
 * failures. Public fields duplicate the getters for historical callers.
 * </p>
 */
public enum ZipUtilConstants {
	/**
	 * Zip file or entry was not found.
	 */
	FILE_NOT_FOUND_ERROR_CODE("KER-UTL-401", "File Not Found"),
	/**
	 * Interrupted or failed zip I/O.
	 */
	IO_ERROR_CODE("KER-UTL-402", "Interrupted IO Operation"),
	/**
	 * Null path or stream argument.
	 */
	NULL_POINTER_ERROR_CODE("KER-UTL-403", "Null Reference found"),
	/**
	 * Bytes are not a valid zip archive.
	 */
	DATA_FORMATE_ERROR_CODE("KER-UTL-404", "Attempting to unzip file that is not zipped"),
	/**
	 * Unzip destination path is invalid.
	 */
	ARCHIVER_ERROR_CODE("KER-UTL-405","unzip location is incorrect"),
	/**
	 * Compression ratio exceeds the zip-bomb threshold.
	 */
	THRESHOLD_RATIO_EXCEPTION("KER-UTL-406","compression ratio is more than the threshold"),
	/**
	 * Uncompressed size exceeds the zip-bomb threshold.
	 */
	THRESHOLD_SIZE_EXCEPTION("KER-UTL-407","Archive size read in the packet is more than the threshold"),
	/**
	 * Entry count exceeds the zip-bomb threshold.
	 */
	THRESHOLD_ENTRIES_EXCEPTION("KER-UTL-408","Number of entries in the packet is more than the threshold"),
	/**
	 * Zip entry path would escape the destination directory.
	 */
	PATH_TRAVERSAL_EXCEPTION("KER-UTL-409","path traversal vulnerability detected, provide proper path");

	/**
	 * MOSIP error code such as {@code KER-UTL-401}.
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
	ZipUtilConstants(final String errorCode, final String message) {
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
