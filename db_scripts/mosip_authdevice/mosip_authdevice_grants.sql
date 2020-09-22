\c mosip_authdevice sysadmin


-- object: grant_b0ae4f0dce | type: PERMISSION --
GRANT CREATE,CONNECT,TEMPORARY
   ON DATABASE mosip_authdevice
   TO sysadmin;
-- ddl-end --

-- object: grant_99dd1cb062 | type: PERMISSION --
GRANT CREATE,CONNECT,TEMPORARY
   ON DATABASE mosip_authdevice
   TO appadmin;
-- ddl-end --

-- object: grant_18180691b7 | type: PERMISSION --
GRANT CONNECT
   ON DATABASE mosip_authdevice
   TO authdeviceuser;
-- ddl-end --

-- object: grant_3543fb6cf7 | type: PERMISSION --
GRANT CREATE,USAGE
   ON SCHEMA authdevice
   TO sysadmin;
-- ddl-end --

-- object: grant_8e1a2559ed | type: PERMISSION --
GRANT USAGE
   ON SCHEMA authdevice
   TO authdeviceuser;
-- ddl-end --

-- object: grant_8e1a2559ed | type: PERMISSION --
GRANT SELECT,INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES
   ON ALL TABLES IN SCHEMA authdevice
   TO authdeviceuser;
-- ddl-end --

ALTER DEFAULT PRIVILEGES IN SCHEMA authdevice 
	GRANT SELECT,INSERT,UPDATE,DELETE,REFERENCES ON TABLES TO authdeviceuser;


-- object: grant_78ed2da4ee | type: PERMISSION --
GRANT SELECT,INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES
   ON ALL TABLES IN SCHEMA authdevice
   TO appadmin;
-- ddl-end --
