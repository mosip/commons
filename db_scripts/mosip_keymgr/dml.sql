\c mosip_keymgr sysadmin

\set CSVDataPath '\'/home/dbadmin/mosip_keymgr/'

-------------- Level 1 data load scripts ------------------------

----- TRUNCATE keymgr.key_policy_def TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE keymgr.key_policy_def cascade ;

\COPY keymgr.key_policy_def (app_id,key_validity_duration,is_active,cr_by,cr_dtimes) FROM './dml/keymgr-key_policy_def.csv' delimiter ',' HEADER  csv;


----- TRUNCATE keymgr.key_policy_def_h TABLE Data and It's reference Data and COPY Data from CSV file -----
TRUNCATE TABLE keymgr.key_policy_def_h cascade ;

\COPY keymgr.key_policy_def_h (app_id,key_validity_duration,is_active,cr_by,cr_dtimes,eff_dtimes) FROM './dml/keymgr-key_policy_def_h.csv' delimiter ',' HEADER  csv;

---------------------------------------------------------------------------------------------------------------------------------------------------------------------


















