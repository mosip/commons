-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.user_detail_h
-- Purpose    	: User Detail History : This to track changes to master record whenever there is an INSERT/UPDATE/DELETE ( soft delete ), Effective DateTimestamp is used for identifying latest or point in time information. Refer master.user_detail table description for details.
--           
-- Create By   	: Nasir Khan / Sadanandegowda
-- Created Date	: 15-Jul-2019
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted not null changes
-- May-2021		Ram Bhatt	    Change LANG code TO NULLABLE AND remove from Primary Key
-- ------------------------------------------------------------------------------------------

-- object: master.user_detail_h | type: TABLE --
-- DROP TABLE IF EXISTS master.user_detail_h CASCADE;
CREATE TABLE master.user_detail_h(
	id character varying(256) NOT NULL,
	name character varying(64) NOT NULL,
	status_code character varying(36),
	regcntr_id character varying(10),
	lang_code character varying(3),
	last_login_dtimes timestamp,
	last_login_method character varying(64),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	eff_dtimes timestamp NOT NULL,
	CONSTRAINT pk_usrdtl_h_id PRIMARY KEY (id,eff_dtimes)

);
-- ddl-end --
COMMENT ON TABLE master.user_detail_h IS 'User Detail History : This to track changes to master record whenever there is an INSERT/UPDATE/DELETE ( soft delete ), Effective DateTimestamp is used for identifying latest or point in time information. Refer master.user_detail table description for details.';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.id IS 'User ID : Unique ID generated / assigned for a user';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.name IS 'Name : User name';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.status_code IS 'Status Code: User status. Refers to master.status_master.code';
-- ddl-end --
COMMENT ON COLUMN master.user_detail.regcntr_id IS 'Registration Center ID : registration center id refers to master.registration_center.id';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.lang_code IS 'Language Code : For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language. ';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.last_login_dtimes IS 'Last Login Datetime: Date and time of the last login by the user';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.last_login_method IS 'Last Login Method: Previous login method in which the user logged into the system';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
COMMENT ON COLUMN master.user_detail_h.eff_dtimes IS 'Effective Date Timestamp : This to track master record whenever there is an INSERT/UPDATE/DELETE ( soft delete ).  The current record is effective from this date-time. ';
-- ddl-end --

