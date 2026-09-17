# Commons

JDK21 · Maven3.9+ · Boot **4.1.1**. `cd kernel && mvn clean install`. No `kernel-bom`.

DB: `mosip_kernel` (UIN/VID/OTP/assigned; PRID *DDL*→Pre-Reg). Keep `mosip_authdevice`/`mosip_regdevice` separate. Kernel schema Δ → all `db_*`.

Ban: salt-generator; merge schemas; skip rollback/revoke; delete `conf-secrets` on restart; change `/v1/idgenerator` `/v1/ridgenerator` `/v1/notifier` `/config` w/o Helm/deploy; RID chart; PRID HTTP.

Ext: [mosip-config](https://github.com/mosip/mosip-config) · `kernel-auth-adapter` · msg91 SMS.
