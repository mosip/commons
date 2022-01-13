-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_regdevice
-- Release Version 	: 1.2.0-rc2
-- Purpose    		: Database Alter scripts for the release for Regdevice DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Nov-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_regdevice sysadmin

ALTER TABLE regdevice.secure_biometric_interface DROP CONSTRAINT IF EXISTS fk_sbi_id CASCADE;
----------------------------------------------------------------------------------------------------
