-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.bulkupload_transaction
-- Purpose    	: Bulk Upload Transaction:  This transaction table to store all bulk upload transactions, This includes uploading master table data as well as packets uploads to the registration processor.
--           
-- Create By   	: Sadanandegowda DM
-- Created Date	: Aug-2019
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- Jan-2021		Ram Bhatt	    Set is_deleted flag to not null and default false
-- Mar-2021		Ram Bhatt	    Reverting is_deleted not null changes
-- Apr-2021		Ram Bhatt	    Removed bulk upload transaction size limit
-- ------------------------------------------------------------------------------------------
-- object: master.bulkupload_transaction | type: TABLE --
-- DROP TABLE IF EXISTS master.bulkupload_transaction CASCADE;
CREATE TABLE master.bulkupload_transaction(
	id character varying(36) NOT NULL,
	entity_name character varying(64) NOT NULL,
	upload_operation character varying(64) NOT NULL,
	status_code character varying(36) NOT NULL,
	record_count integer,
	uploaded_by character varying(256) NOT NULL,
	upload_category character varying(36),
	uploaded_dtimes timestamp NOT NULL,
	upload_description character varying,
	lang_code character varying(3) NOT NULL,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean DEFAULT FALSE,
	del_dtimes timestamp,
	CONSTRAINT pk_butrn_id PRIMARY KEY (id)

);
-- ddl-end --
COMMENT ON TABLE master.bulkupload_transaction IS 'Bulk Upload Transaction:  This transaction table to store all bulk upload transactions, This includes uploading master table data as well as packets uploads to the registration processor.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.id IS 'ID: Unigue ID is assign to bulk data upload transaction. Each transaction will be identifieds by this transaction id.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.entity_name IS 'Entity Name: Name of an entity for which data is beeing uploaded, This can be master data table name or packet in case of packet upload.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.upload_operation IS 'Upload Operation: Operation of the bulk upload example operations can be insert, update, delete and packet-upload.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.status_code IS 'Status Code: Status of the bulk upload transactions. For example... in-progress, success and failed transaction.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.record_count IS 'Record Count: Number of records and packets has been uploaded in a transaction.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.uploaded_by IS 'Uploaded By: The user detail of the person who is uploading the packets or master data.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.upload_category IS 'Upload Category: Upload category will be Master data csv or Packet';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.uploaded_dtimes IS 'Uploaded Date and Time: Date and time of the master data and packets are uploaded';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.upload_description IS 'Upload Description: Bulk data upload description, This will have all details about the transaction including upload failure or success messages.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.lang_code IS 'Language Code : For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.is_active IS 'IS_Active : Flag to mark whether the record/device is Active or In-active';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.cr_by IS 'Created By : ID or name of the user who create / insert record';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.cr_dtimes IS 'Created DateTimestamp : Date and Timestamp when the record is created/inserted';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.upd_by IS 'Updated By : ID or name of the user who update the record with new values';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.upd_dtimes IS 'Updated DateTimestamp : Date and Timestamp when any of the fields in the record is updated with new values.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.is_deleted IS 'IS_Deleted : Flag to mark whether the record is Soft deleted.';
-- ddl-end --
COMMENT ON COLUMN master.bulkupload_transaction.del_dtimes IS 'Deleted DateTimestamp : Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-- ddl-end --
