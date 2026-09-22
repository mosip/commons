# helm/

Order: `conf-secrets` → `config-server` → `authmanager`+`idgenerator`+`notifier`. Align `CHART_VERSION` w/ `deploy/*/install.sh`.

authmanager image `kernel-auth-service`; Istio `prefix` `/v1/authmanager` (from mosip-openid-bridge).
idgen image `kernel-idgenerator-service`; Istio `prefix` `/v1/idgenerator` + `extraPrefixes` `/v1/ridgenerator`. notifier image `kernel-notification-service`; `/v1/notifier`. config health `/config`; new placeholders → `_overrides.tpl` + `values.yaml` `overrides`.

Ban: `pridgenerator`/`ridgenerator`/`regproc-salt` charts; delete `conf-secrets` on upgrade; commit env secrets; change ports/health w/o probes.
