package io.mosip.kernel.core.util.constant;

public enum ZipUtilConstants {
	FILE_NOT_FOUND_ERROR_CODE("KER-UTL-401", "File Not Found"),
	IO_ERROR_CODE("KER-UTL-402", "Interrupted IO Operation"),
	NULL_POINTER_ERROR_CODE("KER-UTL-403", "Null Reference found"),
	DATA_FORMATE_ERROR_CODE("KER-UTL-404", "Attempting to unzip file that is not zipped"),
	ARCHIVER_ERROR_CODE("KER-UTL-405","unzip location is incorrect"),
	THRESHOLD_RATIO_EXCEPTION("KER-UTL-406","compression ratio is more than the threshold"),
	THRESHOLD_SIZE_EXCEPTION("KER-UTL-407","Archive size read in the packet is more than the threshold"),
	THRESHOLD_ENTRIES_EXCEPTION("KER-UTL-408","Number of entries in the packet is more than the threshold"),
	PATH_TRAVERSAL_EXCEPTION("KER-UTL-409","path traversal vulnerability detected, provide proper path");

	/** Error code. */
	public final String errorCode;

	/** Exception Message */
	public final String message;

	/**
	 * @param errorCode        source Error code to use when no localized code is
	 *                         available
	 * @param exceptionMessage source exception message to use when no localized
	 *                         message is available.
	 */
	ZipUtilConstants(final String errorCode, final String message) {
		this.errorCode = errorCode;
		this.message = message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getMessage() {
		return message;
	}

}
