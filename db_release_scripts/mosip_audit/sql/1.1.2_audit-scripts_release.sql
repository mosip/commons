-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_audit
-- Release Version 	: 1.2.0-SNAPSHOT
-- Purpose    		: Database Alter scripts for the release for Audit DB.       
-- Create By   		: Sadanandegowda DM
-- Created Date		: Sep-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -------------------------------------------------------------------------------------------------

\c mosip_audit sysadmin

ALTER TABLE audit.app_audit_log ALTER COLUMN host_ip TYPE varchar(256) USING host_ip::varchar;
