CREATE DATABASE mosip_audit 
	ENCODING = 'UTF8' 
	LC_COLLATE = 'en_US.UTF-8' 
	LC_CTYPE = 'en_US.UTF-8' 
	TABLESPACE = pg_default 
	OWNER = postgres
	TEMPLATE  = template0;

COMMENT ON DATABASE mosip_audit IS 'Audit related logs and the data is stored in this database';

\c mosip_audit postgres

DROP SCHEMA IF EXISTS audit CASCADE;
CREATE SCHEMA audit;
ALTER SCHEMA audit OWNER TO postgres;
ALTER DATABASE mosip_audit SET search_path TO audit,pg_catalog,public;

