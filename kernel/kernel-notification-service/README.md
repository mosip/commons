# kernel-notification-service

SMS and email HTTP service for MOSIP.

Parent: [`../AGENTS.md`](../AGENTS.md) · Helm: [`../../helm/notifier`](../../helm/notifier) · Deploy: [`../../deploy/kernel`](../../deploy/kernel)

Stays a **separate JVM** from idgenerator. Do not change `/v1/notifier` without Helm/deploy.

---

## Table of contents

1. [At a glance](#at-a-glance)
2. [Prerequisites](#prerequisites)
3. [Default port and context path](#default-port-and-context-path)
4. [Local setup (Windows / Linux / macOS)](#local-setup-windows--linux--macos)
5. [Run from IDE](#run-from-ide)
6. [REST APIs](#rest-apis)
7. [Troubleshooting](#troubleshooting)
8. [How to run (config-server — production)](#how-to-run-config-server--production)
9. [Docker](#docker)
10. [Application properties](#application-properties)

---

## At a glance

| Item | Value |
|------|--------|
| Port | `8083` |
| Context path | `/v1/notifier` |
| Main class | `io.mosip.kernel.emailnotification.NotificationBootApplication` |
| Local profile | `local` |
| Production config | Spring Cloud Config (`application`, `kernel`) |
| Local properties | [`src/main/resources/application-local.properties`](src/main/resources/application-local.properties) |
| Windows script | [`run-local.bat`](run-local.bat) |
| Linux / macOS script | [`run-local.sh`](run-local.sh) |
| Maven deps | `kernel-auth-adapter`, `kernel-smsserviceprovider-msg91` (not Docker wget) |

**Quick health check (after start):**

| Purpose | URL |
|---------|-----|
| Health | http://localhost:8083/v1/notifier/actuator/health |
| Swagger UI | http://localhost:8083/v1/notifier/swagger-ui/index.html |
| OpenAPI | http://localhost:8083/v1/notifier/v3/api-docs |
| Email | `POST /v1/notifier/email/send` |
| SMS | `POST /v1/notifier/sms/send` |

---

## Prerequisites

| Tool | Version / notes |
|------|-----------------|
| JDK | **21** (`JAVA_HOME` set) |
| Maven | **3.9+** |
| OS | Windows 10/11, Linux, or macOS |
| Optional | `curl` (smoke checks) |
| Production | Config-server and [mosip-config](https://github.com/mosip/mosip-config); real SMTP / MSG91 only if proxy flags are off |

---

## Default port and context path

```properties
server.port=8083
server.servlet.path=/v1/notifier
spring.mvc.servlet.path=/v1/notifier
```

---

## Local setup (Windows / Linux / macOS)

Local mode uses profile **`local`**. Config is served from the classpath (`application-local.properties`). **No config-server, Git clone, SMTP, or SMS gateway is required.** SMS and email are **proxied** (`mosip.kernel.sms.proxy-sms=true`, `mosip.kernel.mail.proxy-mail=true`).

### Build once

From the `kernel/` parent:

```bash
cd kernel
mvn -pl kernel-notification-service -am clean package -DskipTests -Dgpg.skip=true -Dmaven.javadoc.skip=true
```

Or use `init` in the helper scripts below.

### Helper scripts

| Command | What it does |
|---------|----------------|
| `init` | Maven package this module |
| `start` | Start on port **8083** and wait until Spring Boot is ready |
| `smoke` | `GET /v1/notifier/actuator/health` |
| `stop` | Stop the process |
| `test` | Maven unit tests |
| `all` | `init` + `test` + `start` + `smoke` |

#### Windows (cmd)

```bat
cd kernel\kernel-notification-service
run-local.bat init
run-local.bat start
run-local.bat smoke
run-local.bat stop
```

Override port:

```bat
set NOTIFIER_PORT=8083
run-local.bat start
```

Logs / PID: `kernel-notification-service\.local\`

#### Linux / macOS / Git Bash

```bash
cd kernel/kernel-notification-service
chmod +x run-local.sh
./run-local.sh init
./run-local.sh start
./run-local.sh smoke
./run-local.sh stop
```

Override port:

```bash
NOTIFIER_PORT=8083 ./run-local.sh start
```

Logs / PID: `kernel-notification-service/.local/`

### Maven `spring-boot:run` (any OS)

```bash
cd kernel
mvn -pl kernel-notification-service -am spring-boot:run \
  -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local -Dserver.port=8083"
```

PowerShell (quote `-D`):

```powershell
cd kernel
mvn -pl kernel-notification-service -am spring-boot:run "-Dspring-boot.run.jvmArguments=-Dspring.profiles.active=local -Dserver.port=8083"
```

### Packaged JAR (any OS)

```bash
cd kernel/kernel-notification-service
java -Dspring.profiles.active=local -Dserver.port=8083 -jar target/kernel-notification-service-*.jar
```

Windows:

```bat
cd kernel\kernel-notification-service
java -Dspring.profiles.active=local -Dserver.port=8083 -jar target\kernel-notification-service-1.4.1-SNAPSHOT.jar
```

---

## Run from IDE

Import the **`kernel`** Maven project (open `commons/kernel` or load `kernel/pom.xml`). Use module **`kernel-notification-service`**.

**Shared settings (all IDEs):**

| Setting | Value |
|---------|--------|
| Main class | `io.mosip.kernel.emailnotification.NotificationBootApplication` |
| VM options | `-Dspring.profiles.active=local -Dserver.port=8083` |
| JDK | 21 |

### IntelliJ IDEA

1. **File → Open** → `commons/kernel` (or repo root, then open the `kernel` module).
2. Wait for Maven import; select **JDK 21**.
3. Open `NotificationBootApplication.java`.
4. **Run → Edit Configurations… → + → Application**
   - Name: `kernel-notification-service (local)`
   - Main class: `io.mosip.kernel.emailnotification.NotificationBootApplication`
   - Module: `kernel-notification-service`
   - VM options: `-Dspring.profiles.active=local -Dserver.port=8083`
   - Working directory: `$MODULE_DIR$` or `…/kernel/kernel-notification-service`
5. **Apply → Run / Debug**.

Optional: Maven tool window → `kernel-notification-service` → Plugins → `spring-boot` → `spring-boot:run` with the same JVM arguments under **Runner**.

### Eclipse / STS

1. **File → Import → Existing Maven Projects** → select `commons/kernel`.
2. Set **Java 21** (**Project → Properties → Java Build Path / Java Compiler**).
3. Right-click `NotificationBootApplication` → **Run As → Run Configurations… → Java Application**
   - Main class: `io.mosip.kernel.emailnotification.NotificationBootApplication`
   - **Arguments → VM arguments:** `-Dspring.profiles.active=local -Dserver.port=8083`
4. **Run / Debug**.

### VS Code / Cursor

1. Install **Extension Pack for Java** (and optionally **Spring Boot Extension Pack**).
2. Open the `commons` folder (or `kernel`).
3. Let Java import Maven; select **JDK 21** if prompted.
4. Add `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "kernel-notification-service (local)",
      "request": "launch",
      "mainClass": "io.mosip.kernel.emailnotification.NotificationBootApplication",
      "projectName": "kernel-notification-service",
      "cwd": "${workspaceFolder}/kernel/kernel-notification-service",
      "vmArgs": "-Dspring.profiles.active=local -Dserver.port=8083"
    }
  ]
}
```

5. **Run and Debug** → **kernel-notification-service (local)** → Start.

You can also use the integrated terminal with `run-local.bat` or `./run-local.sh`.

---

## REST APIs

Servlet path is `/v1/notifier`. Local profile uses proxy SMS/email, so these succeed without a gateway.

### Send email

```bash
curl -X POST "http://localhost:8083/v1/notifier/email/send" \
  -F "mailTo=user@example.com" \
  -F "mailSubject=hello" \
  -F "mailContent=test body"
```

Optional: `mailCc` (repeatable), `attachments` (files).

### Send SMS

```bash
curl -X POST "http://localhost:8083/v1/notifier/sms/send" \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"string\",\"version\":\"1.0\",\"requesttime\":\"2026-01-01T00:00:00.000Z\",\"request\":{\"number\":\"9999999999\",\"message\":\"hello\"}}"
```

### Notes

1. Production calls typically send an `Authorization` bearer token. Local profile sets `spring.security.method-security.enabled=false` and offline token validation so you can exercise the APIs without authmanager.
2. Do not merge this service into idgenerator. Keep `/v1/notifier` unless Helm/deploy is updated in the same change.

---

## Troubleshooting

| Symptom | What to do |
|---------|------------|
| `Port 8083 was already in use` | `run-local.bat stop` / `./run-local.sh stop`, or free the port |
| Wrong Java / build fails | Confirm `java -version` is 21; set `JAVA_HOME` |
| IDE cannot find main class | Reimport Maven; build `kernel-notification-service` first |
| Health not reachable | Wait for `Started NotificationBootApplication` in `.local/logs/notifier.log` |
| `Fetching config from server at : http://localhost:8888` | Profile `local` must be active so [`bootstrap-local.properties`](src/main/resources/bootstrap-local.properties) disables the config client. Rebuild after changing that file. |
| `required a bean of type 'io.micrometer.tracing.Tracer'` | Local profile sets `spring.sleuth.enabled=false`. Rebuild, or add that flag to VM options |
| Real SMS/email not sent locally | Expected: proxy flags are `true`. Turn them off and set SMTP/MSG91 only when you intend to send |
| Config-server required | Use a non-`local` profile and the section below |

---

## How to run (config-server — production)

1. Config-server must be reachable (cluster: install `conf-secrets` then config-server; local: see [`../kernel-config-server/README.md`](../kernel-config-server/README.md)).
2. Point the service at config-server. Indexes and names come from [mosip-config](https://github.com/mosip/mosip-config).

```bash
java -jar \
  -Dspring.profiles.active=<profile> \
  -Dspring.cloud.config.uri=<config-server-url> \
  -Dspring.cloud.config.label=<git-branch> \
  kernel-notification-service-1.4.1-SNAPSHOT.jar
```

Example against a local config-server:

```bash
java -jar \
  -Dspring.profiles.active=default \
  -Dspring.cloud.config.uri=http://localhost:51000/config \
  -Dspring.cloud.config.label=master \
  kernel-notification-service-1.4.1-SNAPSHOT.jar
```

`bootstrap.properties` already sets `spring.cloud.config.name=application,kernel` and `server.servlet.path=/v1/notifier`.

---

## Docker

Provide the following runtime arguments:

| # | Variable | Description |
|---|----------|-------------|
| 1 | `active_profile_env` | Spring profile (`dev`, `qa`, …) |
| 2 | `spring_config_url_env` | Config-server URL |
| 3 | `spring_config_label_env` | Config Git branch/label |
| 4 | `artifactory_url_env` | Optional artifactory (Glowroot, extras) |
| 5 | `is_glowroot_env` | Optional Glowroot enable flag |

Auth adapter and MSG91 provider are **Maven dependencies**, not Docker `wget` extras.

Example:

```bash
docker run --name=kernel-notification-service -d \
  -e active_profile_env=default \
  -e spring_config_url_env=http://config-server/config \
  -e spring_config_label_env=master \
  -p 8083:8083 \
  <name-of-docker-image-you-built>
```

Cluster install: [`deploy/kernel`](../../deploy/kernel) (`helm install notifier`). Probe path: `/v1/notifier/actuator/health`.

---

## Application properties

Local profile (excerpt). Full file: [`application-local.properties`](src/main/resources/application-local.properties). Config client is also turned off in [`bootstrap-local.properties`](src/main/resources/bootstrap-local.properties) so the default `http://localhost:8888` is never called.

```properties
server.port=8083
server.servlet.path=/v1/notifier
spring.mvc.servlet.path=/v1/notifier

spring.cloud.config.enabled=false
spring.cloud.config.fail-fast=false
spring.config.import=optional:configserver:
spring.sleuth.enabled=false

mosip.kernel.sms.proxy-sms=true
mosip.kernel.mail.proxy-mail=true
auth.server.admin.offline.token.validate=true
spring.security.method-security.enabled=false
```

Production `bootstrap.properties` (excerpt):

```properties
spring.cloud.config.name=application,kernel
spring.application.name=kernel-notification-service
server.port=8083
server.servlet.path=/v1/notifier
spring.mvc.servlet.path=${server.servlet.path}
management.endpoints.web.exposure.include=info,health,refresh,mappings,prometheus
```

For local development without config-server or real gateways, use [`application-local.properties`](src/main/resources/application-local.properties) and profile `local`.
