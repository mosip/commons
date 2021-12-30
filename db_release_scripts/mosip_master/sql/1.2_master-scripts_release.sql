-- ---------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.2
-- Purpose    		: Database Alter scripts for the release for Master DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: March-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------
-- Apr-2021		Ram Bhatt  	   Removed bulk upload transaction size limit
-- Apr-2021		Ram Bhatt	   Lang_code nullable, removed from pk constraints and multiple FK constraint changes	
-- Apr-2021 		Ram Bhatt          Added new rows in template,template_type and module_detail csv
-- Apr-2021 		Ram Bhatt  	   Creation of master.permitted_local_config
-- May-2021		Ram Bhatt	   Changed Precision and size of version and identity_schema_version
-- Jul-2021		Ram Bhatt          Creation of blocklisted table
-- Aug-2021		Ram Bhatt	   Remove primary key constraint from blacklisted table
-- Aug-2021		Ram Bhatt	   Column size increased for template_typ_code and code column
-- ------------------------------------------------------------------------------------------------------------

\c mosip_master sysadmin
-----------------------------------------------------------------------------------------------------------------------

ALTER TABLE master.template_type ALTER COLUMN code TYPE character varying(64) ;
ALTER TABLE master.template ALTER COLUMN template_typ_code TYPE character varying(64) ;

--------------------------------------------------------------------------------------------------------------------
ALTER TABLE master.blacklisted_words DROP CONSTRAINT IF EXISTS pk_blwrd_code CASCADE;
-------------------------------------------------------------------------------------------------------------------
\ir ../ddl/master-ui_spec.sql
\ir ../ddl/master-blocklisted_words.sql


----- TRUNCATE master.blocklisted_words TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.blocklisted_words cascade ;

