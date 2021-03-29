-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_keymgr
-- Release Version 	: 1.1.6-SNAPSHOT
-- Purpose    		: Database Alter scripts for the release for Key Manager DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_keymgr sysadmin

ALTER TABLE keymgr.key_alias ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE keymgr.licensekey_list ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE keymgr.tsp_licensekey_map ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE keymgr.licensekey_permission ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE keymgr.partner_cert_store ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE keymgr.ca_cert_store ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE keymgr.key_store ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE keymgr.key_policy_def ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE keymgr.key_policy_def_h ALTER COLUMN is_deleted SET NOT NULL;

ALTER TABLE keymgr.key_alias ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE keymgr.licensekey_list ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE keymgr.tsp_licensekey_map ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE keymgr.licensekey_permission ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE keymgr.partner_cert_store ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE keymgr.ca_cert_store ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE keymgr.key_store ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE keymgr.key_policy_def ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE keymgr.key_policy_def_h ALTER COLUMN is_deleted SET DEFAULT FALSE;


----------------------------------------------------------------------------------------------------
