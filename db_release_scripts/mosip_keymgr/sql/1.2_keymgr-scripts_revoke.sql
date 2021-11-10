-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_keymgr
-- Release Version 	: 1.2.0
-- Purpose    		: Revoking Database Alter deployement done for release in Key manager DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Nov-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_kernel sysadmin

ALTER TABLE keymgr.key_alias DROP COLUMN cert_thumbprint;

-----------------------------------------------------------------------------------------------------
