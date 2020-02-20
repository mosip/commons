package io.mosip.kernel.masterdata.constant;

/**
 * Constants for RegistrationCenterDevice related errors.
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @since 1.0.0
 *
 */
public enum RegistrationCenterDeviceErrorCode {

	REGISTRATION_CENTER_DEVICE_CREATE_EXCEPTION("KER-MSD-075",
			"Error occurred while inserting a mapping of Device and Center"),
	REGISTRATION_CENTER_DEVICE_DATA_NOT_FOUND("KER-MSD-115", "Mapping for Device and Center not found"),
	REGISTRATION_CENTER_DEVICE_DELETE_EXCEPTION("KER-MSD-105",
			"Error occurred while deleting a mapping of Device and Center"),
	DEVICE_REGISTRATION_CENTER_NOT_FOUND_EXCEPTION("KER-MSD-441", "Registration Center and Device Not Found"),
	REGISTRATION_CENTER_NOT_FOUND_EXCEPTION("KER-MSD-409", "Registration Center not found"),
	REGISTRATION_CENTER_DEVICE_FETCH_EXCEPTION("KER-MSD-433", "Error occurred while fetching Center Device details"),
	REGISTRATION_CENTER_DEVICE_ALREADY_UNMAPPED_EXCEPTION("KER-MSD-434",
			"Device-Registration Center un-mapping already exist"),
	DEVICE_AND_REG_CENTER_MAPPING_NOT_FOUND_EXCEPTION("KER-MSD-435",
			"Device Id %s - Center Id %s mapping does not exist"),
	INVALIDE_DEVICE_ZONE("KER-MSD-415", "Admin not authorized to map/un-map this Device"),
	INVALIDE_CENTER_ZONE("KER-MSD-411", "Admin not authorized to map/un-map this Registration Center"),
	REGISTRATION_CENTER_DEVICE_MAPPING_EXCEPTION("KER-MSD-436",
			"Error occurred while mapping Device to Registration Center"),
	REGISTRATION_CENTER_DEVICE_NOT_IN_SAME_HIERARCHY("KER-MSD-416",
			"Device cannot be mapped to the Center as Center and Device does not belong to the same Administrative Zone"),
	REGISTRATION_CENTER_ALREADY_MAPPED("KER-MSD-419",
			"Cannot map the Device as it is mapped to another Registration Center"),
	REGISTATION_CENTER_DEVICE_DECOMMISIONED_STATE("KER-MSD-418",
			"Cannot map as the Registration Center/Device is Decommissioned"),
	DEVICE_NOT_FOUND_EXCEPTION("KER-MSD-417", "Device not Found");

	private final String errorCode;
	private final String errorMessage;

	private RegistrationCenterDeviceErrorCode(final String errorCode, final String errorMessage) {
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
