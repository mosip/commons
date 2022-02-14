\c mosip_regdevice 

TRUNCATE TABLE regdevice.reg_device_type cascade ;

\COPY regdevice.reg_device_type (code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/regdevice-reg_device_type.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE regdevice.reg_device_sub_type cascade ;

\COPY regdevice.reg_device_sub_type (code,dtyp_code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/regdevice-reg_device_sub_type.csv' delimiter ',' HEADER  csv;















