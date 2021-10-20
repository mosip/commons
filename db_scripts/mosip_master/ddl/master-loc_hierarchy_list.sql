-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.loc_hierarchy_list
-- Purpose    	: Location Hierarchy List: Master list of location hierarchy, Contains pre defined location hierarchy level, location hierarchy name in multiple language for the country.
--           
-- Create By   	: Sadanandegowda
-- Created Date	: 17-Apr-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted not null changes
-- ------------------------------------------------------------------------------------------

-- object: master.loc_hierarchy_list | type: TABLE --
-- DROP TABLE IF EXISTS master.loc_hierarchy_list CASCADE;
CREATE TABLE master.loc_hierarchy_list(
	hierarchy_level smallint NOT NULL,
	hierarchy_level_name character varying(64) NOT NULL,
	lang_code character varying(3) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_loc_hierlst PRIMARY KEY (hierarchy_level,hierarchy_level_name,lang_code)

);
-- ddl-end --
COMMENT ON TABLE master.loc_hierarchy_list IS 'Location Hierarchy List: Master list of location hierarchy, Contains pre defined location hierarchy level, location hierarchy name in multiple language for the country.';
-- ddl-end --
COMMENT ON COLUMN master.loc_hierarchy_list.hierarchy_level IS 'Hierarchy Level: Number of hierarchy levels defined by each country. Typically it starts with 0 for the topmost hierarchy level. This hierarchy level is referenced in location table';
-- ddl-end --
COMMENT ON COLUMN master.loc_hierarchy_list.hierarchy_level_name IS 'Hierarchy Level Name: Hierarchy level name defined by each country. for ex., COUNTRY->STATE->CITY->PINCODE. This hierarchy level name is referenced in location table';
-- ddl-end --
COMMENT ON COLUMN master.loc_hierarchy_list.lang_code IS 'Language Code : For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language. ';
-- ddl-end --
COMMENT ON COLUMN master.loc_hierarchy_list.is_active IS 'IS_Active : Flag to mark whether the record is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN master.loc_hierarchy_list.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN master.loc_hierarchy_list.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN master.loc_hierarchy_list.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN master.loc_hierarchy_list.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN master.loc_hierarchy_list.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN master.loc_hierarchy_list.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
