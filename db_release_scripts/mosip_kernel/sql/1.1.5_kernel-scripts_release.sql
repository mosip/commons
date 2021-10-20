-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_kernel
-- Release Version 	: 1.1.5.3
-- Purpose    		: Database Alter scripts for the release for Kernel DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------
-- Mar-2021		Ram Bhatt	    Reverting is_deleted not null changes
----------------------------------------------------------------------------------------------------

\c mosip_kernel sysadmin

--ALTER TABLE kernel.otp_transaction ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE kernel.prid ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE kernel.uin_assigned ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE kernel.vid ALTER COLUMN is_deleted SET NOT NULL;
--ALTER TABLE kernel.uin ALTER COLUMN is_deleted SET NOT NULL;

--ALTER TABLE kernel.otp_transaction ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE kernel.prid ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE kernel.uin_assigned ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE kernel.vid ALTER COLUMN is_deleted SET DEFAULT FALSE;
--ALTER TABLE kernel.uin ALTER COLUMN is_deleted SET DEFAULT FALSE;







----------------------------------------------------------------------------------------------------
