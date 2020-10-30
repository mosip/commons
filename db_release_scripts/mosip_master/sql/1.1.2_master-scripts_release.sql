-- ---------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.1.2
-- Purpose    		: Database Alter scripts for the release for Master DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: Sep-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin

DROP TABLE IF EXISTS master.reg_center_device CASCADE;
DROP TABLE IF EXISTS master.reg_center_device_h CASCADE;
DROP TABLE IF EXISTS master.reg_center_machine CASCADE;
DROP TABLE IF EXISTS master.reg_center_machine_device CASCADE;
DROP TABLE IF EXISTS master.reg_center_machine_device_h CASCADE;
DROP TABLE IF EXISTS master.reg_center_machine_h CASCADE;
DROP TABLE IF EXISTS master.reg_center_user CASCADE;
DROP TABLE IF EXISTS master.reg_center_user_h CASCADE;
DROP TABLE IF EXISTS master.reg_center_user_machine CASCADE;
DROP TABLE IF EXISTS master.reg_center_user_machine_h CASCADE;
DROP TABLE IF EXISTS master.transaction_type CASCADE;
DROP TABLE IF EXISTS master.user_pwd CASCADE;
DROP TABLE IF EXISTS master.user_role CASCADE;

DROP TABLE IF EXISTS master.reg_device_type CASCADE;
DROP TABLE IF EXISTS master.reg_device_sub_type CASCADE;
DROP TABLE IF EXISTS master.mosip_device_service CASCADE;
DROP TABLE IF EXISTS master.mosip_device_service_h CASCADE;
DROP TABLE IF EXISTS master.device_provider CASCADE;
DROP TABLE IF EXISTS master.device_provider_h CASCADE;
DROP TABLE IF EXISTS master.foundational_trust_provider CASCADE;
DROP TABLE IF EXISTS master.foundational_trust_provider_h CASCADE;
DROP TABLE IF EXISTS master.registered_device_master CASCADE;
DROP TABLE IF EXISTS master.registered_device_master_h CASCADE;

\ir ../ddl/master-sync_job_def.sql
\ir ../ddl/master-bulkupload_transaction.sql

ALTER TABLE master.device_master ADD COLUMN IF NOT EXISTS regcntr_id character varying(10);
ALTER TABLE master.machine_master ADD COLUMN IF NOT EXISTS regcntr_id character varying(10);
ALTER TABLE master.user_detail ADD COLUMN IF NOT EXISTS regcntr_id character varying(10);

ALTER TABLE master.device_master_h ADD COLUMN IF NOT EXISTS regcntr_id character varying(10);
ALTER TABLE master.machine_master_h ADD COLUMN IF NOT EXISTS regcntr_id character varying(10);
ALTER TABLE master.user_detail_h ADD COLUMN IF NOT EXISTS regcntr_id character varying(10);

ALTER TABLE master.machine_master ADD COLUMN IF NOT EXISTS sign_public_key character varying(1024);
ALTER TABLE master.machine_master ADD COLUMN IF NOT EXISTS sign_key_index character varying(128);
ALTER TABLE master.machine_master ALTER COLUMN public_key DROP NOT NULL;
ALTER TABLE master.machine_master ALTER COLUMN key_index DROP NOT NULL;

ALTER TABLE master.machine_master_h ADD COLUMN IF NOT EXISTS sign_public_key character varying(1024);
ALTER TABLE master.machine_master_h ADD COLUMN IF NOT EXISTS sign_key_index character varying(128);
ALTER TABLE master.machine_master_h ALTER COLUMN public_key DROP NOT NULL;
ALTER TABLE master.machine_master_h ALTER COLUMN key_index DROP NOT NULL;


