\c mosip_kernel

--#INDEX--
DROP INDEX IF EXISTS idx_uin_status ON kernel.uin;
DROP INDEX IF EXISTS idx_uin_uin ON kernel.uin;

-- Below script required to rollback from 1.3.0-beta.3 to 1.3.0.
-- ROLLBACK FOR PERFORMANCE OPTIMIZATION INDEXES

DROP INDEX IF EXISTS kernel.idx_vid_status_not_deleted;

DROP INDEX CONCURRENTLY IF EXISTS kernel.idx_vid_status_isdeleted;

-- END ROLLBACK FOR PERFORMANCE OPTIMIZATION INDEXES
