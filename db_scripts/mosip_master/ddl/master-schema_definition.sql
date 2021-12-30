
-- object: master.schema_definition | type: TABLE --
-- DROP TABLE IF EXISTS master.schema_definition CASCADE;
CREATE TABLE master.schema_definition(
	id character varying(36) NOT NULL,
	def_type character varying(16),
	def_name character varying(36),
	add_props boolean,
	def_json character varying(4086),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_schdef_id PRIMARY KEY (id)

);
-- ddl-end --
COMMENT ON TABLE master.schema_definition IS 'Schema Definition: Defination of the id defination, Based on this schema defination id schema will be generated.';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.id IS 'ID: Unigue service ID, Service ID is geerated by the MOSIP system';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.def_type IS 'Defination Type: Schema defination type';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.def_name IS 'Definition Name: Name of the schema defination';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.add_props IS 'Additional Properties: This boolean value represents weather additional property consideration is allowed or not.';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.def_json IS 'ID Definition JSON: This holds the JSON text which will be used define ID schema.';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN master.schema_definition.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