\COPY master.blocklisted_words (word,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-blocklisted_words.csv' delimiter ',' HEADER  csv;


--------------------------------------------UI SPEC TABLE CREATION-----------------------------------------------
TRUNCATE TABLE master.ui_spec  cascade ;

---------------------------------------------------------------------------------------------------------------------
\COPY master.ui_spec (ID, VERSION , DOMAIN, TITLE, DESCRIPTION, TYPE, IDENTITY_SCHEMA_ID, IDENTITY_SCHEMA_VERSION, JSON_SPEC, STATUS_CODE, EFFECTIVE_FROM , ADD_PROPS, IS_ACTIVE , CR_BY, CR_DTIMES, UPD_BY, UPD_DTIMES, IS_DELETED,DEL_DTIMES) FROM '../dml/master-ui_spec.csv' delimiter ',' HEADER  csv;
-----------------------------------------------------DATA LOAD FROM IDENTITY SCHEMA TABLE-----------------------------------------------
INSERT into master.ui_spec (id,version,domain,title,description,type,json_spec,identity_schema_id,identity_schema_version,effective_from,status_code,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes) SELECT id,id_version,'registration-client', title,description,'schema',id_attr_json,id,id_version,effective_from,status_code,is_active,cr_by,cr_dtimes,upd_by,upd_dtimes,is_deleted,del_dtimes FROM master.identity_schema;

-----------------------------------------------------------DROP COLUMN-----------------------------------------------------------------

ALTER TABLE master.identity_schema DROP COLUMN id_attr_json;
--------------------------------------------------------------------------------------------------------------------------------------

ALTER TABLE master.bulkupload_transaction ALTER COLUMN upload_description character varying;

-----------------------------------------------ALTER FK constraints with lang code -----------------------------------------------------------

ALTER TABLE master.device_master DROP CONSTRAINT IF EXISTS fk_devicem_dspec CASCADE;
ALTER TABLE master.device_master ADD CONSTRAINT fk_devicem_dspec FOREIGN KEY (dspec_id)
REFERENCES master.device_spec (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;


ALTER TABLE master.device_master DROP CONSTRAINT IF EXISTS fk_devicem_zone CASCADE;
ALTER TABLE master.device_master ADD CONSTRAINT fk_devicem_zone FOREIGN KEY (zone_code)
REFERENCES master.zone (code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;


ALTER TABLE master.device_master DROP CONSTRAINT IF EXISTS fk_devicem_center CASCADE;
ALTER TABLE master.device_master ADD CONSTRAINT fk_devicem_center FOREIGN KEY (regcntr_id)
REFERENCES master.registration_center (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;


ALTER TABLE master.device_spec DROP CONSTRAINT IF EXISTS fk_dspec_dtyp CASCADE;
ALTER TABLE master.device_spec ADD CONSTRAINT fk_dspec_dtyp FOREIGN KEY (dtyp_code)
REFERENCES master.device_type (code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;




ALTER TABLE master.machine_master DROP CONSTRAINT IF EXISTS fk_machm_mspec CASCADE;
ALTER TABLE master.machine_master ADD CONSTRAINT fk_machm_mspec FOREIGN KEY (mspec_id)
REFERENCES master.machine_spec (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;



ALTER TABLE master.machine_master DROP CONSTRAINT IF EXISTS fk_machm_zone CASCADE;
ALTER TABLE master.machine_master ADD CONSTRAINT fk_machm_zone FOREIGN KEY (zone_code)
REFERENCES master.zone (code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;



ALTER TABLE master.machine_master DROP CONSTRAINT IF EXISTS fk_machm_center CASCADE;
ALTER TABLE master.machine_master ADD CONSTRAINT fk_machm_center FOREIGN KEY (regcntr_id)
REFERENCES master.registration_center (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;




ALTER TABLE master.machine_spec DROP CONSTRAINT IF EXISTS fk_mspec_mtyp CASCADE;
ALTER TABLE master.machine_spec ADD CONSTRAINT fk_mspec_mtyp FOREIGN KEY (mtyp_code)
REFERENCES master.machine_type (code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;



ALTER TABLE master.template DROP CONSTRAINT IF EXISTS fk_tmplt_tmpltyp CASCADE;
ALTER TABLE master.template ADD CONSTRAINT fk_tmplt_tmpltyp FOREIGN KEY (template_typ_code)
REFERENCES master.template_type (code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;



ALTER TABLE master.template DROP CONSTRAINT IF EXISTS fk_tmplt_tffmt CASCADE;
ALTER TABLE master.template ADD CONSTRAINT fk_tmplt_tffmt FOREIGN KEY (file_format_code)
REFERENCES master.template_file_format (code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.biometric_attribute DROP CONSTRAINT IF EXISTS fk_bmattr_bmtyp CASCADE;
ALTER TABLE master.biometric_attribute ADD CONSTRAINT fk_bmattr_bmtyp FOREIGN KEY (bmtyp_code)
REFERENCES master.biometric_type (code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.zone_user DROP CONSTRAINT IF EXISTS fk_zoneuser_zone CASCADE;
ALTER TABLE master.zone_user ADD CONSTRAINT fk_zoneuser_zone FOREIGN KEY (zone_code)
REFERENCES master.zone (code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;


ALTER TABLE master.reg_working_nonworking DROP CONSTRAINT IF EXISTS fk_rwn_daycode CASCADE;
ALTER TABLE master.reg_working_nonworking ADD CONSTRAINT fk_rwn_daycode FOREIGN KEY (day_code)
REFERENCES master.daysofweek_list (code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;



ALTER TABLE master.reg_working_nonworking DROP CONSTRAINT IF EXISTS fk_rwn_regcntr CASCADE;
ALTER TABLE master.reg_working_nonworking ADD CONSTRAINT fk_rwn_regcntr FOREIGN KEY (regcntr_id)
REFERENCES master.registration_center (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;


ALTER TABLE master.reg_exceptional_holiday DROP CONSTRAINT IF EXISTS fk_regeh_regcntr CASCADE;
ALTER TABLE master.reg_exceptional_holiday ADD CONSTRAINT fk_regeh_regcntr FOREIGN KEY (regcntr_id)
REFERENCES master.registration_center (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;


ALTER TABLE master.device_master_h DROP CONSTRAINT IF EXISTS pk_devicem_h_id CASCADE;
ALTER TABLE master.device_master_h ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.device_master_h ADD CONSTRAINT pk_devicem_h_id PRIMARY KEY (id,eff_dtimes);

ALTER TABLE master.machine_master_h DROP CONSTRAINT IF EXISTS pk_machm_h_id CASCADE;
ALTER TABLE master.machine_master_h ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.machine_master_h ADD CONSTRAINT pk_machm_h_id PRIMARY KEY (id,eff_dtimes);

ALTER TABLE master.user_detail_h DROP CONSTRAINT IF EXISTS pk_usrdtl_h_id CASCADE;
ALTER TABLE master.user_detail_h ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.user_detail_h ADD CONSTRAINT pk_usrdtl_h_id PRIMARY KEY (id,eff_dtimes);


--------------------------------------------------------------DROP CONSTRAINTS-------------------------------------------

ALTER TABLE master.dynamic_field DROP CONSTRAINT IF EXISTS uk_schfld_name;

ALTER TABLE master.user_detail ALTER COLUMN status_code DROP NOT NULL;
ALTER TABLE master.user_detail ALTER COLUMN name DROP NOT NULL;
ALTER TABLE master.user_detail ALTER COLUMN lang_code DROP NOT NULL;

ALTER TABLE master.user_detail DROP CONSTRAINT IF EXISTS fk_usrdtl_center CASCADE;
ALTER TABLE master.zone_user DROP CONSTRAINT IF EXISTS fk_zoneuser_zone CASCADE;


--------------------------------------------LANG CODE NULLABLE AND CHANGE PK CONSTRAINTS ---------------------------------

ALTER TABLE master.app_authentication_method DROP CONSTRAINT IF EXISTS pk_appauthm_id;
ALTER TABLE master.app_authentication_method ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.app_authentication_method ADD CONSTRAINT pk_appauthm_id PRIMARY KEY (id);


ALTER TABLE master.app_role_priority DROP CONSTRAINT IF EXISTS pk_roleprt_id;
ALTER TABLE master.app_role_priority ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.app_role_priority ADD CONSTRAINT pk_roleprt_id PRIMARY KEY (app_id,process_id,role_code);

ALTER TABLE master.applicant_valid_document DROP CONSTRAINT IF EXISTS pk_avaldoc_code;
ALTER TABLE master.applicant_valid_document ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.applicant_valid_document ADD CONSTRAINT pk_avaldoc_code PRIMARY KEY (apptyp_code,doccat_code,doctyp_code);

ALTER TABLE master.authentication_method DROP CONSTRAINT IF EXISTS pk_authm_code;
ALTER TABLE master.authentication_method ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.authentication_method ADD CONSTRAINT pk_authm_code PRIMARY KEY (code);


ALTER TABLE master.biometric_type DROP CONSTRAINT IF EXISTS pk_bmtyp_code;
ALTER TABLE master.biometric_type ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.biometric_type ADD CONSTRAINT pk_bmtyp_code PRIMARY KEY (code);


ALTER TABLE master.device_master DROP CONSTRAINT IF EXISTS pk_devicem_id;
ALTER TABLE master.device_master ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.device_master ADD CONSTRAINT pk_devicem_id PRIMARY KEY (id);


ALTER TABLE master.device_spec DROP CONSTRAINT IF EXISTS pk_dspec_code;
ALTER TABLE master.device_spec ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.device_spec ADD CONSTRAINT pk_dspec_code PRIMARY KEY (id);


ALTER TABLE master.device_type DROP CONSTRAINT IF EXISTS pk_dtyp_code;
ALTER TABLE master.device_type ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.device_type ADD CONSTRAINT pk_dtyp_code PRIMARY KEY (code);

ALTER TABLE master.device_type DROP CONSTRAINT IF EXISTS pk_dtyp_code;
ALTER TABLE master.device_type ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.device_type ADD CONSTRAINT pk_dtyp_code PRIMARY KEY (code);

ALTER TABLE master.machine_master DROP CONSTRAINT IF EXISTS pk_machm_id;
ALTER TABLE master.machine_master ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.machine_master ADD CONSTRAINT pk_machm_id PRIMARY KEY (id);


ALTER TABLE master.machine_spec DROP CONSTRAINT IF EXISTS pk_mspec_code;
ALTER TABLE master.machine_spec ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.machine_spec ADD CONSTRAINT pk_mspec_code PRIMARY KEY (id);


ALTER TABLE master.machine_type DROP CONSTRAINT IF EXISTS pk_mtyp_code;
ALTER TABLE master.machine_type ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.machine_type ADD CONSTRAINT pk_mtyp_code PRIMARY KEY (code);


ALTER TABLE master.reg_exceptional_holiday DROP CONSTRAINT IF EXISTS pk_exceptional_hol;
ALTER TABLE master.reg_exceptional_holiday ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.reg_exceptional_holiday ADD CONSTRAINT pk_exceptional_hol PRIMARY KEY (regcntr_id,hol_date);


ALTER TABLE master.reg_working_nonworking DROP CONSTRAINT IF EXISTS pk_working_nonworking;
ALTER TABLE master.reg_working_nonworking ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.reg_working_nonworking ADD CONSTRAINT pk_working_nonworking PRIMARY KEY (regcntr_id,day_code);


--ALTER TABLE master.template_file_format DROP CONSTRAINT IF EXISTS pk_tffmt_code;
--ALTER TABLE master.template_file_format ALTER COLUMN lang_code DROP NOT NULL;
--ALTER TABLE master.template_file_format ADD CONSTRAINT pk_tffmt_code PRIMARY KEY (code);


ALTER TABLE master.template_type DROP CONSTRAINT IF EXISTS pk_tmpltyp_code;
ALTER TABLE master.template_type ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.template_type ADD CONSTRAINT pk_tmpltyp_code PRIMARY KEY (code);


ALTER TABLE master.zone_user DROP CONSTRAINT IF EXISTS pk_zoneuser;
ALTER TABLE master.zone_user ALTER COLUMN lang_code DROP NOT NULL;
ALTER TABLE master.zone_user ALTER COLUMN zone_code DROP NOT NULL;
ALTER TABLE master.user_detail_h ALTER COLUMN status_code DROP NOT NULL;
ALTER TABLE master.zone_user ADD CONSTRAINT pk_zoneuser PRIMARY KEY (usr_id);
ALTER TABLE master.machine_master ADD CONSTRAINT uq_machm_name UNIQUE (name);
ALTER TABLE master.machine_master ADD CONSTRAINT uq_machm_key_index UNIQUE (key_index);
ALTER TABLE master.machine_master ADD CONSTRAINT uq_machm_skey_index UNIQUE (sign_key_index);

ALTER TABLE master.loc_holiday DROP CONSTRAINT IF EXISTS pk_lochol_id;
ALTER TABLE master.loc_holiday ADD CONSTRAINT pk_lochol_id PRIMARY KEY (holiday_date, location_code, lang_code);

-----------------------------------------------------------------------------------------------------------------------------------------------
-------------------------------------template,template_type and module_detail----------------------------------------------------------

TRUNCATE TABLE master.template cascade ;

\COPY master.template (id,name,descr,file_format_code,model,file_txt,module_id,module_name,template_typ_code,lang_code,is_active,cr_by,cr_dtimes) FROM '../dml/master-template.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE master.template_type cascade ;

\COPY master.template_type (code,descr,lang_code,is_active,cr_by,cr_dtimes) FROM '../dml/master-template_type.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE master.module_detail cascade ;

\COPY master.module_detail (id,name,descr,lang_code,is_active,cr_by,cr_dtimes) FROM '../dml/master-module_detail.csv' delimiter ',' HEADER  csv;

----------------------------------------------CREATION OF PERMITTED LOCAL CONFIG -------------------------------------------------------------

\ir ../ddl/master-permitted_local_config.sql

--------------------------------------------------------------------------------------------------------------------------------------------

-----------------------------------------------	ALTER TABLE OF MASTER UI SPEC TABLE----------------------------------------------------------

ALTER TABLE master.ui_spec ALTER COLUMN version TYPE numeric(5,3);
ALTER TABLE master.ui_spec ALTER COLUMN identity_schema_version TYPE numeric(5,3);


-------------------------------------------------------------------------------------------------------------------------------------------


ALTER TABLE master.ca_cert_store ADD CONSTRAINT cert_thumbprint_unique UNIQUE (cert_thumbprint,partner_domain);

