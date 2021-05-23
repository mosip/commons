CREATE DATABASE mosip_keymgr
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;
COMMENT ON DATABASE mosip_keymgr IS 'Key Manager database maintains common / system configurations, data related to key services like encryption, decryption keys, certificates..etc';

\c mosip_keymgr 

DROP SCHEMA IF EXISTS keymgr CASCADE;
CREATE SCHEMA keymgr;
ALTER SCHEMA keymgr OWNER TO postgres;

ALTER DATABASE mosip_keymgr SET search_path TO keymgr,pg_catalog,public;
