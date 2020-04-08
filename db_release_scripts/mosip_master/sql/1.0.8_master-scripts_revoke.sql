-- --------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.0.8
-- Purpose    		: Revoking Database Alter deployement done for release in Master DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: 07-Apr-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- -----------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin

ALTER TABLE master.machine_master ALTER COLUMN mac_address TYPE character varying(64) NOT NULL DEFAULT 'DUMMY-MAC';
ALTER TABLE master.machine_master ALTER COLUMN serial_num TYPE character varying(64) NOT NULL DEFAULT 'DUMMY-SERIAL';
ALTER TABLE master.machine_master ALTER COLUMN public_key TYPE bytea;
ALTER TABLE master.machine_master ALTER COLUMN key_index TYPE character varying(128);


ALTER TABLE master.machine_master_h ALTER COLUMN mac_address TYPE character varying(64) NOT NULL DEFAULT 'DUMMY-MAC';
ALTER TABLE master.machine_master_h ALTER COLUMN serial_num TYPE character varying(64) NOT NULL DEFAULT 'DUMMY-SERIAL';
ALTER TABLE master.machine_master_h ALTER COLUMN public_key TYPE bytea;
ALTER TABLE master.machine_master_h ALTER COLUMN key_index TYPE character varying(128);