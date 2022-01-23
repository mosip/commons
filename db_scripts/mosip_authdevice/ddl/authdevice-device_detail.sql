
-- object: authdevice.device_detail | type: TABLE --
-- DROP TABLE IF EXISTS authdevice.device_detail CASCADE;
CREATE TABLE authdevice.device_detail(
	id character varying(36) NOT NULL,
	dprovider_id character varying(36) NOT NULL,
	dtype_code character varying(36) NOT NULL,
	dstype_code character varying(36) NOT NULL,
	make character varying(36) NOT NULL,
	model character varying(36) NOT NULL,
	partner_org_name character varying(128),
	approval_status character varying(36) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_devdtl_id PRIMARY KEY (id),
	CONSTRAINT uk_devdtl_id UNIQUE (dprovider_id,dtype_code,dstype_code,make,model)

);
-- ddl-end --
COMMENT ON TABLE authdevice.device_detail IS 'Device Detail : Details of the device like device provider id, make , model, device type, device sub type, approval status are stored here.';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.id IS 'ID: Unigue service ID, Service ID is geerated by the MOSIP system';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.dprovider_id IS 'Device Provider ID : Device provider ID, Referenced from master.device_provider.id';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.dtype_code IS 'Device Type Code: Code of the device type, Referenced from master.reg_device_type.code';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.dstype_code IS ' Device Sub Type Code: Code of the device sub type, Referenced from master.reg_device_sub_type.code';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.make IS 'Make: Make of the device';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.model IS ' Model: Model of the device';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.partner_org_name IS 'Partner Organisation Name';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.approval_status IS 'Approval Status';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN authdevice.device_detail.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
