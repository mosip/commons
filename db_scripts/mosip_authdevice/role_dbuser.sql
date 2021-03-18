-- object: authdeviceuser | type: ROLE --
-- DROP ROLE IF EXISTS authdeviceuser;
CREATE ROLE authdeviceuser WITH 
	INHERIT
	LOGIN
	PASSWORD :dbuserpwd;
-- ddl-end --
