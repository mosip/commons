-- --------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.1.5
-- Purpose    		: Revoking Database Alter deployement done for release in Master DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- -----------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin

DROP TABLE master.ui_spec;

ALTER TABLE master.identity_schema ADD COLUMN id_attr_json character varying;
--------------------------------------------------------------------------------------------------------------
