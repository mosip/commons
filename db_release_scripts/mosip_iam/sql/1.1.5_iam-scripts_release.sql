
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
