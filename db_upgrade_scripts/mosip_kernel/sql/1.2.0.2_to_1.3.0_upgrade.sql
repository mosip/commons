\c mosip_kernel

--#INDEX--
CREATE INDEX IF NOT EXISTS idx_uin_status ON kernel.uin using btree(uin_status) where uin_status='UNUSED';
CREATE INDEX IF NOT EXISTS idx_uin_uin ON kernel.uin using btree(uin);
CREATE INDEX IF NOT EXISTS idx_uin_status_all ON kernel.uin USING btree (uin_status);
CREATE INDEX IF NOT EXISTS idx_uin_status_crdtime ON kernel.uin USING btree (uin_status, cr_dtimes);
CREATE INDEX IF NOT EXISTS idx_uin_status_isdeleted ON kernel.uin USING btree (uin_status, is_deleted);
CREATE INDEX IF NOT EXISTS idx_vid_status_available_not_deleted ON kernel.vid USING btree (vid_status) WHERE vid_status = 'AVAILABLE' AND is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_vid_status_crdtime ON kernel.vid USING btree (vid_status, cr_dtimes);
CREATE INDEX IF NOT EXISTS idx_vid_status_unused ON kernel.vid USING btree (cr_dtimes) WHERE vid_status = 'UNUSED';

-- Below script required to upgrade from 1.3.0-beta.3 to 1.3.0.
-- UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES

CREATE INDEX idx_vid_status_not_deleted ON kernel.vid (vid_status) WHERE is_deleted = false;
CREATE INDEX CONCURRENTLY idx_vid_status_isdeleted ON kernel.vid (vid_status, is_deleted);

ALTER TABLE uin SET (autovacuum_vacuum_scale_factor = 0.05, autovacuum_vacuum_threshold = 1000, autovacuum_analyze_scale_factor = 0.03, autovacuum_analyze_threshold = 500);
ALTER TABLE vid SET (autovacuum_vacuum_scale_factor = 0.05, autovacuum_vacuum_threshold = 1000, autovacuum_analyze_scale_factor = 0.03, autovacuum_analyze_threshold = 500);

---END UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES--
