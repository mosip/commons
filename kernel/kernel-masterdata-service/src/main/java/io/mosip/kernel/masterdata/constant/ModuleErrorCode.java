package io.mosip.kernel.masterdata.constant;

/**
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

public enum ModuleErrorCode {
	MODULE_FETCH_EXCEPTION("KER-MSD-399", "Error occured while fetching Module"),
	MODULE_NOT_FOUND_EXCEPTION("KER-MSD-398", "Module not Found");

	private final String errorCode;
	private final String errorMessage;

	private ModuleErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}

