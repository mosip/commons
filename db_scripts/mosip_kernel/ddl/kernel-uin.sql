

-- object: kernel.uin | type: TABLE --
-- DROP TABLE IF EXISTS kernel.uin CASCADE;
CREATE TABLE kernel.uin(
	uin character varying(28) NOT NULL,
	uin_status character varying(16),
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_uin_id PRIMARY KEY (uin)

);
-- ddl-end --

--index section starts----
CREATE INDEX IF NOT EXISTS idx_uin_status ON kernel.uin using btree(uin_status) where uin_status='UNUSED';
CREATE INDEX IF NOT EXISTS idx_uin_uin ON kernel.uin using btree(uin);
CREATE INDEX IF NOT EXISTS idx_uin_status_all ON kernel.uin USING btree (uin_status);
CREATE INDEX IF NOT EXISTS idx_uin_status_crdtime ON kernel.uin USING btree (uin_status, cr_dtimes);
CREATE INDEX IF NOT EXISTS idx_uin_status_isdeleted ON kernel.uin USING btree (uin_status, is_deleted);
--index section ends------

COMMENT ON TABLE kernel.uin IS 'UIN: Stores pre-generated UINs that are assigned to an individual as part of registration process.';
-- ddl-end --
COMMENT ON COLUMN kernel.uin.uin IS 'UIN: Pre-generated UINs (Unique Identification Number), which will be used to assign to an individual';
-- ddl-end --
COMMENT ON COLUMN kernel.uin.uin_status IS 'Is Used: Status of the pre-generated UIN, whether it is assigned, unassigned or issued.';
-- ddl-end --
COMMENT ON COLUMN kernel.uin.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN kernel.uin.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN kernel.uin.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN kernel.uin.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN kernel.uin.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN kernel.uin.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --


-- autovacuum tuning section starts --
ALTER TABLE uin SET (autovacuum_vacuum_scale_factor = 0.05, autovacuum_vacuum_threshold = 1000, autovacuum_analyze_scale_factor = 0.03, autovacuum_analyze_threshold = 500);
-- autovacuum tuning section ends --