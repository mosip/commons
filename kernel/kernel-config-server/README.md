# kernel-config-server

Spring Cloud Config Server for all MOSIP microservices.

[Background & Design](https://github.com/mosip/mosip/wiki/MOSIP-Configuration-Server) · Parent: [`../AGENTS.md`](../AGENTS.md) · Helm: [`../../helm/config-server`](../../helm/config-server) · Deploy: [`../../deploy/config-server`](../../deploy/config-server)

Install after `conf-secrets`; those secrets are durable. Do not merge into other kernel services or change `/config` without Helm/deploy. Local helper scripts match [`../kernel-notification-service`](../kernel-notification-service/README.md) (`init` / `start` / `smoke` / `stop` / `test` / `all`).

---

## Table of contents

1. [At a glance](#at-a-glance)
2. [Prerequisites](#prerequisites)
3. [Default port and context path](#default-port-and-context-path)
4. [Local setup (Windows / Linux / macOS)](#local-setup-windows--linux--macos)
5. [Run from IDE](#run-from-ide)
6. [Optional: real mosip-config folder](#optional-real-mosip-config-folder)
7. [Troubleshooting](#troubleshooting)
8. [Encryption / decryption of properties](#encryption--decryption-of-properties)
9. [How to run (Git / composite — production)](#how-to-run-git--composite--production)
10. [Docker](#docker)
11. [Encrypt and decrypt APIs](#encrypt-and-decrypt-apis)
12. [Application properties](#application-properties)
13. [Config hierarchy](#config-hierarchy)
14. [Config client (other MOSIP services)](#config-client-other-mosip-services)
15. [Cloud config backends](#cloud-config-backends)

---

## At a glance

| Item | Value |
|------|--------|
| Port | `51000` |
| Context path | `/config` |
| Main class | `io.mosip.kernel.config.server.ConfigServerBootApplication` |
| Local profiles | `local,native` |
| Production profile | `composite` (Git) |
| Local properties | [`src/main/resources/application-local.properties`](src/main/resources/application-local.properties) |
| Windows script | [`run-local.bat`](run-local.bat) |
| Linux / macOS script | [`run-local.sh`](run-local.sh) |

**Quick health check (after start):**

| Purpose | URL |
|---------|-----|
| Health | http://localhost:51000/config/actuator/health |
| Sample local config | http://localhost:51000/config/application/local |
| Pattern | `http://localhost:51000/config/{application}/{profile}` |

---

## Prerequisites

| Tool | Version / notes |
|------|-----------------|
| JDK | **21** (`JAVA_HOME` set) |
| Maven | **3.9+** |
| OS | Windows 10/11, Linux, or macOS |
| Optional | `curl` (smoke checks); [mosip-config](https://github.com/mosip/mosip-config) clone for real configs |
| Production Git mode | SSH keys under `${user.home}/.ssh` |

---

## Default port and context path

```properties
server.port=51000
server.servlet.path=/config
```

---

## Local setup (Windows / Linux / macOS)

Local mode uses profiles **`local,native`**. Config is served from the classpath (`application-local.properties`). **No Git clone and no SSH keys are required.**

### Build once

From the `kernel/` parent:

```bash
cd kernel
mvn -pl kernel-config-server -am clean package -DskipTests -Dgpg.skip=true -Dmaven.javadoc.skip=true
```

Or use `init` in the helper scripts below.

### Helper scripts

| Command | What it does |
|---------|----------------|
| `init` | Maven package this module |
| `start` | Start on port **51000** and wait until Spring Boot is ready |
| `smoke` | `GET /config/actuator/health` |
| `stop` | Stop the process |
| `test` | Maven unit tests |
| `all` | `init` + `test` + `start` + `smoke` |

#### Windows (cmd)

```bat
cd kernel\kernel-config-server
run-local.bat init
run-local.bat start
run-local.bat smoke
run-local.bat stop
```

Override port:

```bat
set CONFIG_SERVER_PORT=51000
run-local.bat start
```

Logs / PID: `kernel-config-server\.local\`

#### Linux / macOS / Git Bash

```bash
cd kernel/kernel-config-server
chmod +x run-local.sh
./run-local.sh init
./run-local.sh start
./run-local.sh smoke
./run-local.sh stop
```

Override port:

```bash
CONFIG_SERVER_PORT=51000 ./run-local.sh start
```

Logs / PID: `kernel-config-server/.local/`

### Maven `spring-boot:run` (any OS)

```bash
cd kernel
mvn -pl kernel-config-server -am spring-boot:run \
  -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local,native -Dserver.port=51000"
```

PowerShell (quote `-D`):

```powershell
cd kernel
mvn -pl kernel-config-server -am spring-boot:run "-Dspring-boot.run.jvmArguments=-Dspring.profiles.active=local,native -Dserver.port=51000"
```

### Packaged JAR (any OS)

```bash
cd kernel/kernel-config-server
java -Dspring.profiles.active=local,native -Dserver.port=51000 -jar target/kernel-config-server-*.jar
```

Windows:

```bat
cd kernel\kernel-config-server
java -Dspring.profiles.active=local,native -Dserver.port=51000 -jar target\kernel-config-server-1.4.1-SNAPSHOT.jar
```

---

## Run from IDE

Import the **`kernel`** Maven project (open `commons/kernel` or load `kernel/pom.xml`). Use module **`kernel-config-server`**.

**Shared settings (all IDEs):**

| Setting | Value |
|---------|--------|
| Main class | `io.mosip.kernel.config.server.ConfigServerBootApplication` |
| VM options | `-Dspring.profiles.active=local,native -Dserver.port=51000` |
| JDK | 21 |

### IntelliJ IDEA

1. **File → Open** → `commons/kernel` (or repo root, then open the `kernel` module).
2. Wait for Maven import; select **JDK 21**.
3. Open `ConfigServerBootApplication.java`.
4. **Run → Edit Configurations… → + → Application**
   - Name: `kernel-config-server (local)`
   - Main class: `io.mosip.kernel.config.server.ConfigServerBootApplication`
   - Module: `kernel-config-server`
   - VM options: `-Dspring.profiles.active=local,native -Dserver.port=51000`
   - Working directory: `$MODULE_DIR$` or `…/kernel/kernel-config-server`
5. **Apply → Run / Debug**.

Optional: Maven tool window → `kernel-config-server` → Plugins → `spring-boot` → `spring-boot:run` with the same JVM arguments under **Runner**.

### Eclipse / STS

1. **File → Import → Existing Maven Projects** → select `commons/kernel`.
2. Set **Java 21** (**Project → Properties → Java Build Path / Java Compiler**).
3. Right-click `ConfigServerBootApplication` → **Run As → Run Configurations… → Java Application**
   - Main class: `io.mosip.kernel.config.server.ConfigServerBootApplication`
   - **Arguments → VM arguments:** `-Dspring.profiles.active=local,native -Dserver.port=51000`
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
      "name": "kernel-config-server (local)",
      "request": "launch",
      "mainClass": "io.mosip.kernel.config.server.ConfigServerBootApplication",
      "projectName": "kernel-config-server",
      "cwd": "${workspaceFolder}/kernel/kernel-config-server",
      "vmArgs": "-Dspring.profiles.active=local,native -Dserver.port=51000"
    }
  ]
}
```

5. **Run and Debug** → **kernel-config-server (local)** → Start.

You can also use the integrated terminal with `run-local.bat` or `./run-local.sh`.

---

## Optional: real mosip-config folder

Point native search at a local [mosip-config](https://github.com/mosip/mosip-config) `config` directory:

**Windows**

```bat
set MOSIP_CONFIG_DIR=D:\path\to\mosip-config\config
run-local.bat start
```

**Linux / macOS**

```bash
export MOSIP_CONFIG_DIR=/path/to/mosip-config/config
./run-local.sh start
```

**IDE VM options (add):**

```text
-Dspring.cloud.config.server.native.search-locations=file:/path/to/mosip-config/config,classpath:/
```

On Windows use forward slashes, e.g. `file:D:/mosip/mosip-config/config,classpath:/`.

---

## Troubleshooting

| Symptom | What to do |
|---------|------------|
| `Port 51000 was already in use` | `run-local.bat stop` / `./run-local.sh stop`, or free the port |
| Wrong Java / build fails | Confirm `java -version` is 21; set `JAVA_HOME` |
| IDE cannot find main class | Reimport Maven; build `kernel-config-server` first |
| Health not reachable | Wait for `Started ConfigServerBootApplication` in `.local/logs/config-server.log` |
| Need Git-backed config | Use profile `composite` and the sections below (not `local,native`) |

---

## Encryption / decryption of properties

Create a keystore:

```bash
keytool -genkeypair -alias <your-alias> -keyalg RSA -keystore server.keystore \
  -storepass <store-password> \
  -dname "CN=<your-CN>,OU=<OU>,O=<O>,L=<L>,S=<S>,C=<C>"
```

When prompted for the password for `<your-alias>`, choose a password or press Enter to reuse the store password.

The JKS keystore uses a proprietary format. It is recommended to migrate to **PKCS12**:

```bash
keytool -importkeystore -srckeystore server.keystore -destkeystore server.keystore -deststoretype pkcs12
```

More information: [Spring Cloud Config — creating a key store for testing](https://cloud.spring.io/spring-cloud-config/single/spring-cloud-config.html#_creating_a_key_store_for_testing)

---

## How to run (Git / composite — production)

To run with Git-backed configuration:

1. Configure SSH keys for Git (default location `${user.home}/.ssh`).
2. Set environment variables for the **composite** profile. Indexes `0`, `1`, … are list items. If a property exists in multiple repositories, **repo at index 0 has higher priority**.

```bash
export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI=<git-repo-ssh-url>
export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=git
export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_DEFAULT_LABEL=<branch-to-refer>

export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_URI=<git-repo-ssh-url>
export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_TYPE=git
export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_DEFAULT_LABEL=<branch-to-refer>
```

3. Run the JAR (encryption keystore passed **at runtime**):

```bash
java -jar \
  -Dencrypt.keyStore.location=file:///<file-location-of-keystore> \
  -Dencrypt.keyStore.password=<keystore-password> \
  -Dencrypt.keyStore.alias=<keystore-alias> \
  -Dencrypt.keyStore.secret=<keystore-secret> \
  <jar-name>
```

Example:

```bash
java -jar \
  -Dencrypt.keyStore.location=file:///opt/keys/server.keystore \
  -Dencrypt.keyStore.password=<password> \
  -Dencrypt.keyStore.alias=<alias> \
  -Dencrypt.keyStore.secret=<secret> \
  kernel-config-server-1.4.1-SNAPSHOT.jar
```

---

## Docker

Provide the following runtime arguments:

| # | Variable | Description |
|---|----------|-------------|
| 1 | `SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI` | URL of your Git repo |
| 2 | `SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE` | Repo type (`git`) |
| 3 | `SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_DEFAULT_LABEL` | Branch to refer (defaults to `main` if omitted) |
| 4 | `SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_SEARCH_PATHS` | Folder inside the Git repo that contains configuration |
| 5 | `encrypt_keyStore_location_env` | Encrypt keystore location |
| 6 | `encrypt_keyStore_password_env` | Encryption keystore password |
| 7 | `encrypt_keyStore_alias_env` | Encryption keystore alias |
| 8 | `encrypt_keyStore_secret_env` | Encryption keystore secret |

Example:

```bash
docker run --name=<name-the-container> -d \
  -v <location-of-encrypt-keystore>/server.keystore:<mount-keystore-location-inside-container>/server.keystore:z \
  -v /home/madmin/<location-of-folder-containing-git-ssh-keys>:<mount-ssh-location-inside-container>/.ssh:z \
  -e SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI=<git_ssh_url_env> \
  -e SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=git \
  -e SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_DEFAULT_LABEL=<branch-for-repo> \
  -e encrypt_keyStore_location_env=file:///<mount-keystore-location-inside-container>/server.keystore \
  -e encrypt_keyStore_password_env=<encrypt_keyStore_password_env> \
  -e encrypt_keyStore_alias_env=<encrypt_keyStore_alias_env> \
  -e encrypt_keyStore_secret_env=<encrypt_keyStore_secret_env> \
  -p 51000:51000 \
  <name-of-docker-image-you-built>
```

---

## Encrypt and decrypt APIs

### Encrypt any property

```bash
curl http://<your-config-server-address>/<application-context-path-if-any>/encrypt -d <value-to-encrypt>
```

Example:

```bash
curl http://localhost:51000/config/encrypt -d mySecretValue
```

Place the encrypted value in the client application properties file as:

```properties
password={cipher}<encrypted-value>
```

### Decrypt any property manually

```bash
curl http://<your-config-server-address>/<application-context-path-if-any>/decrypt -d <encrypted-value-to-decrypt>
```

Example:

```bash
curl http://localhost:51000/config/decrypt -d <encrypted-value>
```

### Notes

1. There is no need to write a decryption mechanism in client applications for encrypted values. They are automatically decrypted by the config server.
2. Config server does not support asterisk (`*`) in URLs as older Java clients sometimes did. Provide the complete URL when fetching a config file.
   - Old: `http://<config-server-url>/*/profile/label/<path-to-file>`
   - New: `http://<config-server-url>/application/profile/label/<path-to-file>`
3. Config server does not automatically trim whitespace at the end of a property value. Avoid accidental trailing spaces.

---

## Application properties

```properties
# Port where mosip spring cloud config server needs to run
server.port=51000

# Adding context path
server.servlet.path=/config

spring.profiles.active=composite

# Server returns HTTP 404 if the application is not found. By default this flag is true.
spring.cloud.config.server.accept-empty=false

## As spring.profiles.active is composite, use env variables to provide values for git configuration as below
##########################
## Git repository location where configuration files are stored
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI=<your-git-repository-URL>

## Type of repository: git, svn, native
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=git

## Branch/label to refer in config repository
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_DEFAULT_LABEL=<your-git-repository-branch>

# Spring Cloud Config Server clones the remote git repository. If the local copy gets
# dirty (e.g. folder content changed by an OS process), it cannot update from remote.
# For force-pull in such cases, set the flag to true.
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_FORCE_PULL=true

# Refresh rate in seconds so config server checks Git for updates (lower for production if needed).
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_REFRESH_RATE=60

# Clone on start of server instead of on first request
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_CLONE_ON_START=true

# Path inside the GIT repo where config files are stored (often a config directory)
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_SEARCH_PATHS=<folder-in-git-repository-containing-configuration>

# Disable health endpoint to improve performance of config server while in development
# health.config.enabled=false

# For encryption of properties
###########################################
# pass at runtime
#encrypt.keyStore.location=file:///<your-encryption-keyStore-path>
#encrypt.keyStore.password=<your-encryption-keyStore-password>
#encrypt.keyStore.alias=<your-encryption-keyStore-alias>
#encrypt.keyStore.secret=<your-encryption-keyStore-secret>
```

Pass encrypt settings on the JVM when starting (same keys as above):

```bash
java -jar \
  -Dencrypt.keyStore.location=file:///<your-encryption-keyStore-path> \
  -Dencrypt.keyStore.password=<your-encryption-keyStore-password> \
  -Dencrypt.keyStore.alias=<your-encryption-keyStore-alias> \
  -Dencrypt.keyStore.secret=<your-encryption-keyStore-secret> \
  kernel-config-server-*.jar
```

For local development without a keystore or Git, use [`application-local.properties`](src/main/resources/application-local.properties) and profiles `local,native`.

---

## Config hierarchy

![Config Properties](../../docs/design/kernel/_images/GlobalProperties_1.jpg)

---

## Config client (other MOSIP services)

### Maven dependency

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-config</artifactId>
  <version>${spring-cloud-config.version}</version>
</dependency>
```

### Config client `bootstrap.properties`

```properties
spring.cloud.config.uri=http://<config-host-url>:<config-port>
spring.cloud.config.label=<git-branch>
spring.application.name=<application-name>
spring.cloud.config.name=<property-file-to-pick-up-configuration-from>
spring.profiles.active=composite
management.endpoints.web.exposure.include=refresh
#management.security.enabled=false

# Disabling health check so that client does not try to load properties from
# spring config server every 5 minutes (should not be done in production)
spring.cloud.config.server.health.enabled=false
```

Example pointing at a local config-server:

```properties
spring.cloud.config.uri=http://localhost:51000/config
spring.cloud.config.label=master
spring.application.name=kernel
spring.cloud.config.name=application,kernel
spring.profiles.active=local
```

---

## Cloud config backends

### Git (composite list)

```text
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI=<your-git-repository-URL>
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=git
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_DEFAULT_LABEL=<your-git-repository-branch>
```

```text
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_URI=<your-another-git-repository-URL>
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_TYPE=git
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_DEFAULT_LABEL=<your-another-git-repository-branch>
```

### Native (file)

```text
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI=<file-path-for-local-properties>
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=native
```

Or with the local helper scripts / IDE, see [Optional: real mosip-config folder](#optional-real-mosip-config-folder).
