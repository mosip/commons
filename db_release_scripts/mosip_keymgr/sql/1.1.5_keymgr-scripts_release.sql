-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_keymgr
-- Release Version 	: 1.1.5
-- Purpose    		: Database Alter scripts for the release for Key Manager DB.       
-- Create By   		: Chandra Keshav Mishra
-- Created Date		: Feb-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_keymgr sysadmin

ALTER TABLE keymgr.key_alias ADD COLUMN cert_thumbprint character varying(100);
----------------------------------------------------------------------------------------------------
