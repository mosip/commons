CREATE DATABASE :mosipdbname
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;

COMMENT ON DATABASE :mosipdbname IS 'Kernel related logs and the data is stored in this database';

\c :mosipdbname postgres

DROP SCHEMA IF EXISTS kernel CASCADE;
CREATE SCHEMA kernel;
ALTER SCHEMA kernel OWNER TO postgres;
ALTER DATABASE :mosipdbname SET search_path TO kernel,pg_catalog,public;
