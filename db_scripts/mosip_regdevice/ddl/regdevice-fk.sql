

-- Foreign Key Constraints Same DB/Schema tables.

-- object: fk_devdtl_id | type: CONSTRAINT --
-- ALTER TABLE regdevice.device_detail DROP CONSTRAINT IF EXISTS fk_devdtl_id CASCADE;
ALTER TABLE regdevice.device_detail ADD CONSTRAINT fk_devdtl_id FOREIGN KEY (dtype_code,dstype_code)
REFERENCES regdevice.reg_device_sub_type (dtyp_code,code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_rdstyp_dtype_code | type: CONSTRAINT --
-- ALTER TABLE regdevice.reg_device_sub_type DROP CONSTRAINT IF EXISTS fk_rdstyp_dtype_code CASCADE;
ALTER TABLE regdevice.reg_device_sub_type ADD CONSTRAINT fk_rdstyp_dtype_code FOREIGN KEY (dtyp_code)
REFERENCES regdevice.reg_device_type (code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_regdevm_ftpid | type: CONSTRAINT --
-- ALTER TABLE regdevice.registered_device_master DROP CONSTRAINT IF EXISTS fk_regdevm_ftpid CASCADE;
ALTER TABLE regdevice.registered_device_master ADD CONSTRAINT fk_regdevm_ftpid FOREIGN KEY (foundational_trust_provider_id)
REFERENCES regdevice.foundational_trust_provider (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --


-- object: fk_regdevm | type: CONSTRAINT --
-- ALTER TABLE regdevice.registered_device_master DROP CONSTRAINT IF EXISTS fk_regdevm CASCADE;
ALTER TABLE regdevice.registered_device_master ADD CONSTRAINT fk_regdevm FOREIGN KEY (device_detail_id)
REFERENCES regdevice.device_detail (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_sbi_id | type: CONSTRAINT --
-- ALTER TABLE regdevice.secure_biometric_interface DROP CONSTRAINT IF EXISTS fk_sbi_id CASCADE;
--ALTER TABLE regdevice.secure_biometric_interface ADD CONSTRAINT fk_sbi_id FOREIGN KEY (device_detail_id)
--REFERENCES regdevice.device_detail (id) MATCH FULL
--ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: fk_fcdtl_frpid | type: CONSTRAINT --
-- ALTER TABLE regdevice.ftp_chip_detail DROP CONSTRAINT IF EXISTS fk_fcdtl_frpid CASCADE;
ALTER TABLE regdevice.ftp_chip_detail ADD CONSTRAINT fk_fcdtl_frpid FOREIGN KEY (foundational_trust_provider_id)
REFERENCES regdevice.foundational_trust_provider (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --
