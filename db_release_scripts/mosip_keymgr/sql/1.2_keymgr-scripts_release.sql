-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_keymgr
-- Release Version 	: 1.2.0
-- Purpose    		: Database Alter scripts for the release for Key Manager DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Nov-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_keymgr sysadmin

ALTER TABLE keymgr.key_alias ADD COLUMN cert_thumbprint character varying(100);

----------------------------------------------------------------------------------------------------
