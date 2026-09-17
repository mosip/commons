# kernel-idgenerator-service

RID + UIN/VID HTTP service for MOSIP.

Parent: [`../AGENTS.md`](../AGENTS.md) · Helm: [`../../helm/idgenerator`](../../helm/idgenerator) · Deploy: [`../../deploy/kernel`](../../deploy/kernel)

Stays a **separate JVM** from notifier and config-server. Do not change `/v1/idgenerator` or `/v1/ridgenerator` without Helm/deploy. Do not reintroduce PRID HTTP (`/v1/pridgenerator`).

Local helper scripts match [`../kernel-notification-service`](../kernel-notification-service/README.md) (`init` / `start` / `smoke` / `stop` / `test` / `all`).

---

## Table of contents

1. [At a glance](#at-a-glance)
2. [Prerequisites](#prerequisites)
3. [Default port and context path](#default-port-and-context-path)
4. [Local setup (Windows / Linux / macOS)](#local-setup-windows--linux--macos)
5. [Run from IDE](#run-from-ide)
6. [REST APIs](#rest-apis)
7. [UIN generation filters](#uin-generation-filters)
8. [Troubleshooting](#troubleshooting)
9. [How to run (config-server — production)](#how-to-run-config-server--production)
10. [Docker](#docker)
11. [Application properties](#application-properties)

---

## At a glance

| Item | Value |
|------|--------|
| Port | `8080` |
| Context paths | `/v1/idgenerator` (UIN/VID), `/v1/ridgenerator` (RID) |
| Main class | `io.mosip.kernel.idgenerator.IDGeneratorVertxApplication` |
| Local profile | `local` |
| Production config | Spring Cloud Config (`application`, `kernel`) |
| Local properties | [`src/main/resources/application-local.properties`](src/main/resources/application-local.properties) |
| Windows script | [`run-local.bat`](run-local.bat) |
| Linux / macOS script | [`run-local.sh`](run-local.sh) |
| Local DB | In-memory H2 (`mosip_kernel`) |
| Maven deps | `kernel-core`, `kernel-auth-adapter` |

**Quick health check (after start):**

| Purpose | URL |
|---------|-----|
| Health | http://localhost:8080/v1/idgenerator/actuator/health |
| Metrics | http://localhost:8080/v1/idgenerator/metrics |
| UIN | `GET /v1/idgenerator/uin` |
| VID | `GET /v1/idgenerator/vid` |
| RID | `GET /v1/ridgenerator/generate/rid/{centerid}/{machineid}` |

HTTP starts **after** the local UIN/VID pools are filled. `start` waits up to 180s for that.

---

## Prerequisites

| Tool | Version / notes |
|------|-----------------|
| JDK | **21** (`JAVA_HOME` set) |
| Maven | **3.9+** |
| OS | Windows 10/11, Linux, or macOS |
| Optional | `curl` (smoke checks) |
| Production | Config-server, [mosip-config](https://github.com/mosip/mosip-config), and Postgres `mosip_kernel` |

---

## Default port and context path

```properties
server.port=8080
server.servlet.path=/v1/idgenerator
mosip.kernel.rid.servlet.path=/v1/ridgenerator
```

---

## Local setup (Windows / Linux / macOS)

Local mode uses profile **`local`**. Config is served from the classpath (`application-local.properties`). **No config-server, Git clone, or Postgres is required.** JDBC is in-memory H2; UIN/VID response signing is off.

### Build once

From the `kernel/` parent:

```bash
cd kernel
mvn -pl kernel-idgenerator-service -am clean package -DskipTests -Dgpg.skip=true -Dmaven.javadoc.skip=true
```

Or use `init` in the helper scripts below.

### Helper scripts

| Command | What it does |
|---------|----------------|
| `init` | Maven package this module |
| `start` | Start on port **8080** and wait until Vert.x HTTP is ready |
| `smoke` | `GET /v1/idgenerator/actuator/health` |
| `stop` | Stop the process |
| `test` | Maven unit tests |
| `all` | `init` + `test` + `start` + `smoke` |

#### Windows (cmd)

```bat
cd kernel\kernel-idgenerator-service
run-local.bat init
run-local.bat start
run-local.bat smoke
run-local.bat stop
```

Override port:

```bat
set IDGENERATOR_PORT=8080
run-local.bat start
```

Logs / PID: `kernel-idgenerator-service\.local\`

#### Linux / macOS / Git Bash

```bash
cd kernel/kernel-idgenerator-service
chmod +x run-local.sh
./run-local.sh init
./run-local.sh start
./run-local.sh smoke
./run-local.sh stop
```

Override port:

```bash
IDGENERATOR_PORT=8080 ./run-local.sh start
```

Logs / PID: `kernel-idgenerator-service/.local/`

### Maven `spring-boot:run` (any OS)

```bash
cd kernel
mvn -pl kernel-idgenerator-service -am spring-boot:run \
  -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local -Dserver.port=8080"
```

PowerShell (quote `-D`):

```powershell
cd kernel
mvn -pl kernel-idgenerator-service -am spring-boot:run "-Dspring-boot.run.jvmArguments=-Dspring.profiles.active=local -Dserver.port=8080"
```

### Packaged JAR (any OS)

```bash
cd kernel/kernel-idgenerator-service
java -Dspring.profiles.active=local -Dserver.port=8080 -jar target/kernel-idgenerator-service-*.jar
```

Windows:

```bat
cd kernel\kernel-idgenerator-service
java -Dspring.profiles.active=local -Dserver.port=8080 -jar target\kernel-idgenerator-service-1.4.1-SNAPSHOT.jar
```

---

## Run from IDE

Import the **`kernel`** Maven project (open `commons/kernel` or load `kernel/pom.xml`). Use module **`kernel-idgenerator-service`**.

**Shared settings (all IDEs):**

| Setting | Value |
|---------|--------|
| Main class | `io.mosip.kernel.idgenerator.IDGeneratorVertxApplication` |
| VM options | `-Dspring.profiles.active=local -Dserver.port=8080` |
| Working directory | `kernel/kernel-idgenerator-service` |
| JDK | 21 |

### IntelliJ IDEA

1. **File → Open** → `commons/kernel` (or repo root, then open the `kernel` module).
2. Wait for Maven import; select **JDK 21**.
3. Open `IDGeneratorVertxApplication.java`.
4. **Run → Edit Configurations… → + → Application**
   - Name: `kernel-idgenerator-service (local)`
   - Main class: `io.mosip.kernel.idgenerator.IDGeneratorVertxApplication`
   - Module: `kernel-idgenerator-service`
   - VM options: `-Dspring.profiles.active=local -Dserver.port=8080`
   - Working directory: `$MODULE_DIR$` or `…/kernel/kernel-idgenerator-service`
5. **Apply → Run / Debug**.

Optional: Maven tool window → `kernel-idgenerator-service` → Plugins → `spring-boot` → `spring-boot:run` with the same JVM arguments under **Runner**.

### Eclipse / STS

1. **File → Import → Existing Maven Projects** → select `commons/kernel`.
2. Set **Java 21** (**Project → Properties → Java Build Path / Java Compiler**).
3. Right-click `IDGeneratorVertxApplication` → **Run As → Run Configurations… → Java Application**
   - Main class: `io.mosip.kernel.idgenerator.IDGeneratorVertxApplication`
   - **Arguments → VM arguments:** `-Dspring.profiles.active=local -Dserver.port=8080`
   - **Arguments → Working directory:** `kernel/kernel-idgenerator-service`
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
      "name": "kernel-idgenerator-service (local)",
      "request": "launch",
      "mainClass": "io.mosip.kernel.idgenerator.IDGeneratorVertxApplication",
      "projectName": "kernel-idgenerator-service",
      "cwd": "${workspaceFolder}/kernel/kernel-idgenerator-service",
      "vmArgs": "-Dspring.profiles.active=local -Dserver.port=8080"
    }
  ]
}
```

5. **Run and Debug** → **kernel-idgenerator-service (local)** → Start.

You can also use the integrated terminal with `run-local.bat` or `./run-local.sh`.

---

## REST APIs

Vert.x serves both servlet paths from one process. Health does not require a token. UIN/VID/RID fetch routes expect MOSIP roles (`ID_REPOSITORY` / `REGISTRATION_PROCESSOR`) when the auth adapter is active.

### Health

```bash
curl -sS "http://localhost:8080/v1/idgenerator/actuator/health"
```

### Fetch UIN

```bash
curl -sS "http://localhost:8080/v1/idgenerator/uin"
```

### Fetch VID

```bash
curl -sS "http://localhost:8080/v1/idgenerator/vid"
```

Optional query: `videxpiry` as UTC `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'` (must not be in the past).

### Generate RID

```bash
curl -sS "http://localhost:8080/v1/ridgenerator/generate/rid/10001/10001"
```

Center id and machine id lengths follow `mosip.kernel.registrationcenterid.length` and `mosip.kernel.machineid.length` (both `5` locally).

### Notes

1. Production calls typically send an `Authorization` bearer token. Local profile sets `auth.server.admin.offline.token.validate=true` so you can start without authmanager; fetch APIs may still require a token depending on the adapter.
2. HTTP is bound only after the VID pool is initialized. Wait for `Deployed verticle HttpServerVerticle` in `.local/logs/idgenerator.log`.
3. Do not split RID into a second Boot app. Keep `/v1/idgenerator` and `/v1/ridgenerator` unless Helm/deploy is updated in the same change.

---

## UIN generation filters

MOSIP uses `SecureRandom` to generate UINs. A checksum is added using the Verhoeff algorithm. Generated UINs are filtered against the constraints below to eliminate easily identifiable numbers. The random number seed is refreshed every 45 minutes or as configured via `mosip.idgen.uin.secure-random-reinit-frequency` (minutes).

The UIN should follow:

* Only integers with length as specified in `mosip.kernel.uin.length`.
* Minimum unused UINs as specified in `mosip.kernel.uin.min-unused-threshold`. If not available then the next batch of generation starts.
* Number of UINs to generate as specified in `mosip.kernel.uin.uins-to-generate`.
* Upper bound of digits in sequence allowed, as specified in `mosip.kernel.uin.length.sequence-limit`. For example if limit is 3, then 12 is allowed but 123 is not (ascending or descending).
* Number of digits in a repeating block allowed, as specified in `mosip.kernel.uin.length.repeating-block-limit`. For example if limit is 2, then 4xxx4 is allowed but 48xxx48 is not.
* Lower bound of digits allowed between two repeating digits, as specified in `mosip.kernel.uin.length.repeating-limit`. For example if limit is 2, then 11 and 1x1 are not allowed.
* Reverse digits group limit, as specified in `mosip.kernel.uin.length.reverse-digits-limit`. For example if limit is 5 and UIN is 4345665434, then first 5 digits are 43456, reverse 65434.
* Digits group limit, as specified in `mosip.kernel.uin.length.digits-limit`. For example if limit is 5 and UIN is 4345643456, then the 5-digit group is 43456.
* Adjacent even digits limit, as specified in `mosip.kernel.uin.length.conjugative-even-digits-limit`. For example if limit is 3 then any 3 even adjacent digits are not allowed.
* Restricted numbers, comma-separated, as specified in `mosip.kernel.uin.restricted-numbers`.
* Numbers that must not be the starting digits, comma-separated, as specified in `mosip.kernel.uin.not-start-with`. For example the number should not start with `0` or `1`.
* No alphanumeric characters.
* No cyclic numbers such as `"142857"`, `"0588235294117647"`, `"052631578947368421"`, `"0434782608695652173913"`, `"0344827586206896551724137931"`, `"0212765957446808510638297872340425531914893617"`, `"0169491525423728813559322033898305084745762711864406779661"`, `"016393442622950819672131147540983606557377049180327868852459"`, `"010309278350515463917525773195876288659793814432989690721649484536082474226804123711340206185567"`.

Retain the stated values unless a product change asks otherwise.

---

## Troubleshooting

| Symptom | What to do |
|---------|------------|
| `Port 8080 was already in use` | `run-local.bat stop` / `./run-local.sh stop`, or free the port |
| Wrong Java / build fails | Confirm `java -version` is 21; set `JAVA_HOME` |
| IDE cannot find main class | Reimport Maven; build `kernel-idgenerator-service` first |
| Health not reachable | Wait for `Deployed verticle HttpServerVerticle` in `.local/logs/idgenerator.log` (pool fill can take a minute) |
| `Retrieving configuration from Spring-Config-Server` then a warn | Expected locally when `spring.cloud.config.uri` is unset; the process falls back to classpath `application-local.properties` |
| Fetch APIs return 401 | Send a MOSIP bearer token, or exercise health/metrics without auth |
| H2 / schema errors | Local profile uses in-memory H2 and `classpath:schema.sql`. Rebuild after changing that file |
| Config-server / Postgres required | Use a non-`local` profile and the section below |

---

## How to run (config-server — production)

1. Config-server must be reachable (cluster: install `conf-secrets` then config-server; local: see [`../kernel-config-server/README.md`](../kernel-config-server/README.md)).
2. Point the service at config-server and a Postgres `mosip_kernel` database. Indexes and names come from [mosip-config](https://github.com/mosip/mosip-config).

```bash
java -jar \
  -Dspring.profiles.active=<profile> \
  -Dspring.cloud.config.uri=<config-server-url> \
  -Dspring.cloud.config.label=<git-branch> \
  kernel-idgenerator-service-1.4.1-SNAPSHOT.jar
```

Example against a local config-server:

```bash
java -jar \
  -Dspring.profiles.active=default \
  -Dspring.cloud.config.uri=http://localhost:51000/config \
  -Dspring.cloud.config.label=master \
  kernel-idgenerator-service-1.4.1-SNAPSHOT.jar
```

`bootstrap.properties` already sets `spring.cloud.config.name=application,kernel`, `server.servlet.path=/v1/idgenerator`, and `mosip.kernel.rid.servlet.path=/v1/ridgenerator`.

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

`kernel-auth-adapter` is a Maven dependency (not Docker `wget` extras).

Example:

```bash
docker run --name=kernel-idgenerator-service -d \
  -e active_profile_env=default \
  -e spring_config_url_env=http://config-server/config \
  -e spring_config_label_env=master \
  -p 8080:8080 \
  <name-of-docker-image-you-built>
```

Cluster install: [`deploy/kernel`](../../deploy/kernel) (`helm install idgenerator`). Probe path: `/v1/idgenerator/actuator/health`.

---

## Application properties

Local profile (excerpt). Full file: [`application-local.properties`](src/main/resources/application-local.properties). Hibernate also loads [`bootstrap.properties`](src/main/resources/bootstrap.properties).

```properties
server.port=8080
server.servlet.path=/v1/idgenerator
mosip.kernel.rid.servlet.path=/v1/ridgenerator

javax.persistence.jdbc.driver=org.h2.Driver
id_database_url=jdbc:h2:mem:mosip_kernel;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:schema.sql'

spring.cloud.config.enabled=false
spring.sleuth.enabled=false
mosip.kernel.uin.response-signing-enable=false
mosip.kernel.vid.response-signing-enable=false
auth.server.admin.offline.token.validate=true
```

Production `bootstrap.properties` (excerpt):

```properties
spring.cloud.config.name=application,kernel
spring.application.name=kernel-idgenerator-service
server.port=8080
server.servlet.path=/v1/idgenerator
mosip.kernel.rid.servlet.path=/v1/ridgenerator
management.endpoints.web.exposure.include=info,health,refresh,mappings,prometheus
```

For local development without config-server or Postgres, use [`application-local.properties`](src/main/resources/application-local.properties) and profile `local`.
