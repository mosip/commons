package io.mosip.kernel.masterdata.constant;

/**
 * Constants for device Details
 * 
 * @author Megha Tanga
 * @author Neha Sinha
 * @author Ramadurai Pandian
 * @since 1.0.0
 *
 */
public enum DeviceErrorCode {
	DEVICE_FETCH_EXCEPTION("KER-MSD-009", "Error occured while fetching Devices"),
	DEVICE_NOT_FOUND_EXCEPTION("KER-MSD-010", "Device not Found"),
	DEVICE_NOT_EXISTS_EXCEPTION("KER-MSD-042", "Device ID %s does not exist"),
	DEVICE_INSERT_EXCEPTION("KER-MSD-069", "Error occurred while inserting Device details"),
	DEVICE_UPDATE_EXCEPTION("KER-MSD-083", "Error while updating"),
	DEVICE_DELETE_EXCEPTION("KER-MSD-084", "Error while deleting"),
	DEPENDENCY_EXCEPTION("KER-MSD-147", "Cannot delete as dependency found"),
	MAPPED_DEVICE_ID_NOT_FOUND_EXCEPTION("KER-MSD-332", "No Device id mapped found"),
	DEVICE_ID_ALREADY_MAPPED_EXCEPTION("KER-MSD-333", "All Device Id are mapped"),
	INVALID_DEVICE_FILTER_VALUE_EXCEPTION("KER-MSD-334", "Invalid filter value"),
	DEVICE_ID_NOT_FOUND_FOR_NAME_EXCEPTION("KER-MSD-335", "No Device Id found for name:%s"),
	DEVICE_SPECIFICATION_ID_NOT_FOUND_FOR_NAME_EXCEPTION("KER-MSD-336", "No Device specification id found for name:%s"),
	DEVICE_NOT_TAGGED_TO_ZONE("KER-MSD-344", "No zone assigned to the user"),
	ZONE_NOT_EXIST("KER-MSD-345", "Zone %s doesn't exist"),
	REGISTRATION_CENTER_DEVICE_FETCH_EXCEPTION("KER-MSD-440",
			"Error occurred while fetching a Device details mapped with the given Registration Center"),
	DEVICE_REGISTRATION_CENTER_NOT_FOUND_EXCEPTION("KER-MSD-441", "Registration Center and Device Not Found"),
	DEVICE_ZONE_NOT_FOUND_EXCEPTION("KER-MSD-395", "User is not authorized to decommission the Device "),
	MAPPED_TO_REGCENTER("KER-MSD-438", "Device cannot be Decommissioned as it is assigned to a Registration Center"),
	DEVICE_ERROR("KER-MSD-XXX", "Device Error"),
	INVALID_DEVICE_ZONE("KER-MSD-439", "Admin not authorized to access this Device for this Zone"),
	DECOMMISSIONED("KER-MSD-DDD", "Device has been already decommissioned or Device not found");

	private final String errorCode;
	private final String errorMessage;

	private DeviceErrorCode(final String errorCode, final String errorMessage) {
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
