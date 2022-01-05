CREATE TABLE master.machine_master(
    id character varying(10) NOT NULL,
    name character varying(64) NOT NULL,
    mac_address character varying(64),
    serial_num character varying(64),
    ip_address character varying(17),
    validity_end_dtimes timestamp,
    mspec_id character varying(36) NOT NULL,
    public_key character varying(1024),
    key_index character varying(128),
    sign_public_key character varying(1024),
    sign_key_index character varying(128),
    zone_code character varying(36) NOT NULL,
    regcntr_id character varying(10),
    lang_code character varying(3),
    is_active boolean NOT NULL,
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,
    is_deleted boolean DEFAULT FALSE,
    del_dtimes timestamp,
    CONSTRAINT pk_machm_id PRIMARY KEY (id),
    CONSTRAINT uq_machm_name UNIQUE (name),
    CONSTRAINT uq_machm_key_index UNIQUE (key_index),
    CONSTRAINT uq_machm_skey_index UNIQUE (sign_key_index)
);
CREATE INDEX IF NOT EXISTS idx_mac_master_cntr_id ON master.machine_master USING btree (regcntr_id);
CREATE INDEX IF NOT EXISTS idx_mac_master_cr_dtimes ON master.machine_master USING btree (cr_dtimes);
CREATE INDEX IF NOT EXISTS idx_mac_master_regcntr_id ON master.machine_master USING btree (regcntr_id);
COMMENT ON TABLE master.machine_master IS 'Machine Master : Contains list of approved Machines and  details,  like laptop, desktop, dongle etc used at registration centers. Valid Machines with active status only allowed at registration centers for respective functionalities. Machine onboarding are handled through admin application/portal by the user who is having the Machine onboarding authority. ';

COMMENT ON COLUMN master.machine_master.id IS 'Machine ID : Unique ID generated / assigned for machine';
COMMENT ON COLUMN master.machine_master.name IS 'Name : Machine name';
COMMENT ON COLUMN master.machine_master.mac_address IS 'Mac Address: Mac address of the machine';
COMMENT ON COLUMN master.machine_master.serial_num IS 'Serial Number: Serial number of the machine';
COMMENT ON COLUMN master.machine_master.ip_address IS 'IP Address: IP address of the machine';
COMMENT ON COLUMN master.machine_master.validity_end_dtimes IS 'Validity End Datetime: Machine validity expiry date';
COMMENT ON COLUMN master.machine_master.mspec_id IS 'Machine Specification ID : Machince specification id refers to master.machine_spec.id';
COMMENT ON COLUMN master.machine_master.public_key IS 'Public Key: Public key of the machine,  This will be Machine Identification TPM Endorsement key';
COMMENT ON COLUMN master.machine_master.key_index IS 'Key Index: Fingerprint[Unique Hash ]  for the TPM public key';
COMMENT ON COLUMN master.machine_master.sign_public_key IS 'Signed Public Key: Field for signature verification publicKey';
COMMENT ON COLUMN master.machine_master.sign_key_index IS 'Signed Key Index: Field for signature verification public key fingerprint';
COMMENT ON COLUMN master.machine_master.zone_code IS 'Zone Code : Unique zone code generated or entered by admin while creating zones, It is referred to master.zone.code. ';
COMMENT ON COLUMN master.machine_master.regcntr_id IS 'Registration Center ID : registration center id refers to master.registration_center.id';
COMMENT ON COLUMN master.machine_master.lang_code IS 'Language Code : For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language. ';
COMMENT ON COLUMN master.machine_master.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
COMMENT ON COLUMN master.machine_master.cr_by IS 'Created By : ID or name of the user who create / insert record';
COMMENT ON COLUMN master.machine_master.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN master.machine_master.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN master.machine_master.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN master.machine_master.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
COMMENT ON COLUMN master.machine_master.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
