-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_regdevice
-- Table Name 	: regdevice.registered_device_master
-- Purpose    	: Registered Device Master : Contains list of registered devices and details, like fingerprint scanner, iris scanner, scanner etc used at registration centers, authentication services, eKYC...etc. Valid devices with active status only allowed at registering devices for respective functionalities. Device onboarding are handled through admin application/portal by the user who is having the device onboarding authority. 
--           
-- Create By   	: Sadanandegowda
-- Created Date	: Aug-2019
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------

-- object: regdevice.registered_device_master | type: TABLE --
-- DROP TABLE IF EXISTS regdevice.registered_device_master CASCADE;
CREATE TABLE regdevice.registered_device_master(
	code character varying(36) NOT NULL,
	status_code character varying(64),
	device_id character varying(256) NOT NULL,
	device_sub_id character varying(256),
	digital_id character varying(1024) NOT NULL,
	serial_number character varying(64) NOT NULL,
	device_detail_id character varying(36) NOT NULL,
	purpose character varying(64) NOT NULL,
	firmware character varying(128),
	expiry_date timestamp,
	certification_level character varying(3),
	foundational_trust_provider_id character varying(36),
	hotlisted boolean,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT pk_regdevm_code PRIMARY KEY (code)

);
-- ddl-end --
COMMENT ON TABLE regdevice.registered_device_master IS 'Registered Device Master : Contains list of registered devices and details, like fingerprint scanner, iris scanner, scanner etc used at registration centers, authentication services, eKYC...etc. Valid devices with active status only allowed at registering devices for respective functionalities. Device onboarding are handled through admin application/portal by the user who is having the device onboarding authority. ';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.code IS 'Registred Device Code : Unique ID generated / assigned for device which is registred in MOSIP system for the purpose';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.status_code IS 'Status Code : Status of the registered devices, The status code can be Registered, De-Registered or Retired/Revoked.';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.device_id IS 'Device ID: Device ID is the unigue id provided by device provider for each device';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.device_sub_id IS 'Device Sub ID: Sub ID of the devices, Each device can have an array of sub IDs.';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.digital_id IS 'Digital ID: Digital ID received as a Json value containing below values like Serial number of the device, make , model, type, provider details..etc';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.serial_number IS 'Serial Number : Serial number of the device, This will be the Unique ID of the device by the provider';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.device_detail_id IS 'Device Detail ID';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.purpose IS 'Purpose : Purpose of these devices in the MOSIP system. ex. Registrations, Authentication, eKYC...etc';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.firmware IS 'Firmware: Firmware used in devices';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.expiry_date IS 'Expiry Date: expiry date of the device';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.certification_level IS 'Certification Level: Certification level for the device, This can be L0 or L1 devices';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.foundational_trust_provider_id IS 'Foundational Trust Provider ID: Foundational trust provider ID, This will be soft referenced from regdevice.ftp_chip_detail.foundational_trust_provider_id. Required only for L1 devices.';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN regdevice.registered_device_master.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --