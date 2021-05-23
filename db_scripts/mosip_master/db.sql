CREATE DATABASE mosip_master
	ENCODING = 'UTF8' 
	LC_COLLATE = 'en_US.UTF-8' 
	LC_CTYPE = 'en_US.UTF-8' 
	TABLESPACE = pg_default 
	OWNER = postgres
	TEMPLATE  = template0;

COMMENT ON DATABASE mosip_master IS 'Masterdata related logs and the data is stored in this database';

\c mosip_master postgres

DROP SCHEMA IF EXISTS master CASCADE;
CREATE SCHEMA master;
ALTER SCHEMA master OWNER TO postgres;
ALTER DATABASE mosip_master SET search_path TO master,pg_catalog,public;

