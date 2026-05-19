CREATE DATABASE :mosipdbname
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;
COMMENT ON DATABASE :mosipdbname IS 'Database to store all partner authentication device management data, look-up data, configuration data, metadata...etc.';

\c :mosipdbname

DROP SCHEMA IF EXISTS authdevice CASCADE;
CREATE SCHEMA authdevice;
ALTER SCHEMA authdevice OWNER TO postgres;

ALTER DATABASE :mosipdbname SET search_path TO authdevice,pg_catalog,public;