-- object: fk_machm_center | type: CONSTRAINT --
ALTER TABLE master.machine_master DROP CONSTRAINT IF EXISTS fk_machm_center CASCADE;
ALTER TABLE master.machine_master ADD CONSTRAINT fk_machm_center FOREIGN KEY (regcntr_id,lang_code)
REFERENCES master.registration_center (id,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_devicem_center | type: CONSTRAINT --
ALTER TABLE master.device_master DROP CONSTRAINT IF EXISTS fk_devicem_center CASCADE;
ALTER TABLE master.device_master ADD CONSTRAINT fk_devicem_center FOREIGN KEY (regcntr_id,lang_code)
REFERENCES master.registration_center (id,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_usrdtl_center | type: CONSTRAINT --
ALTER TABLE master.user_detail DROP CONSTRAINT IF EXISTS fk_usrdtl_center CASCADE;
ALTER TABLE master.user_detail ADD CONSTRAINT fk_usrdtl_center FOREIGN KEY (regcntr_id,lang_code)
REFERENCES master.registration_center (id,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --


ALTER TABLE master.dynamic_field ALTER COLUMN value_json TYPE character varying;

ALTER TABLE master.identity_schema ALTER COLUMN id_attr_json TYPE character varying;
ALTER TABLE master.identity_schema ALTER COLUMN schema_json TYPE character varying;

ALTER TABLE master.template ALTER COLUMN file_txt TYPE character varying(10240);


\ir ../ddl/master-batch_job_execution.sql
\ir ../ddl/master-batch_job_execution_context.sql
\ir ../ddl/master-batch_job_execution_param.sql
\ir ../ddl/master-batch_job_instance.sql
\ir ../ddl/master-batch_step_execution.sql
\ir ../ddl/master-batch_step_execution_context.sql

\ir ../ddl/master-batch-fk.sql


----- TRUNCATE master.sync_job_def TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.sync_job_def  cascade ;

\COPY master.sync_job_def (ID,NAME,API_NAME,PARENT_SYNCJOB_ID,SYNC_FREQ,LOCK_DURATION,LANG_CODE,IS_ACTIVE,CR_BY,CR_DTIMES,UPD_BY,UPD_DTIMES,IS_DELETED,DEL_DTIMES) FROM './dml/master-sync_job_def.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.dynamic_field TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.dynamic_field cascade ;

\COPY master.dynamic_field (id,name,description,data_type,value_json,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-dynamic_field.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.device_master TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.device_master cascade ;

\COPY master.device_master (id,name,mac_address,serial_num,ip_address,dspec_id,zone_code,regcntr_id,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-device_master.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.machine_master TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.machine_master cascade ;

\COPY master.machine_master (id,name,mac_address,serial_num,ip_address,mspec_id,public_key,key_index,zone_code,regcntr_id,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-machine_master.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.user_detail TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.user_detail cascade ;

\COPY master.user_detail (id,uin,name,email,mobile,status_code,regcntr_id,lang_code,last_login_method,is_active,cr_by,cr_dtimes) FROM './dml/master-user_detail.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.template_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.template_type cascade ;

\COPY master.template_type (code,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-template_type.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.template TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.template cascade ;

\COPY master.template (id,name,descr,file_format_code,model,file_txt,module_id,module_name,template_typ_code,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-template.csv' delimiter ',' HEADER  csv;


----- TRUNCATE master.device_master_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.device_master_h cascade ;

\COPY master.device_master_h (id,name,mac_address,serial_num,ip_address,dspec_id,zone_code,regcntr_id,lang_code,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-device_master_h.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.machine_master_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.machine_master_h cascade ;

\COPY master.machine_master_h (id,name,mac_address,serial_num,ip_address,mspec_id,public_key,key_index,zone_code,regcntr_id,lang_code,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-machine_master_h.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.user_detail_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.user_detail_h cascade ;

\COPY master.user_detail_h (id,uin,name,email,mobile,status_code,regcntr_id,lang_code,last_login_method,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-user_detail_h.csv' delimiter ',' HEADER  csv;





---------------------------------------------------------------------------------------------------------------