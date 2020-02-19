-- create table section -------------------------------------------------
-- schema 		: audit				- Audit database schema 

-- schemas section -------------------------------------------------

-- create schema if master schema for Identity Issuance tables is not exists
create schema if not exists MASTER;
create schema if not exists KERNEL