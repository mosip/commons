-- ---------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.2
-- Purpose    		: Database Alter scripts for the release for Master DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: March-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------------------
--------------------------------------------UI SPEC TABLE CREATION-----------------------------------------------
\c mosip_master sysadmin

\ir ../ddl/master-ui_spec.sql

TRUNCATE TABLE master.ui_spec  cascade ;

---------------------------------------------------------------------------------------------------------------------
\COPY master.ui_spec (ID, VERSION , DOMAIN, TITLE, DESCRIPTION, TYPE, IDENTITY_SCHEMA_ID, IDENTITY_SCHEMA_VERSION, JSON_SPEC, STATUS_CODE, EFFECTIVE_FROM , ADD_PROPS, IS_ACTIVE , CR_BY, CR_DTIMES, UPD_BY, UPD_DTIMES, IS_DELETED,DEL_DTIMES) FROM './dml/master-ui_spec.csv' delimiter ',' HEADER  csv;
-----------------------------------------------------DATA LOAD FROM IDENTITY SCHEMA TABLE-----------------------------------------------
INSERT into master.ui_spec (id,version,domain,title,description,type,json_spec,identity_schema_id,identity_schema_version,effective_from,status_code,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) SELECT id,id_version,'registration-client', title,description,'schema',id_attr_json,id,id_version,effective_from,status_code,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes FROM master.identity_schema;

-----------------------------------------------------------DROP COLUMN-----------------------------------------------------------------

ALTER TABLE master.identity_schema DROP COLUMN id_attr_json;
--------------------------------------------------------------------------------------------------------------------------------------

