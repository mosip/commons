# helm/authmanager

Image `kernel-auth-service`; Istio `prefix` `/v1/authmanager`; port `8091`; health `/v1/authmanager/actuator/health`.

Align `Chart.yaml` `version` with `deploy/kernel/install.sh` `CHART_VERSION`.

Ban: change `/v1/authmanager` sans deploy/Istio; wget auth-adapter at container start (bake in image).
