-- object: keymgruser | type: ROLE --
-- DROP ROLE IF EXISTS keymgruser;
CREATE ROLE keymgruser WITH 
	INHERIT
	LOGIN
	PASSWORD :dbuserpwd;
-- ddl-end --

