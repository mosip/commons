\c mosip_authdevice 

GRANT CONNECT
   ON DATABASE mosip_authdevice
   TO authdeviceuser;

GRANT USAGE
   ON SCHEMA authdevice
   TO authdeviceuser;

GRANT SELECT,INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES
   ON ALL TABLES IN SCHEMA authdevice
   TO authdeviceuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA authdevice 
	GRANT SELECT,INSERT,UPDATE,DELETE,REFERENCES ON TABLES TO authdeviceuser;

