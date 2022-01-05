ALTER TABLE master.app_authentication_method ADD CONSTRAINT fk_appauthm_appdtl FOREIGN KEY (app_id,lang_code)
REFERENCES master.app_detail (id,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.app_authentication_method ADD CONSTRAINT fk_appauthm_prclst FOREIGN KEY (process_id,lang_code)
REFERENCES master.process_list (id,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.app_authentication_method ADD CONSTRAINT fk_appauthm_rolelst FOREIGN KEY (role_code,lang_code)
REFERENCES master.role_list (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.app_role_priority ADD CONSTRAINT fk_roleprt_appdtl FOREIGN KEY (app_id,lang_code)
REFERENCES master.app_detail (id,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.app_role_priority ADD CONSTRAINT fk_roleprt_prclst FOREIGN KEY (process_id,lang_code)
REFERENCES master.process_list (id,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.app_role_priority ADD CONSTRAINT fk_roleprt_rolelst FOREIGN KEY (role_code,lang_code)
REFERENCES master.role_list (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.loc_holiday ADD CONSTRAINT fk_lochol_loc FOREIGN KEY (location_code,lang_code)
REFERENCES master.location (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.applicant_valid_document ADD CONSTRAINT fk_avaldoc_doctyp FOREIGN KEY (doctyp_code,lang_code)
REFERENCES master.doc_type (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.applicant_valid_document ADD CONSTRAINT fk_avaldoc_doccat FOREIGN KEY (doccat_code,lang_code)
REFERENCES master.doc_category (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.biometric_attribute ADD CONSTRAINT fk_bmattr_bmtyp FOREIGN KEY (bmtyp_code)
REFERENCES master.biometric_type (code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.device_master ADD CONSTRAINT fk_devicem_dspec FOREIGN KEY (dspec_id)
REFERENCES master.device_spec (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.device_spec ADD CONSTRAINT fk_dspec_dtyp FOREIGN KEY (dtyp_code)
REFERENCES master.device_type (code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.machine_master ADD CONSTRAINT fk_machm_mspec FOREIGN KEY (mspec_id)
REFERENCES master.machine_spec (id) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.machine_spec ADD CONSTRAINT fk_mspec_mtyp FOREIGN KEY (mtyp_code)
REFERENCES master.machine_type (code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.reason_list ADD CONSTRAINT fk_rsnlst_rsncat FOREIGN KEY (rsncat_code,lang_code)
REFERENCES master.reason_category (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.registration_center ADD CONSTRAINT fk_regcntr_cntrtyp FOREIGN KEY (cntrtyp_code,lang_code)
REFERENCES master.reg_center_type (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.registration_center ADD CONSTRAINT fk_regcntr_loc FOREIGN KEY (location_code,lang_code)
REFERENCES master.location (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.registration_center ADD CONSTRAINT fk_regcntr_zone FOREIGN KEY (zone_code,lang_code)
REFERENCES master.zone (code,lang_code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.screen_authorization ADD CONSTRAINT fk_scrauth_scrdtl FOREIGN KEY (screen_id,lang_code)
REFERENCES master.screen_detail (id,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.screen_authorization ADD CONSTRAINT fk_scrauth_rolelst FOREIGN KEY (role_code,lang_code)
REFERENCES master.role_list (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.screen_detail ADD CONSTRAINT fk_scrdtl_appdtl FOREIGN KEY (app_id,lang_code)
REFERENCES master.app_detail (id,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.status_list ADD CONSTRAINT fk_status_sttyp FOREIGN KEY (sttyp_code,lang_code)
REFERENCES master.status_type (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.template ADD CONSTRAINT fk_tmplt_tffmt FOREIGN KEY (file_format_code)
REFERENCES master.template_file_format (code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.valid_document ADD CONSTRAINT fk_valdoc_doctyp FOREIGN KEY (doctyp_code,lang_code)
REFERENCES master.doc_type (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.valid_document ADD CONSTRAINT fk_valdoc_doccat FOREIGN KEY (doccat_code,lang_code)
REFERENCES master.doc_category (code,lang_code) MATCH SIMPLE
ON DELETE NO ACTION ON UPDATE NO ACTION;

ALTER TABLE master.location ADD CONSTRAINT fk_loc_lochierlst FOREIGN KEY (hierarchy_level,hierarchy_level_name,lang_code)
REFERENCES master.loc_hierarchy_list (hierarchy_level,hierarchy_level_name,lang_code) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
