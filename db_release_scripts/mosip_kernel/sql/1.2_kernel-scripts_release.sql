-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_kernel
-- Release Version 	: 1.2
-- Purpose    		: Database Alter scripts for the release for Kernel DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_kernel sysadmin


CREATE INDEX IF NOT EXISTS idx_prid_status ON kernel.prid USING btree (prid_status);

----------------------------------------------------------------------------------------------------
