\c mosip_regdevice sysadmin

\set CSVDataPath '\'/home/dbadmin/mosip_regdevice/dml'

-------------- Level 1 data load scripts ------------------------

----- TRUNCATE regdevice.reg_device_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE regdevice.reg_device_type cascade ;

\COPY regdevice.reg_device_type (code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/regdevice-reg_device_type.csv' delimiter ',' HEADER  csv;

-------------- Level 2 data load scripts ------------------------

----- TRUNCATE regdevice.reg_device_sub_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE regdevice.reg_device_sub_type cascade ;

\COPY regdevice.reg_device_sub_type (code,dtyp_code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/regdevice-reg_device_sub_type.csv' delimiter ',' HEADER  csv;

----- TRUNCATE regdevice.device_detail TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE regdevice.device_detail cascade ;

\COPY regdevice.device_detail (id,dprovider_id,dtype_code,dstype_code,make,model,partner_org_name,approval_status,is_active,cr_by,cr_dtimes) FROM './dml/regdevice-device_detail.csv' delimiter ',' HEADER  csv;

----- TRUNCATE regdevice.registered_device_master TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE regdevice.registered_device_master cascade ;

\COPY regdevice.registered_device_master (code,status_code,device_id,device_sub_id,digital_id,serial_number,device_detail_id,purpose,firmware,expiry_date,certification_level,hotlisted,is_active,cr_by,cr_dtimes) FROM './dml/regdevice-registered_device_master.csv' delimiter ',' HEADER  csv;

----- TRUNCATE regdevice.secure_biometric_interface TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE regdevice.secure_biometric_interface cascade ;

\COPY regdevice.secure_biometric_interface (id,sw_binary_hash,sw_version,device_detail_id,sw_cr_dtimes,sw_expiry_dtimes,approval_status,is_active,cr_by,cr_dtimes) FROM './dml/regdevice-secure_biometric_interface.csv' delimiter ',' HEADER  csv;

----- TRUNCATE regdevice.registered_device_master_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE regdevice.registered_device_master_h cascade ;

\COPY regdevice.registered_device_master_h (code,status_code,device_id,device_sub_id,digital_id,serial_number,device_detail_id,purpose,firmware,expiry_date,certification_level,hotlisted,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/regdevice-registered_device_master_h.csv' delimiter ',' HEADER  csv;

----- TRUNCATE regdevice.secure_biometric_interface_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE regdevice.secure_biometric_interface_h cascade ;

\COPY regdevice.secure_biometric_interface_h (id,sw_binary_hash,sw_version,device_detail_id,sw_cr_dtimes,sw_expiry_dtimes,approval_status,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/regdevice-secure_biometric_interface_h.csv' delimiter ',' HEADER  csv;
















