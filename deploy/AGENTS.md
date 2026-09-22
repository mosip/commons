# deploy/

Order: conf-secrets → config-server → kernel (authmanager+idgen+notifier, NS `kernel`). `install|restart|delete.sh [kubeconfig]`. Pin `CHART_VERSION`.

Smoke: `/config` `/v1/authmanager` `/v1/notifier` `/v1/idgenerator` `/v1/ridgenerator`.

Ban: conf-secrets in restart/delete-kernel; salt Job; hardcoded secrets; split RID/PRID releases.
