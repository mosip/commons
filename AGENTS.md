# Commons

JDK 21 · Maven 3.9+ · Boot **4.1.1**. `cd kernel && mvn clean install`. No `kernel-bom`.

DBs: `mosip_kernel` (UIN/VID/OTP/assigned; PRID *DDL* for Pre-Reg). Keep `mosip_authdevice` / `mosip_regdevice` separate. Kernel schema change → `db_scripts` + `db_upgrade_scripts` + `db_release_scripts`.

Ban: salt-generator; merge schemas; skip rollback/revoke; delete `conf-secrets` on config restart; change `/v1/idgenerator`, `/v1/ridgenerator`, `/v1/notifier`, `/config` without Helm/deploy; RID chart; PRID HTTP (`/v1/pridgenerator`).

Config: [mosip-config](https://github.com/mosip/mosip-config). Auth: `kernel-auth-adapter`. SMS: `kernel-smsserviceprovider-msg91` (notifier).
