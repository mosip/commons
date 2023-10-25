\echo 'Upgrade Queries not required for transition from $CURRENT_VERSION to $UPGRADE_VERSION'

ALTER TABLE kernel.otp_transaction ALTER COLUMN otp TYPE varchar(64);