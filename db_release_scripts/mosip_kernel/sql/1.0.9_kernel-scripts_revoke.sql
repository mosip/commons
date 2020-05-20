-- -------------------------------------------------------------------------------------------------
-- Database Name	: mosip_kernel
-- Release Version 	: 1.0.9
-- Purpose    		: Revoking Database Alter deployement done for release in Kernel DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: 20-May-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_kernel sysadmin

DROP TABLE IF EXISTS kernel.uin_assigned;
-----------------------------------------------------------------------------------------------------