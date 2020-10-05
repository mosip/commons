-- ---------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.1.0
-- Purpose    		: Database Alter scripts for the release for Master DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: May-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin

\ir ../ddl/master-daysofweek_list.sql
\ir ../ddl/master-reg_working_nonworking.sql
\ir ../ddl/master-reg_exceptional_holiday.sql

-- object: fk_rwn_daycode | type: CONSTRAINT --
-- ALTER TABLE master.reg_working_nonworking DROP CONSTRAINT IF EXISTS fk_rwn_daycode CASCADE;
ALTER TABLE master.reg_working_nonworking ADD CONSTRAINT fk_rwn_daycode FOREIGN KEY (day_code,lang_code)
REFERENCES master.daysofweek_list (code,lang_code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_rwn_regcntr | type: CONSTRAINT --
-- ALTER TABLE master.reg_working_nonworking DROP CONSTRAINT IF EXISTS fk_rwn_regcntr CASCADE;
ALTER TABLE master.reg_working_nonworking ADD CONSTRAINT fk_rwn_regcntr FOREIGN KEY (regcntr_id,lang_code)
REFERENCES master.registration_center (id,lang_code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_regeh_regcntr | type: CONSTRAINT --
-- ALTER TABLE master.reg_exceptional_holiday DROP CONSTRAINT IF EXISTS fk_regeh_regcntr CASCADE;
ALTER TABLE master.reg_exceptional_holiday ADD CONSTRAINT fk_regeh_regcntr FOREIGN KEY (regcntr_id,lang_code)
REFERENCES master.registration_center (id,lang_code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;


ALTER TABLE master.registered_device_master DROP COLUMN IF EXISTS foundational_trust_signature;
ALTER TABLE master.registered_device_master DROP COLUMN IF EXISTS foundational_trust_certificate;
ALTER TABLE master.registered_device_master DROP COLUMN IF EXISTS dprovider_signature;

ALTER TABLE master.registered_device_master_h DROP COLUMN IF EXISTS foundational_trust_signature;
ALTER TABLE master.registered_device_master_h DROP COLUMN IF EXISTS foundational_trust_certificate;
ALTER TABLE master.registered_device_master_h DROP COLUMN IF EXISTS dprovider_signature;

ALTER TABLE master.registered_device_master ALTER COLUMN firmware TYPE character varying(256);

ALTER TABLE master.registered_device_master_h ALTER COLUMN firmware TYPE character varying(256);

-------------- Level 1 data load scripts ------------------------

----- TRUNCATE master.daysofweek_list TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.daysofweek_list cascade ;

\COPY master.daysofweek_list (code,name,day_seq,is_global_working,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-daysofweek_list.csv' delimiter ',' HEADER  csv;


----- TRUNCATE master.template_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.template_type cascade ;

\COPY master.template_type (code,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-template_type.csv' delimiter ',' HEADER  csv;

-------------- Level 2 data load scripts ------------------------
----- TRUNCATE master.reg_working_nonworking TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.reg_working_nonworking cascade ;

\COPY master.reg_working_nonworking (regcntr_id,day_code,lang_code,is_working,is_active,cr_by,cr_dtimes) FROM './dml/master-reg_working_nonworking.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.reg_exceptional_holiday TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.reg_exceptional_holiday cascade ;

\COPY master.reg_exceptional_holiday (regcntr_id,hol_date,hol_name,hol_reason,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-reg_exceptional_holiday.csv' delimiter ',' HEADER  csv;


----- TRUNCATE master.template TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.template cascade ;

\COPY master.template (id,name,descr,file_format_code,model,file_txt,module_id,module_name,template_typ_code,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-template.csv' delimiter ',' HEADER  csv;

----------------------------------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------------------------------
ALTER TABLE master.machine_master DROP COLUMN mac_address;
ALTER TABLE master.machine_master DROP COLUMN serial_num;
ALTER TABLE master.machine_master DROP COLUMN public_key;
ALTER TABLE master.machine_master DROP COLUMN key_index;

ALTER TABLE master.machine_master ADD COLUMN mac_address character varying(64);
ALTER TABLE master.machine_master ADD COLUMN serial_num character varying(64);
ALTER TABLE master.machine_master ADD COLUMN public_key character varying(1024) NOT NULL DEFAULT 'DUMMY KEY';
ALTER TABLE master.machine_master ADD COLUMN key_index character varying(128) NOT NULL DEFAULT 'DUMMY INDEX';

ALTER TABLE master.machine_master_h DROP COLUMN mac_address;
ALTER TABLE master.machine_master_h DROP COLUMN serial_num;
ALTER TABLE master.machine_master_h DROP COLUMN public_key;
ALTER TABLE master.machine_master_h DROP COLUMN key_index;

ALTER TABLE master.machine_master_h ADD COLUMN mac_address character varying(64);
ALTER TABLE master.machine_master_h ADD COLUMN serial_num character varying(64);
ALTER TABLE master.machine_master_h ADD COLUMN public_key character varying(1024) NOT NULL DEFAULT 'DUMMY KEY';
ALTER TABLE master.machine_master_h ADD COLUMN key_index character varying(128) NOT NULL DEFAULT 'DUMMY INDEX';

-------------- Level 3 data load scripts ------------------------
----- TRUNCATE master.machine_master TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.machine_master cascade ;

\COPY master.machine_master (id,name,mac_address,serial_num,ip_address,mspec_id,public_key,key_index,zone_code,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-machine_master_1.1.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.user_detail TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.user_detail cascade ;

\COPY master.user_detail (id,uin,name,email,mobile,status_code,lang_code,last_login_method,is_active,cr_by,cr_dtimes) FROM './dml/master-user_detail_1.1.csv' delimiter ',' HEADER  csv;


-------------- Level 4 data load scripts ------------------------
----- TRUNCATE master.reg_center_machine TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.reg_center_machine cascade ;

\COPY master.reg_center_machine (regcntr_id,machine_id,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-reg_center_machine.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.reg_center_user TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.reg_center_user cascade ;

\COPY master.reg_center_user (regcntr_id,usr_id,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-reg_center_user.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.reg_center_user_machine TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.reg_center_user_machine cascade ;

\COPY master.reg_center_user_machine (regcntr_id,usr_id,machine_id,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-reg_center_user_machine.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.zone_user TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.zone_user cascade ;

\COPY master.zone_user (zone_code,usr_id,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-zone_user.csv' delimiter ',' HEADER  csv;


-------------- Level 5 data load scripts ------------------------


----- TRUNCATE master.machine_master_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.machine_master_h cascade ;

\COPY master.machine_master_h (id,name,mac_address,serial_num,ip_address,mspec_id,public_key,key_index,zone_code,lang_code,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-machine_master_1.1_h.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.user_detail_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.user_detail_h cascade ;

\COPY master.user_detail_h (id,uin,name,email,mobile,status_code,lang_code,last_login_method,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-user_detail_1.1_h.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.reg_center_machine_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.reg_center_machine_h cascade ;

\COPY master.reg_center_machine_h (regcntr_id,machine_id,lang_code,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-reg_center_machine_h.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.reg_center_user_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.reg_center_user_h cascade ;

\COPY master.reg_center_user_h (regcntr_id,usr_id,lang_code,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-reg_center_user_h.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.reg_center_user_machine_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.reg_center_user_machine_h cascade ;

\COPY master.reg_center_user_machine_h (regcntr_id,usr_id,machine_id,lang_code,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-reg_center_user_machine_h.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.zone_user TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.zone_user_h cascade ;

\COPY master.zone_user_h (zone_code,usr_id,lang_code,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-zone_user_h.csv' delimiter ',' HEADER  csv;

----------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------

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
