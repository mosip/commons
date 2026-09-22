# Commons

JDK21 · Maven3.9+ · Boot **4.1.1**. `cd kernel && mvn clean install`. No `kernel-bom`.

DB: `mosip_kernel` (UIN/VID/OTP/assigned; PRID *DDL*→Pre-Reg). Keep `mosip_authdevice`/`mosip_regdevice` separate. Kernel schema Δ → all `db_*`.

Ban: salt-generator; merge schemas; skip rollback/revoke; delete `conf-secrets` on restart; change `/v1/idgenerator` `/v1/ridgenerator` `/v1/notifier` `/v1/authmanager` `/config` w/o Helm/deploy; RID chart; PRID HTTP.

Ext: [mosip-config](https://github.com/mosip/mosip-config) · msg91 SMS.

OpenID/auth (`kernel-auth-adapter` incl. former openid-bridge-api, `kernel-authcodeflowproxy-api`, `kernel-auth-service`) live in this `kernel/` reactor (moved from mosip-openid-bridge). Chart: `helm/authmanager`; install via `deploy/kernel`. Ban re-splitting the adapter out while commons services depend on it.
