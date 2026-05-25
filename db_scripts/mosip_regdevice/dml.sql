\c :mosipdbname

TRUNCATE TABLE :dbuname.reg_device_type cascade ;

\COPY :dbuname.reg_device_type (code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/:dbuname-reg_device_type.csv' delimiter ',' HEADER  csv;

TRUNCATE TABLE :dbuname.reg_device_sub_type cascade ;

\COPY :dbuname.reg_device_sub_type (code,dtyp_code,name,descr,is_active,cr_by,cr_dtimes) FROM './dml/:dbuname-reg_device_sub_type.csv' delimiter ',' HEADER  csv;
