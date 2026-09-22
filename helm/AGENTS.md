# helm/

Order: conf-secrets → config-server → authmanager+idgenerator+notifier. Align `CHART_VERSION` w/ `deploy/*/install.sh`.

Images: authmanager→`kernel-auth-service` `/v1/authmanager`; idgen→`kernel-idgenerator-service` `/v1/idgenerator` + extra `/v1/ridgenerator`; notifier→`kernel-notification-service` `/v1/notifier`. Config health `/config`. New placeholders → `_overrides.tpl` + `values.yaml`.

Ban: pridgenerator/ridgenerator/regproc-salt charts; wipe conf-secrets on upgrade; commit env secrets; ports/health w/o probes.
