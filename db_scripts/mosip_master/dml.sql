\c mosip_master 

-------------- Level 1 data load scripts ------------------------

----- TRUNCATE master.app_detail TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.app_detail cascade ;

\COPY master.app_detail (id,name,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-app_detail.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.authentication_method TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.authentication_method cascade ;

\COPY master.authentication_method (code,method_seq,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-authentication_method.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.biometric_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.biometric_type cascade ;

\COPY master.biometric_type (code,name,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-biometric_type.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.doc_category TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.doc_category cascade ;

\COPY master.doc_category (code,name,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-doc_category.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.gender TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.gender cascade ;

\COPY master.gender (code,name,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-gender.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.module_detail TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.module_detail cascade ;

\COPY master.module_detail (id,name,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-module_detail.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.process_list TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.process_list cascade ;

\COPY master.process_list (id,name,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-process_list.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.reason_category TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.reason_category cascade ;

\COPY master.reason_category (code,name,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-reason_category.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.role_list TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.role_list cascade ;

\COPY master.role_list (code,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-role_list.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.status_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.status_type cascade ;

\COPY master.status_type (code,name,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-status_type.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.template_file_format TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.template_file_format cascade ;

\COPY master.template_file_format (code,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-template_file_format.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.template_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.template_type cascade ;

\COPY master.template_type (code,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-template_type.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.daysofweek_list TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.daysofweek_list cascade ;

\COPY master.daysofweek_list (code,name,day_seq,is_global_working,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-daysofweek_list.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.sync_job_def TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.sync_job_def  cascade ;

\COPY master.sync_job_def (ID,NAME,API_NAME,PARENT_SYNCJOB_ID,SYNC_FREQ,LOCK_DURATION,LANG_CODE,IS_ACTIVE,CR_BY,CR_DTIMES,UPD_BY,UPD_DTIMES,IS_DELETED,DEL_DTIMES) FROM './dml/master-sync_job_def.csv' delimiter ',' HEADER  csv;

-------------- Level 2 data load scripts ------------------------

----- TRUNCATE master.app_authentication_method TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.app_authentication_method cascade ;

\COPY master.app_authentication_method (app_id,process_id,role_code,auth_method_code,method_seq,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-app_authentication_method.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.app_role_priority TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.app_role_priority cascade ;

\COPY master.app_role_priority (app_id,process_id,role_code,priority,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-app_role_priority.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.biometric_attribute TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.biometric_attribute cascade ;

\COPY master.biometric_attribute (code,name,descr,bmtyp_code,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-biometric_attribute.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.screen_detail TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.screen_detail cascade ;

\COPY master.screen_detail (id,app_id,name,descr,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-screen_detail.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.status_list TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.status_list cascade ;

\COPY master.status_list (code,descr,sttyp_code,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-status_list.csv' delimiter ',' HEADER  csv;

----- TRUNCATE master.template TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.template cascade ;

\COPY master.template (id,name,descr,file_format_code,model,file_txt,module_id,module_name,template_typ_code,lang_code,is_active,cr_by,cr_dtimes) FROM './dml/master-template.csv' delimiter ',' HEADER  csv;

-------------- Level 3 data load scripts ------------------------

----- TRUNCATE master.screen_authorization TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE master.screen_authorization cascade ;

\COPY master.screen_authorization (screen_id,role_code,lang_code,is_permitted,is_active,cr_by,cr_dtimes) FROM './dml/master-screen_authorization.csv' delimiter ',' HEADER  csv;

















