-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_iam
-- Release Version 	: 1.2.0-SNAPSHOT
-- Purpose    		: Database Alter scripts for the release for IAM DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
--
-- -------------------------------------------------------------------------------------------------

\c mosip_iam sysadmin

-- -------------------------------------------------------------------------------------------------

ALTER TABLE iam.oauth_access_token ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE iam.role_list ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE iam.user_detail ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE iam.user_detail_h ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE iam.user_pwd ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE iam.user_role ALTER COLUMN is_deleted SET NOT NULL;

ALTER TABLE iam.oauth_access_token ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE iam.role_list ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE iam.user_detail ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE iam.user_detail_h ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE iam.user_pwd ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE iam.user_role ALTER COLUMN is_deleted SET DEFAULT FALSE;

----------------------------------------------------------------------------------------------------
