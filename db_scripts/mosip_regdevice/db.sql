DROP DATABASE IF EXISTS mosip_regdevice;
CREATE DATABASE mosip_regdevice
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = sysadmin
	TEMPLATE  = template0;
-- ddl-end --
COMMENT ON DATABASE mosip_regdevice IS 'Database to store all registration device management data, look-up data, configuration data, metadata...etc.';
-- ddl-end --

\c mosip_regdevice sysadmin

-- object: regdevice | type: SCHEMA --
DROP SCHEMA IF EXISTS regdevice CASCADE;
CREATE SCHEMA regdevice;
-- ddl-end --
ALTER SCHEMA regdevice OWNER TO sysadmin;
-- ddl-end --

ALTER DATABASE mosip_regdevice SET search_path TO regdevice,pg_catalog,public;
-- ddl-end --

-- REVOKECONNECT ON DATABASE mosip_regdevice FROM PUBLIC;
-- REVOKEALL ON SCHEMA regdevice FROM PUBLIC;
-- REVOKEALL ON ALL TABLES IN SCHEMA regdevice FROM PUBLIC ;
