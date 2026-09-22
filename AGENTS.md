# Commons

JDK21 · Maven3.9+ · Boot **4.1.1**. `cd kernel && mvn clean install`. No `kernel-bom`.

DB: `mosip_kernel` (UIN/VID/OTP/assigned; PRID DDL→Pre-Reg). Keep `mosip_authdevice`/`mosip_regdevice` separate. Schema Δ → all `db_*`.

Ban: salt-gen; merge schemas; skip rollback/revoke; wipe `conf-secrets` on restart; path Δ `/v1/idgenerator` `/v1/ridgenerator` `/v1/notifier` `/v1/authmanager` `/config` w/o Helm/deploy; RID chart; PRID HTTP; re-split `kernel-auth-adapter`.

Auth in reactor: adapter (←openid-bridge) · authcodeflowproxy · auth-service. Chart `helm/authmanager` via `deploy/kernel`. Ext: [mosip-config](https://github.com/mosip/mosip-config) · msg91.
