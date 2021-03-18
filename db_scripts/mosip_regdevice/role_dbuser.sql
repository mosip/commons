-- object: regdeviceuser | type: ROLE --
-- DROP ROLE IF EXISTS regdeviceuser;
CREATE ROLE regdeviceuser WITH 
	INHERIT
	LOGIN
	PASSWORD :dbuserpwd;
-- ddl-end --
