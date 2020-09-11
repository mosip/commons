-- --------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.1.0
-- Purpose    		: Revoking Database Alter deployement done for release in Master DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: May-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- -----------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin


----- TRUNCATE TABLE Data and It's reference Data loaded for 1.0 release -----
TRUNCATE TABLE master.daysofweek_list cascade ;
TRUNCATE TABLE master.reg_working_nonworking cascade ;
TRUNCATE TABLE master.reg_exceptional_holiday cascade ;

----- DROP Constraints on the new tables created for 1.0 release -----
ALTER TABLE master.reg_working_nonworking DROP CONSTRAINT fk_rwn_daycode;
ALTER TABLE master.reg_working_nonworking DROP CONSTRAINT fk_rwn_regcntr;
ALTER TABLE master.reg_exceptional_holiday DROP CONSTRAINT fk_regeh_regcntr;

----- DROP Tables created for 1.0 rlease -----
DROP TABLE IF EXISTS master.daysofweek_list;
DROP TABLE IF EXISTS master.reg_working_nonworking;
DROP TABLE IF EXISTS master.reg_exceptional_holiday;

--------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------

ALTER TABLE master.machine_master ALTER COLUMN mac_address TYPE character varying(64) NOT NULL DEFAULT 'DUMMY-MAC';
ALTER TABLE master.machine_master ALTER COLUMN serial_num TYPE character varying(64) NOT NULL DEFAULT 'DUMMY-SERIAL';
ALTER TABLE master.machine_master ALTER COLUMN public_key TYPE bytea;
ALTER TABLE master.machine_master ALTER COLUMN key_index TYPE character varying(128);


ALTER TABLE master.machine_master_h ALTER COLUMN mac_address TYPE character varying(64) NOT NULL DEFAULT 'DUMMY-MAC';
ALTER TABLE master.machine_master_h ALTER COLUMN serial_num TYPE character varying(64) NOT NULL DEFAULT 'DUMMY-SERIAL';
ALTER TABLE master.machine_master_h ALTER COLUMN public_key TYPE bytea;
ALTER TABLE master.machine_master_h ALTER COLUMN key_index TYPE character varying(128);
---------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------

----- DROP Constraints on the new tables created for 1.0.9 release -----
ALTER TABLE master.location DROP CONSTRAINT fk_loc_lochierlst;

----- DROP Tables created for 1.0.9 release -----
DROP TABLE IF EXISTS master.loc_hierarchy_list;

DROP TABLE IF EXISTS master.schema_definition;
DROP TABLE IF EXISTS master.dynamic_field;
DROP TABLE IF EXISTS master.identity_schema;
--------------------------------------------------------------------------------------------------------------