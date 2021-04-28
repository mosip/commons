-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.permitted_local_config
-- Purpose    	: 
--           
-- Create By   	: Ram Bhatt
-- Created Date	: Apr-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------

-- object: master.permitted_local_config | type: TABLE --
-- DROP TABLE IF EXISTS master.permitted_local_config CASCADE;
CREATE TABLE master.permitted_local_config (
	code character varying(128) NOT NULL,
	name character varying(128) NOT NULL,
	config_type character varying(128) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(32) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(32),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT permitted_local_config_pk PRIMARY KEY (code)

);
