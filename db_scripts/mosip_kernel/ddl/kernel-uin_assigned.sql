

-- object: kernel.uin_assigned | type: TABLE --
-- DROP TABLE IF EXISTS kernel.uin_assigned CASCADE;
CREATE TABLE kernel.uin_assigned(
	uin character varying(28) NOT NULL,
	uin_status character varying(16),
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_uinass_id PRIMARY KEY (uin)

);
-- ddl-end --
COMMENT ON TABLE kernel.uin_assigned IS 'UIN: Stores pre-generated UINs that are assigned to an individual as part of registration process.';
-- ddl-end --
COMMENT ON COLUMN kernel.uin_assigned.uin IS 'UIN: Pre-generated UINs (Unique Identification Number), which will be used to assign to an individual';
-- ddl-end --
COMMENT ON COLUMN kernel.uin_assigned.uin_status IS 'Is Used: Status of the pre-generated UIN, whether it is assigned, unassigned or issued.';
-- ddl-end --
COMMENT ON COLUMN kernel.uin_assigned.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN kernel.uin_assigned.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN kernel.uin_assigned.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN kernel.uin_assigned.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN kernel.uin_assigned.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN kernel.uin_assigned.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --

