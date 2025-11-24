\c mosip_kernel

--#INDEX--
CREATE INDEX IF NOT EXISTS idx_uin_status ON kernel.uin using btree(uin_status) where uin_status='UNUSED';
CREATE INDEX IF NOT EXISTS idx_uin_uin ON kernel.uin using btree(uin);

-- Below script required to upgrade from 1.3.0-beta.3 to 1.3.0.
-- UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES

CREATE INDEX idx_vid_status_not_deleted ON kernel.vid (vid_status) WHERE is_deleted = false;
CREATE INDEX CONCURRENTLY idx_vid_status_isdeleted ON kernel.vid (vid_status, is_deleted);

---END UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES--
