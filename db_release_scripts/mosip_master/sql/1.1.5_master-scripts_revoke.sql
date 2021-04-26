-- --------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.1.5
-- Purpose    		: Revoking Database Alter deployement done for release in Master DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Apr-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- -----------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin

TRUNCATE TABLE master.dynamic_field cascade ;

\COPY master.dynamic_field (id,name,description,data_type,value_json,lang_code,is_active,cr_by,cr_dtimes) FROM '../dml/master-dynamic_field.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE master.identity_schema cascade ;

\COPY master.identity_schema (id,id_version,title,description,id_attr_json,schema_json,status_code,add_props,effective_from,lang_code,is_active,cr_by,cr_dtimes) FROM '../dml/master-identity_schema.csv' delimiter ',' HEADER  csv;
--------------------------------------------------------------------------------------------------------------
