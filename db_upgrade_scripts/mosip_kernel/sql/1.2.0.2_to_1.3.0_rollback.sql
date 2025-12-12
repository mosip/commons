\c mosip_kernel

--#INDEX--
DROP INDEX IF EXISTS kernel.idx_uin_status;
DROP INDEX IF EXISTS kernel.idx_uin_uin;

-- Below script required to rollback from 1.3.0-beta.3 to 1.3.0.
-- ROLLBACK FOR PERFORMANCE OPTIMIZATION INDEXES

DROP INDEX IF EXISTS kernel.idx_vid_status_not_deleted;

DROP INDEX CONCURRENTLY IF EXISTS kernel.idx_vid_status_isdeleted;
DROP INDEX IF EXISTS kernel.idx_uin_status_all;
DROP INDEX IF EXISTS kernel.idx_uin_status_crdtime;
DROP INDEX IF EXISTS kernel.idx_uin_status_isdeleted;
DROP INDEX IF EXISTS kernel.idx_vid_status_available_not_deleted;
DROP INDEX IF EXISTS kernel.idx_vid_status_crdtime;
DROP INDEX IF EXISTS kernel.idx_vid_status_unused;


ALTER TABLE uin RESET (autovacuum_vacuum_scale_factor, autovacuum_vacuum_threshold, autovacuum_analyze_scale_factor, autovacuum_analyze_threshold);

ALTER TABLE vid RESET (autovacuum_vacuum_scale_factor, autovacuum_vacuum_threshold, autovacuum_analyze_scale_factor, autovacuum_analyze_threshold);

-- END ROLLBACK FOR PERFORMANCE OPTIMIZATION INDEXES
