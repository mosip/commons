DROP DATABASE IF EXISTS mosip_keymgr;
CREATE DATABASE mosip_keymgr
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = sysadmin
	TEMPLATE  = template0;
-- ddl-end --
COMMENT ON DATABASE mosip_keymgr IS 'Key Manager database maintains common / system configurations, data related to key services like encryption, decryption keys, certificates..etc';
-- ddl-end --

\c mosip_keymgr sysadmin

-- object: keymgr | type: SCHEMA --
DROP SCHEMA IF EXISTS keymgr CASCADE;
CREATE SCHEMA keymgr;
-- ddl-end --
ALTER SCHEMA keymgr OWNER TO sysadmin;
-- ddl-end --

ALTER DATABASE mosip_keymgr SET search_path TO keymgr,pg_catalog,public;
-- ddl-end --

-- REVOKECONNECT ON DATABASE mosip_keymgr FROM PUBLIC;
-- REVOKEALL ON SCHEMA keymgr FROM PUBLIC;
-- REVOKEALL ON ALL TABLES IN SCHEMA keymgr FROM PUBLIC ;
