

-- object: keymgr.licensekey_permission | type: TABLE --
-- DROP TABLE IF EXISTS keymgr.licensekey_permission CASCADE;
CREATE TABLE keymgr.licensekey_permission(
	license_key character varying(255) NOT NULL,
	permission character varying(512),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_lkeyper PRIMARY KEY (license_key)

);
-- ddl-end --
COMMENT ON TABLE keymgr.licensekey_permission IS 'Permissions: List of permissions associated with license key';
-- ddl-end --
COMMENT ON COLUMN keymgr.licensekey_permission.license_key IS 'License Key : License key which is mapped to TSP, This will refer to master.licensekey_list.license_key';
-- ddl-end --
COMMENT ON COLUMN keymgr.licensekey_permission.permission IS 'Permission: List of authentication permission which are assigned to license key';
-- ddl-end --
COMMENT ON COLUMN keymgr.licensekey_permission.is_active IS 'Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN keymgr.licensekey_permission.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN keymgr.licensekey_permission.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN keymgr.licensekey_permission.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN keymgr.licensekey_permission.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN keymgr.licensekey_permission.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN keymgr.licensekey_permission.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE''';
-- ddl-end --
