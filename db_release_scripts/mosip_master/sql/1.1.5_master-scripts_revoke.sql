-- --------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.1.5
-- Purpose    		: Revoking Database Alter deployement done for release in Master DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Apr-2021
-- 
-- Modified Date        Modified By         	Comments / Remarks
-- Sept-2021		Chandra Keshav Mishra	Removed the TRUNCATE statements from the revoke scripts.
--						Also as part of 1.1.5 revoke script noting is required as we are just adding a table there in 1.1.5 and if downgrade is done there is no harm in keeping the table intact there.
--
-- -----------------------------------------------------------------------------------------------------------

-- -----------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin


--------------------------------------------------------------------------------------------------------------
