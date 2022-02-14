CREATE DATABASE mosip_regdevice
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;
COMMENT ON DATABASE mosip_regdevice IS 'Database to store all registration device management data, look-up data, configuration data, metadata...etc.';

\c mosip_regdevice 

DROP SCHEMA IF EXISTS regdevice CASCADE;
CREATE SCHEMA regdevice;
ALTER SCHEMA regdevice OWNER TO postgres;

ALTER DATABASE mosip_regdevice SET search_path TO regdevice,pg_catalog,public;
