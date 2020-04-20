package io.mosip.kernel.masterdata.constant;

/**
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

public enum LocationHierarchyErrorCode {
	LOCATION_HIERARCHY_FETCH_EXCEPTION("KER-MSD-399", "Error occured while fetching Location Hierarchy"),
	LOCATION_HIERARCHY_NOT_FOUND_EXCEPTION("KER-MSD-398", "Location Hierarchy not Found");

	private final String errorCode;
	private final String errorMessage;

	private LocationHierarchyErrorCode(final String errorCode, final String errorMessage) {
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


