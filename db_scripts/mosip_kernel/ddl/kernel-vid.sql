
-- object: kernel.vid | type: TABLE --
-- DROP TABLE IF EXISTS kernel.vid CASCADE;
CREATE TABLE kernel.vid(
	vid character varying(36) NOT NULL,
	expiry_dtimes timestamp,
	vid_status character varying(16) NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_vid_id PRIMARY KEY (vid)

);
-- ddl-end --
COMMENT ON TABLE kernel.vid IS 'VID: Stores pre-generated VIDs that are assigned to an individual as part of mosip process.';
-- ddl-end --
COMMENT ON COLUMN kernel.vid.vid IS 'VID: Pre-generated VIDs (Vertual Identification Number), which will be used to assign to an individual';
-- ddl-end --
COMMENT ON COLUMN kernel.vid.expiry_dtimes IS 'Expiry Date and Time: Expiry Date and Time of the Vertual ID';
-- ddl-end --
COMMENT ON COLUMN kernel.vid.vid_status IS 'VID: Status of the pre-generated VID, whether it is available, expired or assigned.';
-- ddl-end --
COMMENT ON COLUMN kernel.vid.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN kernel.vid.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN kernel.vid.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN kernel.vid.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN kernel.vid.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN kernel.vid.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --

-- PERFORMANCE OPTIMIZATION INDEXES
CREATE INDEX idx_vid_status_not_deleted ON kernel.vid (vid_status) WHERE is_deleted = false;
CREATE INDEX CONCURRENTLY idx_vid_status_isdeleted ON kernel.vid (vid_status, is_deleted);
