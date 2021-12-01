-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.machine_master
-- Purpose    	: Machine Master : Contains list of approved Machines and  details,  like laptop, desktop, dongle etc used at registration centers. Valid Machines with active status only allowed at registration centers for respective functionalities. Machine onboarding are handled through admin application/portal by the user who is having the Machine onboarding authority.
--           
-- Create By   	: Nasir Khan / Sadanandegowda
-- Created Date	: 15-Jul-2019
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- 06-Apr-2020          Sadanandegowda      Nullable constraints and Datatype change
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted not null changes
-- Apr-2021		Ram Bhatt	    Lang_code nullable and/or removed from pk constraint
-- ------------------------------------------------------------------------------------------

-- object: master.machine_master | type: TABLE --
-- DROP TABLE IF EXISTS master.machine_master CASCADE;
CREATE TABLE master.machine_master(
	id character varying(10) NOT NULL,
	name character varying(64) NOT NULL,
	mac_address character varying(64),
	serial_num character varying(64),
	ip_address character varying(17),
	validity_end_dtimes timestamp,
	mspec_id character varying(36) NOT NULL,
	public_key character varying(1024) NOT NULL,
	key_index character varying(128),
	sign_public_key character varying(1024) NOT NULL,
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
	CONSTRAINT uq_machm UNIQUE (name,public_key,sign_public_key)

);
-- ddl-end --
COMMENT ON TABLE master.machine_master IS 'Machine Master : Contains list of approved Machines and  details,  like laptop, desktop, dongle etc used at registration centers. Valid Machines with active status only allowed at registration centers for respective functionalities. Machine onboarding are handled through admin application/portal by the user who is having the Machine onboarding authority. ';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.id IS 'Machine ID : Unique ID generated / assigned for machine';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.name IS 'Name : Machine name';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.mac_address IS 'Mac Address: Mac address of the machine';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.serial_num IS 'Serial Number: Serial number of the machine';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.ip_address IS 'IP Address: IP address of the machine';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.validity_end_dtimes IS 'Validity End Datetime: Machine validity expiry date';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.mspec_id IS 'Machine Specification ID : Machince specification id refers to master.machine_spec.id';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.public_key IS 'Public Key: Public key of the machine,  This will be Machine Identification TPM Endorsement key';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.key_index IS 'Key Index: Fingerprint[Unique Hash ]  for the TPM public key';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.sign_public_key IS 'Signed Public Key: Field for signature verification publicKey';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.sign_key_index IS 'Signed Key Index: Field for signature verification public key fingerprint';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.zone_code IS 'Zone Code : Unique zone code generated or entered by admin while creating zones, It is referred to master.zone.code. ';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.regcntr_id IS 'Registration Center ID : registration center id refers to master.registration_center.id';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.lang_code IS 'Language Code : For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language. ';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN master.machine_master.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
