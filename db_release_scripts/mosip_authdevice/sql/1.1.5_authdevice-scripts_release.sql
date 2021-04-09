-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_authdevice
-- Release Version 	: 1.2.0-SNAPSHOT
-- Purpose    		: Database Alter scripts for the release for Authdevice DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
--
-- -------------------------------------------------------------------------------------------------

\c mosip_authdevice sysadmin

-- -------------------------------------------------------------------------------------------------

ALTER TABLE authdevice.ftp_chip_detail ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE authdevice.foundational_trust_provider ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE authdevice.registered_device_master_h ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE authdevice.reg_device_type ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE authdevice.reg_device_sub_type ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE authdevice.device_detail ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE authdevice.secure_biometric_interface ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE authdevice.secure_biometric_interface_h ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE authdevice.registered_device_master ALTER COLUMN is_deleted SET NOT NULL;

ALTER TABLE authdevice.ftp_chip_detail ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE authdevice.foundational_trust_provider ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE authdevice.registered_device_master_h ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE authdevice.reg_device_type ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE authdevice.reg_device_sub_type ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE authdevice.device_detail ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE authdevice.secure_biometric_interface ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE authdevice.secure_biometric_interface_h ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE authdevice.registered_device_master ALTER COLUMN is_deleted SET DEFAULT FALSE;

----------------------------------------------------------------------------------------------------
