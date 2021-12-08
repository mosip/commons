-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_authdevice
-- Release Version 	: 1.2.0-SNAPSHOT
-- Purpose    		: Database Alter scripts for the release for Authdevice DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Oct-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
--
-- -------------------------------------------------------------------------------------------------

\c mosip_authdevice sysadmin

-- -------------------------------------------------------------------------------------------------

ALTER TABLE authdevice.secure_biometric_interface DROP CONSTRAINT IF EXISTS fk_sbi_id CASCADE;


ALTER TABLE authdevice.secure_biometric_interface ADD partner_org_name varchar(128);

ALTER TABLE authdevice.secure_biometric_interface ADD provider_id varchar(36);

ALTER TABLE authdevice.secure_biometric_interface_h ADD partner_org_name varchar(128);

ALTER TABLE authdevice.secure_biometric_interface_h ADD provider_id varchar(36);

----------------------------------------------------------------------------------------------------
