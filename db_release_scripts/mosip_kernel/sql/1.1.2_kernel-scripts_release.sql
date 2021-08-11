-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_kernel
-- Release Version 	: 1.2.0-rc1
-- Purpose    		: Database Alter scripts for the release for Kernel DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: Sep-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_kernel sysadmin

DROP TABLE IF EXISTS kernel.key_alias CASCADE;
DROP TABLE IF EXISTS kernel.key_policy_def CASCADE;
DROP TABLE IF EXISTS kernel.key_policy_def_h CASCADE;
DROP TABLE IF EXISTS kernel.key_store CASCADE;
DROP TABLE IF EXISTS kernel.sync_control CASCADE;
DROP TABLE IF EXISTS kernel.sync_job_def CASCADE;
DROP TABLE IF EXISTS kernel.sync_transaction CASCADE;
DROP TABLE IF EXISTS kernel.dao_key_store CASCADE;
----------------------------------------------------------------------------------------------------