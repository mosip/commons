-- ---------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.0.9
-- Purpose    		: Database Alter scripts for the release for Master DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: 02-Jun-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin

\ir ../ddl/master-loc_hierarchy_list.sql

\ir ../ddl/master-schema_definition.sql
\ir ../ddl/master-dynamic_field.sql
\ir ../ddl/master-identity_schema.sql


----- TRUNCATE master.template_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.loc_hierarchy_list cascade ;

\COPY master.loc_hierarchy_list (hierarchy_level,hierarchy_level_name,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-loc_hierarchy_list.csv' delimiter ',' HEADER  csv;


-- object: fk_loc_lochierlst | type: CONSTRAINT --
-- ALTER TABLE master.location DROP CONSTRAINT IF EXISTS fk_loc_lochierlst CASCADE;
ALTER TABLE master.location ADD CONSTRAINT fk_loc_lochierlst FOREIGN KEY (hierarchy_level,hierarchy_level_name,lang_code)
REFERENCES master.loc_hierarchy_list (hierarchy_level,hierarchy_level_name,lang_code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;


----- TRUNCATE master.identity_schema TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.identity_schema cascade ;

\COPY master.identity_schema (id,id_version,title,description,id_attr_json,schema_json,status_code,add_props,effective_from,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-identity_schema.csv' delimiter ',' HEADER  csv;

----------------------------------------------------------------------------------------------------