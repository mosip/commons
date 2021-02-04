-- ---------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.1.4
-- Purpose    		: Database Alter scripts for the release for Master DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: Dec-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin

ALTER TABLE master.template ALTER COLUMN file_txt TYPE character varying;

insert into master.template_type values('reg-preview-template-part', 'Registration Preview Template', 'eng', 'true', 'superadmin', current_timestamp, null, null, null, null);
insert into master.template_type values('reg-ack-template-part', 'Registration Acknowledgement Template', 'eng', 'true', 'superadmin', current_timestamp, null, null, null, null); 