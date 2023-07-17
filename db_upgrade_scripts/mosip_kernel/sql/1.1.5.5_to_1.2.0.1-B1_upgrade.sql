\c mosip_kernel

REASSIGN OWNED BY sysadmin TO postgres;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA kernel FROM kerneluser;

REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA kernel FROM sysadmin;

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE ON ALL TABLES IN SCHEMA kernel TO kerneluser;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA kernel TO postgres;


CREATE INDEX IF NOT EXISTS idx_prid_status
    ON kernel.prid USING btree
    (prid_status COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
