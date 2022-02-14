CREATE DATABASE mosip_authdevice
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;
COMMENT ON DATABASE mosip_authdevice IS 'Database to store all partner authentication device management data, look-up data, configuration data, metadata...etc.';

\c mosip_authdevice

DROP SCHEMA IF EXISTS authdevice CASCADE;
CREATE SCHEMA authdevice;
ALTER SCHEMA authdevice OWNER TO postgres;

ALTER DATABASE mosip_authdevice SET search_path TO authdevice,pg_catalog,public;
