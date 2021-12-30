
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
