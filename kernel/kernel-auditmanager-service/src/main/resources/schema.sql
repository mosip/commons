-- create table section -------------------------------------------------
-- schema 		: audit				- Audit database schema 
-- table 		: app_audit_log		- Audit log
-- table alias  : adtl

-- schemas section -------------------------------------------------

-- create schema if master schema for Identity Issuance tables is not exists
create schema if not exists audit;

create schema if not exists kernel;

