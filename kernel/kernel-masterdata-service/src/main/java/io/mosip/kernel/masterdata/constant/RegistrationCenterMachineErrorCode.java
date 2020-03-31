package io.mosip.kernel.masterdata.constant;

/**
 * Constants for RegistrationCenterMachine related errors.
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @since 1.0.0
 *
 */
public enum RegistrationCenterMachineErrorCode {

	REGISTRATION_CENTER_MACHINE_CREATE_EXCEPTION("KER-MSD-074",
			"Error occurred while inserting a mapping of Machine and Center"),
	REGISTRATION_CENTER_MACHINE_DATA_NOT_FOUND("KER-MSD-114", "Mapping does not exist"),
	REGISTRATION_CENTER_MACHINE_DELETE_EXCEPTION("KER-MSD-106",
			"Error occurred while deleting a mapping of Machine and Center"),
	REGISTRATION_CENTER_MACHINE_FETCH_EXCEPTION("KER-MSD-601", "Error occurred while fetching Center Machine details"),
	REGISTRATION_CENTER_ZONE_INVALID("KER-MSD-411",
			"Admin not authorized to map/un-map this Registration Center"),
	REGISTRATION_MACHINE_ZONE_INVALID("KER-MSD-412",
			"Admin not authorized to map/un-map this Machine"),
	REGISTRATION_CENTER_MACHINE_STATUS("KER-MSD-602", "Already is in inactive status"),
	REGISTRATION_CENTER_NOT_FOUND("KER-MSD-409", "Registration Center not found"),
	REGISTRATION_CENTER_MACHINE_DECOMMISIONED_STATE("KER-MSD-421",
			"Registration center mapped to machine is decommisioned"),
	REGISTRATION_CENTER_MACHINE_ALREADY_ACTIVE("KER-MSD-600", "Registration center already mapped to machine"),
	REGISTRATION_CENTER_MACHINE_NOT_IN_SAME_HIERARCHY("KER-MSD-420",
			"Registration center and machine is not in same hierarchy");

	private final String errorCode;
	private final String errorMessage;

	private RegistrationCenterMachineErrorCode(final String errorCode, final String errorMessage) {
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
