--#INDEX--
CREATE INDEX IF NOT EXISTS idx_uin_status ON kernel.uin using btree(uin_status) where uin_status='UNUSED';
CREATE INDEX IF NOT EXISTS idx_uin_uin ON kernel.uin using btree(uin);