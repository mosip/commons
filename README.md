# MOSIP Commons

[![Maven Package upon a push](https://github.com/mosip/commons/actions/workflows/push-trigger.yml/badge.svg?branch=release-1.3.x)](https://github.com/mosip/commons/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_commons&metric=alert_status)](https://sonarcloud.io/dashboard?id=mosip_commons)

## Overview

**MOSIP Commons** is a collection of foundational libraries and
utilities used across all MOSIP microservices.\
It provides core building blocks such as cryptography, authentication
adapters, auditing utilities, key management, QR generation, OTP
handling, exception frameworks, HTTP utilities, logging, and more.

These modules provides foundational, reusable, and infrastructure-level services required across the MOSIP ecosystem.
These services support configuration management, ID generation, notifications, and cryptographic salt generation.

Commons acts as a backbone layer enabling consistency, reliability, and standardization across MOSIP services such as Registration Processor, Admin Services, and ID Repository.

For detailed documentation, refer to the **official MOSIP documentation**: https://docs.mosip.io

---

# Services

The following core services are part of MOSIP Commons:

---

## 1. Kernel Notification Service
**Path:** `kernel/kernel-notification-service`  
**Purpose:** Centralized notification service for sending messages such as SMS, email, and system alerts.

### Key Features
- Multi-channel notification support  
- Template-driven messaging  
- Asynchronous delivery  
- Retry and fallback support  
- Pluggable provider architecture  
- Widely used across MOSIP modules  

---

## 2. Kernel Config Server
**Path:** `kernel/kernel-config-server`  
**Purpose:** Centralized configuration service used by all MOSIP microservices.

### Key Features
- Git-backed configuration management  
- Environment-specific configuration loading  
- Ensures consistent runtime configuration  
- Required before launching MOSIP services  

---

## 3. Kernel RID Generator Service
**Path:** `kernel/kernel-ridgenerator-service`  
**Purpose:** Generates globally unique Registration IDs (RID) for the MOSIP Registration Processor.

### Key Features
- Guaranteed uniqueness  
- High-performance ID generation  
- Stateless and reliable  

---

## 4. Kernel ID Generator Service
**Path:** `kernel/kernel-idgenerator-service`  
**Purpose:** Generates unique IDs required across MOSIP services.

### Key Features
- Plug-and-play ID generation strategies  
- High throughput  
- Collision-free generation  

---

## 5. Kernel PRID Generator Service
**Path:** `kernel/kernel-pridgenerator-service`  
**Purpose:** Generates Pre-Registration IDs (PRID) for MOSIP Pre-Registration flows.

### Key Features
- Fast ID generation  
- Stateless operation  
- Ensures consistent PRID format  

---

## 6. Kernel Salt Generator
**Path:** `kernel/kernel-salt-generator`  
**Purpose:** Generates cryptographically strong salts for securing sensitive data.

### Key Features
- High entropy salt generation  
- Used in hashing processes  
- Enhances data integrity and protection  

---

# Required Configuration

MOSIP Commons services obtain their configuration from the **MOSIP Config Server**.
Configuration is centrally managed to ensure consistency across environments.

Only the **minimum required configuration** is documented here.

---

### Configuration Files

The following configuration files are required and must be available in the MOSIP Config repository:

- `kernel-default.properties`
- `application-default.properties`

---

### Configuration via Config Server Overrides

Certain sensitive and environment-specific properties **must not be defined in property files**.
These values are provided to the Config Server using **override environment variables**, as defined in the Config Server Helm chart.

**The following properties must be supplied via overrides:**

- `db.dbuser.password`
- `keycloak.internal.url`
- `keycloak.external.url`
- `keycloak.admin.password`
- `mosip.auth.client.secret`
- `mosip.ida.client.secret`
- `mosip.admin.client.secret`
- `mosip.reg.client.secret`
- `mosip.prereg.client.secret`
- `softhsm.kernel.security.pin`
- `email.smtp.host`
- `email.smtp.username`
- `email.smtp.secret`
- `mosip.api.internal.url`

---

### Common Database Configuration

```properties
mosip.kernel.database.hostname=postgres-postgresql.postgres
mosip.kernel.database.port=5432
```

---

### Kernel Config Server

```properties
spring.application.name=kernel-config-server
```

The Config Server must be running before starting any Commons service.

---

### Notification Service

```properties
mosip.kernel.sms.enabled=true
mosip.kernel.notification.email.from=do-not-reply@mosip.io
```

---

### ID / RID / PRID Generator Services

```properties
mosip.kernel.prid.min-unused-threshold=1000
mosip.kernel.prid.prids-to-generate=2000
```

---

### Key and Salt Management

```properties
mosip.kernel.keymanager.hsm.keystore-type=PKCS11
```
> **Note:** Config Server must be running before starting any Commons service.
---

# Local Setup

## Prerequisites
- **JDK:** 21  
- **Maven:** 3.9+  
- **Docker:** Latest  
- **PostgreSQL:** 10+  
- **Keycloak/IDP:** Required for notification authentication  
- **Config Server** with correct property files  

---

## Build Steps

### 1. Clone the Repository
```sh
git clone https://github.com/mosip/commons.git
cd commons
```

### 2. Build with Maven
```sh
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

### 3. Run a Service
```sh
java -jar <service-path>/target/<service.jar>
```

**Example:**
```sh
java -jar kernel/kernel-notification-service/target/kernel-notification-service.jar
```

---

# Deployment

Commons services can be deployed on Docker and Kubernetes.  
Refer to the MOSIP Deployment Guide:  
https://docs.mosip.io/1.2.0/deploymentnew/v3-installation

---

# Documentation

- Main MOSIP Docs: https://docs.mosip.io  
- API Docs: https://mosip.github.io/documentation  
- Commons Repo Docs: https://github.com/mosip/commons  

---

# Contribution & Community

- Contribution Guide: https://docs.mosip.io/1.2.0/community/code-contributions  
- Community Forum: https://community.mosip.io/  
- Issue Tracker: https://github.com/mosip/commons/issues  

---

# License

Licensed under the **Mozilla Public License 2.0**.
