-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.applicant_login_detail
-- Purpose    	: User Detail : To store the applicant login details.
--           
-- Create By   	: Dhanendra
-- Created Date	: 01-Jul-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------

-- master.applicant_login_detail definition

-- Drop table

-- DROP TABLE master.applicant_login_detail;

CREATE TABLE master.applicant_login_detail (
	id varchar(64) NOT NULL,
	usr_id varchar(64) NOT NULL,
	login_date timestamp NULL,
	is_active bool NOT NULL,
	cr_by varchar(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by varchar(256) NULL,
	upd_dtimes timestamp NULL,
	is_deleted bool NULL,
	del_dtimes timestamp NULL,
	CONSTRAINT pk_usrlogdtl_id PRIMARY KEY (id)
);
-- ddl-end --
COMMENT ON COLUMN master.applicant_login_detail.id IS 'ID : id is system generated UUID';
-- ddl-end --
COMMENT ON COLUMN master.applicant_login_detail.usr_id IS 'User ID : User id refers to login user';
-- ddl-end --
COMMENT ON COLUMN master.applicant_login_detail.login_date IS ' Login Date: Date of the login by the user';
-- ddl-end --
COMMENT ON COLUMN master.applicant_login_detail.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN master.applicant_login_detail.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN master.applicant_login_detail.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN master.applicant_login_detail.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN master.applicant_login_detail.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN master.applicant_login_detail.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN master.applicant_login_detail.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --

