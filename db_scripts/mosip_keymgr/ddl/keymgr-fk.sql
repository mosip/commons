-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_keymgr
-- Table Name   : 
-- Purpose      : All the FKs are created separately, not part of create table scripts to ease the deployment process
--           
-- Create By    : Sadanandegowda DM
-- Created Date : Sep-2020
-- 
-- Modified Date        Modified By         Comments / Remarks
-- ------------------------------------------------------------------------------------------
-- 
-- ------------------------------------------------------------------------------------------

-- object: fk_tsplkeym | type: CONSTRAINT --
-- ALTER TABLE keymgr.tsp_licensekey_map DROP CONSTRAINT IF EXISTS fk_tsplkeym CASCADE;
ALTER TABLE keymgr.tsp_licensekey_map ADD CONSTRAINT fk_tsplkeym FOREIGN KEY (license_key)
REFERENCES keymgr.licensekey_list (license_key) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_lkeyper | type: CONSTRAINT --
-- ALTER TABLE keymgr.licensekey_permission DROP CONSTRAINT IF EXISTS fk_lkeyper CASCADE;
ALTER TABLE keymgr.licensekey_permission ADD CONSTRAINT fk_lkeyper FOREIGN KEY (license_key)
REFERENCES keymgr.licensekey_list (license_key) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --
