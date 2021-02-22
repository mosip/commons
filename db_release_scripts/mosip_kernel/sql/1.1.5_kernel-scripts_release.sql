-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_kernel
-- Release Version 	: 1.1.5
-- Purpose    		: Database Alter scripts for the release for Kernel DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_kernel sysadmin

ALTER TABLE mosip_kernel.otp_transaction ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE mosip_kernel.prid ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE mosip_kernel.uin_assigned ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE mosip_kernel.vid ALTER COLUMN is_deleted SET NOT NULL;
ALTER TABLE mosip_kernel.uin ALTER COLUMN is_deleted SET NOT NULL;

ALTER TABLE mosip_kernel.otp_transaction ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE mosip_kernel.prid ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE mosip_kernel.uin_assigned ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE mosip_kernel.vid ALTER COLUMN is_deleted SET DEFAULT FALSE;
ALTER TABLE mosip_kernel.uin ALTER COLUMN is_deleted SET DEFAULT FALSE;







----------------------------------------------------------------------------------------------------
