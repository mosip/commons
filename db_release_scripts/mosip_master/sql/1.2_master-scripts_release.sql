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
\c mosip_master sysadmin

\ir ../ddl/master-ui_spec.sql

TRUNCATE TABLE master.ui_spec  cascade ;

\COPY master.ui_spec (ID, VERSION , DOMAIN, TITLE, DESCRIPTION, TYPE, IDENTITY_SCHEMA_ID, IDENTITY_SCHEMA_VERSION, JSON_SPEC, STATUS_CODE, EFFECTIVE_FROM , ADD_PROPS, IS_ACTIVE , CR_BY, CR_DTIMES, UPD_BY, UPD_DTIMES, IS_DELETED,DEL_DTIMES) FROM './dml/master-ui_spec.csv' delimiter ',' HEADER  csv;


---------------------------------------------------------------------------------------------------------------------
