# MOSIP Commons

[![Maven Package upon a push](https://github.com/mosip/commons/actions/workflows/push-trigger.yml/badge.svg?branch=master)](https://github.com/mosip/commons/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=master)](https://sonarcloud.io/dashboard?branch=master)

## Overview

**MOSIP Commons** is a collection of foundational libraries used across all MOSIP microservices.

It contains services to support configuration management, ID generation, notifications, and OpenID/auth (including `kernel-auth-adapter`).

---
# Services

The following core services are part of MOSIP Commons:

---

1. **[Kernel Notification Service](kernel/kernel-notification-service)** - Centralized notification service for sending messages such as SMS, emails.
2. **[Kernel Config Server](kernel/kernel-config-server)** - Centralized configuration service used by all MOSIP microservices.
3. **[Kernel ID Generator Service](kernel/kernel-idgenerator-service)** - Generates RID and UIN/VID over one HTTP process (`/v1/ridgenerator`, `/v1/idgenerator`).
4. **[Kernel Auth Service](kernel/kernel-auth-service)** - Auth manager HTTP (`/v1/authmanager`), with sibling libs `kernel-auth-adapter` (includes OpenID bridge API) and `kernel-authcodeflowproxy-api`.

---

## Retired modules

These modules are **not** in this repository. Implementations that are still needed live in `kernel-core` or in another MOSIP repo. Do not add them back here.

### Folded into `kernel-core` (no longer published separately)

| Former module | Notes |
|---------------|--------|
| `kernel-bom` | Versions live on `kernel/pom.xml` (`spring-boot-starter-parent`) |
| `kernel-applicanttype-api` | Implementation in `kernel-core` |
| `kernel-dataaccess-hibernate` | Implementation in `kernel-core` |
| `kernel-datamapper-orika` | Implementation in `kernel-core` |
| `kernel-demographics-api` | Implementation in `kernel-core` |
| `kernel-idgenerator-machineid` | Implementation in `kernel-core` |
| `kernel-idgenerator-mispid` | Implementation in `kernel-core` |
| `kernel-idgenerator-partnerid` | Implementation in `kernel-core` |
| `kernel-idgenerator-rid` | Implementation in `kernel-core` |
| `kernel-idgenerator-tokenid` | Implementation in `kernel-core` |
| `kernel-idgenerator-vid` | Implementation in `kernel-core` |
| `kernel-idgenerator-regcenterid` | Implementation in `kernel-core` |
| `kernel-idobjectvalidator` | Implementation in `kernel-core` |
| `kernel-idvalidator-mispid` | Implementation in `kernel-core` |
| `kernel-idvalidator-prid` | Implementation in `kernel-core` |
| `kernel-idvalidator-rid` | Implementation in `kernel-core` |
| `kernel-idvalidator-uin` | Implementation in `kernel-core` |
| `kernel-idvalidator-vid` | Implementation in `kernel-core` |
| `kernel-licensekeygenerator-misp` | Implementation in `kernel-core` |
| `kernel-logger-logback` | Implementation in `kernel-core` |
| `kernel-pdfgenerator` | Implementation in `kernel-core` |
| `kernel-pinvalidator` | Implementation in `kernel-core` |
| `kernel-qrcodegenerator-zxing` | Implementation in `kernel-core` |
| `kernel-templatemanager-velocity` | Implementation in `kernel-core` |
| `kernel-transliteration-icu4j` | Implementation in `kernel-core` |
| `kernel-websubclient-api` | Implementation in `kernel-core` |
| `kernel-openid-bridge-api` | Folded into `kernel-auth-adapter` (`io.mosip.kernel.openid.bridge.*`) |
| `kernel-openid-bridge-api` | Folded into `kernel-auth-adapter` (`io.mosip.kernel.openid.bridge.*`) |

### Merged into `kernel-idgenerator-service`

| Former module | Preserved path |
|---------------|----------------|
| `kernel-ridgenerator-service` | `/v1/ridgenerator` |
| (UIN/VID HTTP) | `/v1/idgenerator` |

Helm chart `helm/ridgenerator` is retired. Use `helm/idgenerator`. `helm/pridgenerator` is retired; PRID generation moved to Pre-Registration.

### Moved into commons (from mosip-openid-bridge)

> **Future reference:** OpenID / auth Java modules were relocated from [mosip-openid-bridge](https://github.com/mosip/mosip-openid-bridge) `kernel/` into `commons/kernel/` so this reactor can publish and consume **`kernel-auth-adapter`** (and related APIs) without an external dependency. Keep them in this reactor; do not split them back out solely because other commons services depend on the adapter.

| Module | Role |
|--------|------|
| `kernel-auth-adapter` | Spring Security adapter + OpenID bridge API (`io.mosip.kernel.openid.bridge.*`; former `kernel-openid-bridge-api`) |
| `kernel-authcodeflowproxy-api` | OAuth 2.0 authorization-code proxy APIs |
| `kernel-auth-service` | Auth manager HTTP (`/v1/authmanager`) |
| `helm/authmanager` + `deploy/kernel` authmanager install | Cluster chart/scripts (also from mosip-openid-bridge) |


See [kernel/README.md](kernel/README.md#openid--auth-moved-from-mosip-openid-bridge).

### Moved out of commons

| Former module | Destination |
|---------------|-------------|
| `kernel-salt-generator` / `helm/regproc-salt` | Consuming repos that own salt tables (Job, not this repo) |
| `kernel-otpmanager-service` | [otp-manager](https://github.com/mosip/otp-manager) |
| `kernel-biometrics-api` / `kernel-bioapi-provider` | [commons-packet-manager](https://github.com/mosip/commons-packet-manager) / bio-utils |
| `kernel-idgenerator-prid` / `kernel-pridgenerator-service` | [pre-registration](https://github.com/mosip/pre-registration) |

---

## Database
Before starting the local setup, execute the required SQL scripts to initialize the database.
All database SQL scripts are available in the [db scripts](./db_scripts) directory.

# Local Setup

## Prerequisites
- **JDK:** 21  
- **Maven:** 3.9+  
- **Docker:** Latest  
- **PostgreSQL:** 10+  
- **Keycloak/IDP:** Required for notification authentication  
- **Config Server** with correct property files  


### Runtime Dependencies
`kernel-auth-adapter` (includes former `kernel-openid-bridge-api`) is **built in this reactor** and consumed by notifier / idgenerator / auth-service / authcodeflowproxy. `kernel-smsserviceprovider-msg91` (1.3.1-rc.1) remains an external Maven dependency of `kernel-notification-service`. They are packaged in the service JARs; do not download them at container start.

### Configuration

Common module uses the following configuration files that are accessible in this [repository](https://github.com/mosip/mosip-config/tree/master).
Please refer to the required released tagged version for configuration.
- [application-default.properties](https://github.com/mosip/mosip-config/blob/master/application-default.properties) : Contains common configurations which are required across MOSIP modules.
- [kernel-default.properties](https://github.com/mosip/mosip-config/blob/master/kernel-default.properties) : Contains configurations required or to be overridden for Commons module.


## Installation

### Local Setup (for Development or Contribution)

1. Make sure the config server is running. For detailed instructions on setting up and running the config server, refer to the [MOSIP Config Server Setup Guide](https://docs.mosip.io/1.2.0/modules/registration-processor/registration-processor-developers-guide#environment-setup).

**Note**: Refer to the MOSIP Config Server Setup Guide for setup, and ensure the properties mentioned above in the configuration section are taken care of. Replace the properties with your own configurations (e.g., DB credentials, IAM credentials, URL).

2. Clone the repository:

```text
git clone <repo-url>
cd commons 
```

3. Build the project:

```text
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

4. Start the application:
    - Click the Run button in your IDE, or
    - Run via command: `java -jar target/specific-service:<$version>.jar`

5. Verify Swagger is accessible.

### Local Setup with Docker (Easy Setup for Demos)

#### Option 1: Pull from Docker Hub

Recommended for users who want a quick, ready-to-use setup — testers, students, and external users.

Pull the latest pre-built images from Docker Hub using the following commands:

```text
docker pull mosipid/kernel-notification-service:1.4.1-SNAPSHOT
docker pull mosipid/kernel-idgenerator-service:1.4.1-SNAPSHOT
docker pull mosipid/kernel-config-server:1.4.1-SNAPSHOT
```

#### Option 2: Build Docker Images Locally

Recommended for contributors or developers who want to modify or build the services from source.

1. Clone and build the project:

```text
git clone <repo-url>
cd commons
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

2. Navigate to each service directory and build the Docker image:

```text
cd kernel/<service-directory>
docker build -t <service-name> .
```

#### Running the Services

Start each service using Docker:

```text
docker run -d -p <port>:<port> --name <service-name> <service-name>
```

#### Verify Installation

Check that all containers are running:

```text
docker ps
```

Access the services at `http://localhost:<port>` using the port mappings listed abov


---

## Deployment

### Kubernetes

To deploy Admin services on a Kubernetes cluster, refer to the [Sandbox Deployment Guide](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).


## Documentation

### API Documentation

API endpoints, base URL, and mock server details are available via Swagger documentation: [MOSIP Common Service API Documentation](https://mosip.github.io/documentation/1.2.0/1.2.0.html).

### Product Documentation

To learn more about MOSIP services from a functional perspective and use case scenarios, refer to our main documentation: [Click here](https://github.com/mosip/commons).


---
## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/commons/issues)

---

## License

This project is licensed under the [Mozilla Public License 2.0](LICENSE).
