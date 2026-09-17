\c mosip_authdevice 

TRUNCATE TABLE authdevice.reg_device_type cascade ;

\COPY authdevice.reg_device_type (code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/authdevice-reg_device_type.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE authdevice.reg_device_sub_type cascade ;

\COPY authdevice.reg_device_sub_type (code,dtyp_code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/authdevice-reg_device_sub_type.csv' delimiter ',' HEADER  csv;















