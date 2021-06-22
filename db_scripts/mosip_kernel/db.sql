CREATE DATABASE mosip_kernel 
	ENCODING = 'UTF8' 
	LC_COLLATE = 'en_US.UTF-8' 
	LC_CTYPE = 'en_US.UTF-8' 
	TABLESPACE = pg_default 
	OWNER = postgres
	TEMPLATE  = template0;

COMMENT ON DATABASE mosip_kernel IS 'Kernel related logs and the data is stored in this database';

\c mosip_kernel postgres

DROP SCHEMA IF EXISTS kernel CASCADE;
CREATE SCHEMA kernel;
ALTER SCHEMA kernel OWNER TO postgres;
ALTER DATABASE mosip_kernel SET search_path TO kernel,pg_catalog,public;

