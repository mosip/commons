# helm/

Order: `conf-secrets` → `config-server` → `idgenerator` + `notifier`. Align `CHART_VERSION` with `deploy/*/install.sh`.

idgenerator image `kernel-idgenerator-service`; Istio `prefix` `/v1/idgenerator` + `extraPrefixes` `/v1/ridgenerator`. notifier image `kernel-notification-service`; `/v1/notifier`. config-server health `/config`; new placeholders in `_overrides.tpl` + `values.yaml` `overrides`.

Ban: `pridgenerator` / `ridgenerator` / `regproc-salt` charts; delete `conf-secrets` on upgrade; commit env secrets; change ports/health without probes.
