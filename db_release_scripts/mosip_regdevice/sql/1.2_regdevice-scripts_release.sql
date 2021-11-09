-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_regdevice
-- Release Version 	: 1.2.0-SNAPSHOT
-- Purpose    		: Database Alter scripts for the release for Regdevice DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Oct-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_regdevice sysadmin

ALTER TABLE regdevice.secure_biometric_interface DROP CONSTRAINT IF EXISTS fk_sbi_id CASCADE;

ALTER TABLE regdevice.secure_biometric_interface ALTER COLUMN device_detail_id TYPE character varying;
ALTER TABLE regdevice.secure_biometric_interface_h ALTER COLUMN device_detail_id TYPE character varying;
----------------------------------------------------------------------------------------------------


