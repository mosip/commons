-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_authdevice
-- Table Name 	: authdevice.secure_biometric_interface
-- Purpose    	: Secure Biometric Interface : Secure Biometric Interface to have all the details about the device types, provider and software details
--           
-- Create By   	: Sadanandegowda
-- Created Date	: Aug-2019
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- 
-- ------------------------------------------------------------------------------------------

-- object: authdevice.secure_biometric_interface | type: TABLE --
-- DROP TABLE IF EXISTS authdevice.secure_biometric_interface CASCADE;
CREATE TABLE authdevice.secure_biometric_interface(
	id character varying(36) NOT NULL,
	sw_binary_hash bytea NOT NULL,
	sw_version character varying(64) NOT NULL,
	device_detail_id character varying(36) NOT NULL,
	sw_cr_dtimes timestamp,
	sw_expiry_dtimes timestamp,
	approval_status character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT pk_sbi_id PRIMARY KEY (id)

);
-- ddl-end --
COMMENT ON TABLE authdevice.secure_biometric_interface IS 'Secure Biometric Interface : Secure Biometric Interface to have all the details about the device types, provider and software details';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.id IS 'ID: Unigue service ID, Service ID is geerated by the MOSIP system';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.sw_binary_hash IS 'Software Binary Hash : Its is a software binary stored in MOSIP system for the devices';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.sw_version IS 'Software Version : Version of the stored software';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.device_detail_id IS 'Device Detail ID:';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.sw_cr_dtimes IS 'Software Created Date Time: Date and Time on which this software is created';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.sw_expiry_dtimes IS 'Software Expiry Date Time: Expiry date and time of the device software';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.approval_status IS 'Approval Status:';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN authdevice.secure_biometric_interface.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --