-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_regdevice
-- Table Name 	: regdevice.secure_biometric_interface_h
-- Purpose    	: MOSIP Secure Biometric Interface History : History of changes of any MOSIP secure biometric interface will be stored in history table to track any chnages for future validations.
--           
-- Create By   	: Sadanandegowda
-- Created Date	: Aug-2019
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted not null changes
-- Oct-2021		Ram Bhatt	    Max column length for device_detail_id
-- ------------------------------------------------------------------------------------------

-- object: regdevice.secure_biometric_interface_h | type: TABLE --
-- DROP TABLE IF EXISTS regdevice.secure_biometric_interface_h CASCADE;
CREATE TABLE regdevice.secure_biometric_interface_h(
	id character varying(36) NOT NULL,
	sw_binary_hash bytea NOT NULL,
	sw_version character varying(64) NOT NULL,
	device_detail_id character varying NOT NULL,
	sw_cr_dtimes timestamp,
	sw_expiry_dtimes timestamp,
	approval_status character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	eff_dtimes timestamp NOT NULL,
	CONSTRAINT pk_mdsh_id PRIMARY KEY (id,eff_dtimes)

);
-- ddl-end --
COMMENT ON TABLE regdevice.secure_biometric_interface_h IS 'MOSIP Secure Biometric Interface History : History of changes of any MOSIP secure biometric interface will be stored in history table to track any chnages for future validations.';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.id IS 'ID: Unigue service ID, Service ID is geerated by the MOSIP system';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.sw_binary_hash IS 'Software Binary Hash : Its is a software binary stored in MOSIP system for the devices';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.sw_version IS 'Software Version : Version of the stored software';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.device_detail_id IS 'Device Detail ID';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.sw_cr_dtimes IS 'Software Created Date Time: Date and Time on which this software is created';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.sw_expiry_dtimes IS 'Software Expiry Date Time: Expiry date and time of the device software';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.approval_status IS 'Approval Status';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
COMMENT ON COLUMN regdevice.secure_biometric_interface_h.eff_dtimes IS 'Effective Date Timestamp : This to track master record whenever there is an INSERT/UPDATE/DELETE ( soft delete ).  The current record is effective from this date-time.';
-- ddl-end --
