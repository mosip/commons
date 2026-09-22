# Kernel Auth Service

[![Maven Package upon a push](https://github.com/mosip/mosip-openid-bridge/actions/workflows/push-trigger.yml/badge.svg?branch=develop)](https://github.com/mosip/mosip-openid-bridge/actions/workflows/push-trigger.yml)

## Overview

**Kernel Auth Service** is the MOSIP auth manager HTTP process. It issues and validates tokens against Keycloak (or a compatible IdP) for MOSIP modules.

Parent: [`../README.md`](../README.md) · Helm: [`../../helm/authmanager`](../../helm/authmanager/README.md) · Deploy: [`../../deploy`](../../deploy/README.md)

Do not change `/v1/authmanager` without Helm/deploy. Boot 4 uses ZIP layout. Embedded server is Tomcat.

---

## At a glance

| Item | Value |
|------|--------|
| Port | `8091` |
| Context path | `/v1/authmanager` |
| Main class | `io.mosip.kernel.auth.AuthBootApplication` |
| IAM | `KeycloakImpl` |
| Image | `kernel-auth-service` |
| Maven deps | commons `kernel-core`, reactor `kernel-auth-adapter` (includes OpenID bridge API; unpacked at package time) |

**Quick health check (after start):**

| Purpose | URL |
|---------|-----|
| Health | http://localhost:8091/v1/authmanager/actuator/health |
| Swagger UI | http://localhost:8091/v1/authmanager/swagger-ui/index.html |
| OpenAPI | http://localhost:8091/v1/authmanager/v3/api-docs |

---

## Features

- Generate token using userid and password
- Generate token using client-id and secret key
- Validate token
- Refresh token
- Invalidate token on expiry
- OTP and UIN-backed flows where configured

---

## Database

Identity is Keycloak. There are no MOSIP schema scripts in this module.

---

# Local Setup

## Prerequisites
- **JDK:** 21
- **Maven:** 3.9+
- **Docker:** Latest (optional)
- **Keycloak/IDP:** Required for a full run
- **Config Server** with MOSIP property files
- commons **`kernel-core`** installed first

### Configuration

Uses files in [mosip-config](https://github.com/mosip/mosip-config/tree/master). Refer to the tagged version for your release.
- [application-default.properties](https://github.com/mosip/mosip-config/blob/master/application-default.properties)
- [kernel-default.properties](https://github.com/mosip/mosip-config/blob/master/kernel-default.properties)

## Installation

### Local Setup (for Development or Contribution)

1. Config server must be running. See the [MOSIP Config Server Setup Guide](https://docs.mosip.io/1.2.0/modules/registration-processor/registration-processor-developers-guide#environment-setup).

2. From `kernel/` (PowerShell: quote `-D`):

```text
mvn -pl kernel-auth-service -am clean install -Dmaven.javadoc.skip=true "-Dgpg.skip=true"
```

3. Start with `run-local.sh` / `run-local.bat`. **Do not** copy cluster GC flags (`UseZGC`, …) onto the command line. Cluster injects them as `JDK_JAVA_OPTIONS` (Helm `additionalResources.javaOpts`). Local JDK 21 defaults are enough.

### Helper scripts

| Command | What it does |
|---------|----------------|
| `init` | Maven package this module (skip tests) |
| `start` | Start on port **8091** and wait until Spring Boot is ready |
| `smoke` | `GET /v1/authmanager/actuator/health` and Swagger UI |
| `stop` | Stop the process (frees the Boot ZIP for `mvn clean`) |
| `test` | Maven unit tests |
| `all` | `init` + `test` + `start` + `smoke` |
| `docker` | `init`, docker build, run |

Logs / PID: `kernel-auth-service/.local/`

#### Windows (cmd)

```bat
cd kernel\kernel-auth-service
run-local.bat init
run-local.bat start
run-local.bat smoke
run-local.bat stop
```

Override port:

```bat
set PORT=8091
run-local.bat start
```

#### Linux / macOS / Git Bash

```bash
cd kernel/kernel-auth-service
chmod +x run-local.sh
./run-local.sh init
./run-local.sh start
./run-local.sh smoke
./run-local.sh stop
```

Override port:

```bash
PORT=8091 ./run-local.sh start
```

**Manual `java`:**

| OS | Command |
|----|---------|
| Linux / macOS / WSL / Git Bash | `java -Dspring.profiles.active=local -jar target/kernel-auth-service-<$version>.jar` |
| Windows cmd | `java -Dspring.profiles.active=local -jar target\kernel-auth-service-<$version>.jar` |

Config server (scripts also read these env vars):

| OS | |
|----|--|
| bash | `export SPRING_CLOUD_CONFIG_URI=http://localhost:51000` then `./run-local.sh start` |
| cmd | `set SPRING_CLOUD_CONFIG_URI=http://localhost:51000` then `run-local.bat start` |

Optional heap only (not cluster ZGC):

| OS | |
|----|--|
| bash | `export JDK_JAVA_OPTIONS="-Xms512M -Xmx512M"` |
| cmd | `set JDK_JAVA_OPTIONS=-Xms512M -Xmx512M` |

**IDE** — main class `io.mosip.kernel.auth.AuthBootApplication`. Leave VM/GC flags empty.

| IDE | Run |
|-----|-----|
| IntelliJ IDEA | Open `kernel/`, Run the main class. Active profiles: `local`. Env `JDK_JAVA_OPTIONS` only for heap. |
| Eclipse | Run As → Java Application. VM arguments empty, or `-Dspring.profiles.active=local`. |
| VS Code / Cursor | Run/Debug the main class. `launch.json`: `"mainClass": "io.mosip.kernel.auth.AuthBootApplication"`, `"env": { "SPRING_PROFILES_ACTIVE": "local" }`. |
| NetBeans | Run File on `AuthBootApplication`. VM Options empty. |

4. Verify `http://localhost:8091/v1/authmanager/actuator/health`.

### Local Setup with Docker

GC/heap still come from `JDK_JAVA_OPTIONS` (optional). Pass config as `active_profile_env`, `spring_config_url_env`, `spring_config_label_env`. Adapter is in the image; do not wget `kernel-auth-adapter.jar`. Use `host.docker.internal` for a config server on the host (Linux: `--add-host=host.docker.internal:host-gateway`).

**Script:**

```text
SPRING_CLOUD_CONFIG_URI=http://host.docker.internal:51000 ./run-local.sh docker
```

```text
set SPRING_CLOUD_CONFIG_URI=http://host.docker.internal:51000
run-local.bat docker
```

**Manual build:**

```text
cd kernel/kernel-auth-service
docker build -t kernel-auth-service .
```

Linux:

```text
docker run --rm -p 8091:8091 --name kernel-auth-service --add-host=host.docker.internal:host-gateway -e active_profile_env=local -e spring_config_url_env=http://host.docker.internal:51000 -e spring_config_label_env=master kernel-auth-service
```

macOS / Windows Docker Desktop:

```text
docker run --rm -p 8091:8091 --name kernel-auth-service -e active_profile_env=local -e spring_config_url_env=http://host.docker.internal:51000 -e spring_config_label_env=master kernel-auth-service
```

Hub: `docker pull mosipid/kernel-auth-service:<$version>` then the same `docker run` with that image. Optional: `-e JDK_JAVA_OPTIONS="-Xms512M -Xmx512M"`.

---

## Deployment

### Kubernetes

Cluster install: [`deploy/`](../../deploy/README.md) (`./install.sh [kubeconfig]`). Chart [`helm/authmanager`](../../helm/authmanager/README.md). Sandbox: [v3 installation](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).

---

## Documentation

### API Documentation

[MOSIP Kernel Authentication Manager Service](https://mosip.github.io/documentation/1.2.0/kernel-authentication-manager-service.html)

### Product Documentation

[OpenID Bridge developer guide](https://docs.mosip.io/1.2.0/modules/commons/openid-bridge-developer-guide)

---

## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/mosip-openid-bridge/issues)

---

## License

This project is licensed under the [Mozilla Public License 2.0](../../LICENSE).
