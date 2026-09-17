# deploy/

Order: `conf-secrets` → `config-server` → `kernel` (idgenerator + notifier, NS `kernel`). `install.sh [kubeconfig]` · `restart.sh` · `delete.sh`. Pin `CHART_VERSION`. Smoke: `/config`, `/v1/notifier`, `/v1/idgenerator`, `/v1/ridgenerator`.

`conf-secrets/delete.sh` is destructive — not part of restart. Kernel delete does not remove `conf-secrets`. No salt Job, no hardcoded secrets, no separate RID/PRID releases.
