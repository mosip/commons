-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.blocklisted_words
-- Purpose    	: Block Listed Words : List of words that are block listed.
--           
-- Create By   	: Ram Bhatt
-- Created Date	: Jul-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------

-- object: master.blocklisted_words | type: TABLE --
-- DROP TABLE IF EXISTS master.blocklisted_words CASCADE;
CREATE TABLE master.blocklisted_words(
	word character varying(128) NOT NULL,
	descr character varying(256),
	lang_code character varying(3),
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_blwrd_code PRIMARY KEY (word)

);
-- ddl-end --
COMMENT ON TABLE master.blocklisted_words IS 'Black Listed Words : List of words that are black listed.';
-- ddl-end --
COMMENT ON COLUMN master.blocklisted_words.word IS 'Word: Word that is blocklisted by the system';
-- ddl-end --
COMMENT ON COLUMN master.blocklisted_words.descr IS 'Description : Description of word blocklisted';
-- ddl-end --
COMMENT ON COLUMN master.blocklisted_words.lang_code IS 'Language Code : For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language. ';
-- ddl-end --
COMMENT ON COLUMN master.blocklisted_words.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN master.blocklisted_words.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN master.blocklisted_words.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN master.blocklisted_words.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN master.blocklisted_words.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN master.blocklisted_words.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN master.blocklisted_words.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --

