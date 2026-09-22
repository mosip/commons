# deploy/

Order: `conf-secrets` → `config-server` → `kernel` (authmanager+idgenerator+notifier, NS `kernel`). `install.sh [kubeconfig]` · `restart.sh` · `delete.sh`. Pin `CHART_VERSION`. Smoke: `/config` `/v1/authmanager` `/v1/notifier` `/v1/idgenerator` `/v1/ridgenerator`.

`conf-secrets/delete.sh` destructive — not part of restart. Kernel delete ≠ remove `conf-secrets`. No salt Job, no hardcoded secrets, no separate RID/PRID releases.
