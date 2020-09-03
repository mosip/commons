DROP DATABASE IF EXISTS mosip_authdevice;
CREATE DATABASE mosip_authdevice
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = sysadmin
	TEMPLATE  = template0;
-- ddl-end --
COMMENT ON DATABASE mosip_authdevice IS 'Database to store all partner authentication device management data, look-up data, configuration data, metadata...etc.';
-- ddl-end --

\c mosip_authdevice sysadmin

-- object: authdevice | type: SCHEMA --
DROP SCHEMA IF EXISTS authdevice CASCADE;
CREATE SCHEMA authdevice;
-- ddl-end --
ALTER SCHEMA authdevice OWNER TO sysadmin;
-- ddl-end --

ALTER DATABASE mosip_authdevice SET search_path TO authdevice,pg_catalog,public;
-- ddl-end --

-- REVOKECONNECT ON DATABASE mosip_authdevice FROM PUBLIC;
-- REVOKEALL ON SCHEMA authdevice FROM PUBLIC;
-- REVOKEALL ON ALL TABLES IN SCHEMA authdevice FROM PUBLIC ;
