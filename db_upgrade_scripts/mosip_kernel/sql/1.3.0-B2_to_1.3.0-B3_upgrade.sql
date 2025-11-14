-- UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES

CREATE INDEX idx_vid_status_not_deleted ON kernel.vid (vid_status) WHERE is_deleted = false;
CREATE INDEX CONCURRENTLY idx_vid_status_isdeleted ON kernel.vid (vid_status, is_deleted);

---END UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES--
