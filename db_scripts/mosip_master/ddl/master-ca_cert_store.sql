-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_master
-- Table Name 	: master.ca_cert_store
-- Purpose    	: CA Cert Details : List of MOSIP Certificates.
--           
-- Create By   	: Ram Bhatt
-- Created Date	: Mar-2021
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------


-- object: master.ca_cert_store | type: TABLE --
-- DROP TABLE IF EXISTS master.ca_cert_store CASCADE;
CREATE TABLE master.ca_cert_store (
	cert_id character varying(36) NOT NULL,
	cert_subject character varying(500) NOT NULL,
	cert_issuer character varying(500) NOT NULL,
	issuer_id character varying(36) NOT NULL,
	cert_not_before timestamp,
	cert_not_after timestamp,
	crl_uri character varying(120),
	cert_data character varying,
	cert_thumbprint character varying(100),
	cert_serial_no character varying(50),
	partner_domain character varying(36),
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT ca_cert_store_pk PRIMARY KEY (cert_id),
	CONSTRAINT cert_thumbprint_unique UNIQUE (cert_thumbprint,partner_domain)

);

-- indexes section -------------------------------------------------
CREATE INDEX pk_cacs_id ON master.ca_cert_store USING btree (cert_id);

-- ddl-end --
ALTER TABLE master.ca_cert_store OWNER TO sysadmin;
-- ddl-end --


