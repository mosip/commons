

-- object: regdevice.ftp_chip_detail | type: TABLE --
-- DROP TABLE IF EXISTS regdevice.ftp_chip_detail CASCADE;
CREATE TABLE regdevice.ftp_chip_detail(
	id character varying(36) NOT NULL,
	foundational_trust_provider_id character varying(36) NOT NULL,
	make character varying(36),
	model character varying(36),
	certificate_alias character varying(36),
	partner_org_name character varying(128),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_fcdtl_id PRIMARY KEY (id),
	CONSTRAINT uk_fcdtl_id UNIQUE (foundational_trust_provider_id,make,model)

);
-- ddl-end --
COMMENT ON TABLE regdevice.ftp_chip_detail IS 'Foundational Trust Provider Chip Details : To store all foundational trust provider chip details like make, model and certificate.';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.id IS 'Foundational Trust Provider ID: Unique ID of chip and chip details.';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.foundational_trust_provider_id IS 'Foundational Trust Provider ID: This is the partner id who provide chip and required certificates for L1 devices. This is soft referenced from Partner Management Service database.';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.make IS 'Chip Make: Make of the chip provided by the foundational trust provider';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.model IS 'Model : Model of the chip which is provided by the foundational trust provider';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.certificate_alias IS 'Certificate Alias : Its certificate alias which is stored in some key store and provided by MOSIP to a trust provider';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.partner_org_name IS 'Partner Organisation Name';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN regdevice.ftp_chip_detail.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
