-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_regdevice
-- Release Version 	: 1.1.5
-- Purpose    		: Database Alter scripts for the release for Regdevice DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_regdevice sysadmin

ALTER TABLE regdevice.ftp_chip_detail ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE regdevice.foundational_trust_provider ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE regdevice.registered_device_master_h ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE regdevice.reg_device_type ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE regdevice.reg_device_sub_type ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE regdevice.device_detail ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE regdevice.secure_biometric_interface ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE regdevice.secure_biometric_interface_h ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE regdevice.registered_device_master ALTER COLUMN is_deleted SET NOT NULL;

ALTER TABLE regdevice.ftp_chip_detail ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE regdevice.foundational_trust_provider ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE regdevice.registered_device_master_h ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE regdevice.reg_device_type ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE regdevice.reg_device_sub_type ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE regdevice.device_detail ALTER COLUMN is_deleted SET  DEFAULT FALSE;
ALTER TABLE regdevice.secure_biometric_interface ALTER COLUMN is_deleted SET  DEFAULT FALSE;
ALTER TABLE regdevice.secure_biometric_interface_h ALTER COLUMN is_deleted SET  DEFAULT FALSE;
ALTER TABLE regdevice.registered_device_master ALTER COLUMN is_deleted SET  DEFAULT FALSE;

----------------------------------------------------------------------------------------------------
