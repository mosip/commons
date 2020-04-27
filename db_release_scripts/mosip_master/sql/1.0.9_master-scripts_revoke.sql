-- --------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.0.9
-- Purpose    		: Revoking Database Alter deployement done for release in Master DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: 23-Apr-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- -----------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin


----- DROP Constraints on the new tables created for 1.0.9 release -----
ALTER TABLE master.location DROP CONSTRAINT fk_loc_lochierlst;

----- DROP Tables created for 1.0.9 release -----
DROP TABLE IF EXISTS master.loc_hierarchy_list;

DROP TABLE IF EXISTS master.schema_definition;
DROP TABLE IF EXISTS master.dynamic_field;
DROP TABLE IF EXISTS master.identity_schema;
--------------------------------------------------------------------------------------------------------------