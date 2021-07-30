-- ---------------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Release Version 	: 1.2.0-rc1-SNAPSHOT
-- Purpose    		: Database Alter scripts for the release for Master DB.       
-- Create By   		: Ram Bhatt
-- Created Date		: Jan-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- -----------------------------------------------------------------------------------------------------------
-- Mar-2021		Ram Bhatt	    Reverting is_deleted not null changes for 1.1.5
-- Mar-2021		Ram Bhatt           Creation of master.ca_cert_store table
-- ------------------------------------------------------------------------------------------------------------
\c mosip_master sysadmin


\ir ../ddl/master-ca_cert_store.sql
