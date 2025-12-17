# MOSIP Commons

[![Maven Package upon a push](https://github.com/mosip/commons/actions/workflows/push-trigger.yml/badge.svg?branch=release-1.3.x)](https://github.com/mosip/commons/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_commons&metric=alert_status)](https://sonarcloud.io/dashboard?id=mosip_commons)

## Overview

**MOSIP Commons** is a collection of foundational libraries used across all MOSIP microservices.

These services support configuration management, ID generation, notifications, and cryptographic salt generation.

---

# Services

The following core services are part of MOSIP Commons:

---

1. **[Kernel Notification Service](https://github.com/mosip/commons/tree/release-1.3.x/kernel/kernel-notification-service)** - Centralized notification service for sending messages such as SMS, email, and system alerts.
2. **[Kernel Config Server](https://github.com/mosip/commons/tree/release-1.3.x/kernel/kernel-config-server)** - Centralized configuration service used by all MOSIP microservices.
3. **[Kernel RID Generator Service](https://github.com/mosip/commons/tree/release-1.3.x/kernel/kernel-ridgenerator-servic)** - Generates globally unique Registration IDs (RID) for the MOSIP Registration Processor.
4. **[Kernel ID Generator Service](https://github.com/mosip/commons/tree/release-1.3.x/kernel/kernel-idgenerator-servic)** - Generates unique IDs required across MOSIP service.
5. **[Kernel PRID Generator Service](https://github.com/mosip/commons/tree/release-1.3.x/kernel/kernel-pridgenerator-servic)** - Generates Pre-Registration IDs (PRID) for MOSIP Pre-Registration flows.
6. **[Kernel Salt Generator](https://github.com/mosip/commons/tree/release-1.3.x/kernel/kernel-salt-generator)** - Generates cryptographically strong salts for securing sensitive data.

---

# Required Configuration

### Configuration

Common module uses the following configuration files that are accessible in this [repository](https://github.com/mosip/mosip-config/tree/master).
Please refer to the required released tagged version for configuration.
- [application-default.properties](https://github.com/mosip/mosip-config/blob/master/application-default.properties) : Contains common configurations which are required across MOSIP modules.
- [kernel-default.properties](https://github.com/mosip/mosip-config/blob/master/kernel-default.properties) : Contains configurations required or to be overridden for Common module.



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

## Documentation

For more detailed documents for repositories, you can [check here](https://github.com/mosip/documentation/tree/1.2.0/docs).

### API Documentation

API endpoints, base URL, and mock server details are available via Swagger documentation: [MOSIP Common Service API Documentation](https://mosip.github.io/documentation/1.2.0/1.2.0.html).

### Product Documentation

To learn more about admin services from a functional perspective and use case scenarios, refer to our main documentation: [Click here](https://github.com/mosip/commons).


---
## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/admin-services/issues)

---

## License

This project is licensed under the [Mozilla Public License 2.0](LICENSE).
