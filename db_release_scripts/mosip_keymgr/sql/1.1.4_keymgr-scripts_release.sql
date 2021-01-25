-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_keymgr
-- Release Version 	: 1.1.4
-- Purpose    		: Database Alter scripts for the release for Key Manager DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: Dec-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_keymgr sysadmin

ALTER TABLE keymgr.partner_cert_store ALTER COLUMN cert_data TYPE character varying;
ALTER TABLE keymgr.partner_cert_store ALTER COLUMN signed_cert_data TYPE character varying;

ALTER TABLE keymgr.ca_cert_store ALTER COLUMN cert_data TYPE character varying;

ALTER TABLE keymgr.key_store ALTER COLUMN certificate_data TYPE character varying;

----------------------------------------------------------------------------------------------------