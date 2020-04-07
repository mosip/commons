-- ---------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.0.8
-- Purpose    		: Database Alter scripts for the release for Master DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: 07-Apr-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin

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

\COPY master.machine_master (id,name,mac_address,serial_num,ip_address,mspec_id,public_key,key_index,zone_code,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-machine_master.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.user_detail TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.user_detail cascade ;

\COPY master.user_detail (id,uin,name,email,mobile,status_code,lang_code,last_login_method,is_active,cr_by,cr_dtimes) FROM './dml/master-user_detail.csv' delimiter ',' HEADER  csv;


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

\COPY master.machine_master_h (id,name,mac_address,serial_num,ip_address,mspec_id,public_key,key_index,zone_code,lang_code,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-machine_master_h.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.user_detail_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.user_detail_h cascade ;

\COPY master.user_detail_h (id,uin,name,email,mobile,status_code,lang_code,last_login_method,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/master-user_detail_h.csv' delimiter ',' HEADER  csv;

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