-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.ui_spec
-- Purpose    	: UI Specifications : Stores UI Specifications with values used in application modules.
--           
-- Create By   	: Ram Bhatt
-- Created Date	: March 2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- May-2021		Ram Bhatt	    Changed Precision and size of version and identity_schema_version
-- ------------------------------------------------------------------------------------------

-- object: master.ui_spec | type: TABLE --
-- DROP TABLE IF EXISTS master.ui_spec CASCADE;
CREATE TABLE master.ui_spec (
	id character varying(36) NOT NULL,
	version numeric(5,3) NOT NULL,
	domain character varying(36) NOT NULL,
	title character varying(64) NOT NULL,
	description character varying(256) NOT NULL,
	type character varying(36) NOT NULL,
	json_spec character varying NOT NULL,
	identity_schema_id character varying(36) NOT NULL,
	identity_schema_version numeric(5,3) NOT NULL,
	effective_from timestamp,
	status_code character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT unq_dmn_type_vrsn_ischmid UNIQUE (domain,type,version,identity_schema_id),
	CONSTRAINT ui_spec_pk PRIMARY KEY (id)

);
-- ddl-end --
COMMENT ON TABLE master.ui_spec IS E'UI Specifications :  Stores UI Specifications with values used in application modules.';
-- ddl-end --
COMMENT ON CONSTRAINT unq_dmn_ttl_vrsn_ischmid ON master.ui_spec  IS E'Unique Constraint on domain,title,version,identity_schema_id';
-- ddl-end --
ALTER TABLE master.ui_spec OWNER TO sysadmin;
-- ddl-end --
