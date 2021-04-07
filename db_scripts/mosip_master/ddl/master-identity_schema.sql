-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.identity_schema
-- Purpose    	: Identity Schema: To store id schema which is used in MOSIP platform. All MOSIP modules will use this schema for fields representation and validation. ID schema will contain the key pair of the fields and values used and other meta information of schema in table.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: 23-Apr-2019
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted not null changes
-- ------------------------------------------------------------------------------------------
-- object: master.identity_schema | type: TABLE --
-- DROP TABLE IF EXISTS master.identity_schema CASCADE;
CREATE TABLE master.identity_schema(
	id character varying(36) NOT NULL,
	id_version numeric(5,3),
	title character varying(64),
	description character varying(256),
	id_attr_json character varying,
	schema_json character varying,
	status_code character varying(36),
	add_props boolean,
	effective_from timestamp,
	lang_code character varying(3) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_idsch_id PRIMARY KEY (id)

);
-- ddl-end --
COMMENT ON TABLE master.identity_schema IS 'Identity Schema: To store id schema which is used in MOSIP platform. All MOSIP modules will use this schema for fields representation and validation. ID schema will contain the key pair of the fields and values used and other meta information of schema in table. ';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.id IS 'ID: Unigue ID is assign to identity schema. Each identity schema will have its own unigue id.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.id_version IS 'ID Version: Version of the identity schema, versions will be used for validation of right version usage in the system.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.title IS 'Title: Tittle of the id schema, Gives high level detail about the id schema.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.description IS 'Description: Description of the id schema which will give more details about id schema.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.id_attr_json IS 'ID Attribute JSON: Field includes both UI related attributes as well as actual ID-Schema related attributes. It is acutal representation of POJO used by UI.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.schema_json IS 'Schema JSON: Its the ID-Schema json that is supported by JSON-Schema validator.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.status_code IS 'Status Code: Status of the identity schema. which gives details on the schema is in use, expired or outdated....etc.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.add_props IS 'Additional Properties: This boolean value represents weather additional property consideration is allowed or not.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.effective_from IS 'Effective From: Effective date and time of the id schema which can be used in MOSIP platform.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.lang_code IS 'Language Code : For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN master.identity_schema.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
