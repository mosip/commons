\c mosip_authdevice sysadmin

\set CSVDataPath '\'/home/dbadmin/mosip_authdevice/dml'

-------------- Level 1 data load scripts ------------------------

----- TRUNCATE authdevice.reg_device_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE authdevice.reg_device_type cascade ;

\COPY authdevice.reg_device_type (code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/authdevice-reg_device_type.csv' delimiter ',' HEADER  csv;

-------------- Level 2 data load scripts ------------------------

----- TRUNCATE authdevice.reg_device_sub_type TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE authdevice.reg_device_sub_type cascade ;

\COPY authdevice.reg_device_sub_type (code,dtyp_code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/authdevice-reg_device_sub_type.csv' delimiter ',' HEADER  csv;















