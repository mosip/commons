-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.user_detail
-- Purpose    	: User Detail : List of applicatgion users in the system, who can perform UIN registration functions as per roles assigned.
--           
-- Create By   	: Nasir Khan / Sadanandegowda
-- Created Date	: 15-Jul-2019
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted not null changes
-- Apr-2021		Ram Bhatt           Status_code, name and lang_code to be nullable
-- ------------------------------------------------------------------------------------------

-- object: master.user_detail | type: TABLE --
-- DROP TABLE IF EXISTS master.user_detail CASCADE;
CREATE TABLE master.user_detail(
	id character varying(256) NOT NULL,
	name character varying(64) ,
	status_code character varying(36),
	regcntr_id character varying(10),
	lang_code character varying(3) ,
	last_login_dtimes timestamp,
	last_login_method character varying(64),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_usrdtl_id PRIMARY KEY (id)

);
COMMENT ON TABLE master.user_detail IS 'User Detail : List of applicatgion users in the system, who can perform UIN registration functions as per roles assigned.';
COMMENT ON COLUMN master.user_detail.id IS 'User ID : Unique ID generated / assigned for a user';
COMMENT ON COLUMN master.user_detail.name IS 'Name : User name';
COMMENT ON COLUMN master.user_detail.status_code IS 'Status Code: User status. Refers to master.status_master.code';
COMMENT ON COLUMN master.user_detail.regcntr_id IS 'Registration Center ID : registration center id refers to master.registration_center.id';
COMMENT ON COLUMN master.user_detail.lang_code IS 'Language Code : For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language. ';
COMMENT ON COLUMN master.user_detail.last_login_dtimes IS 'Last Login Datetime: Date and time of the last login by the user';
COMMENT ON COLUMN master.user_detail.last_login_method IS 'Last Login Method: Previous login method in which the user logged into the system';
COMMENT ON COLUMN master.user_detail.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
COMMENT ON COLUMN master.user_detail.cr_by IS 'Created By : ID or name of the user who create / insert record';
COMMENT ON COLUMN master.user_detail.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN master.user_detail.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
COMMENT ON COLUMN master.user_detail.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN master.user_detail.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
COMMENT ON COLUMN master.user_detail.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';

