# MOSIP Commons

[![Maven Package upon a push](https://github.com/mosip/commons/actions/workflows/push-trigger.yml/badge.svg?branch=release-1.3.x)](https://github.com/mosip/commons/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?branch=release-1.3.x&project=mosip_commons&metric=alert_status)](https://sonarcloud.io/dashboard?branch=release-1.3.x&id=mosip_commons)

## Overview

**MOSIP Commons** is a collection of foundational libraries used across all MOSIP microservices.

It contains services to support configuration management, ID generation, notifications, and cryptographic salt generation.

---
# Services

The following core services are part of MOSIP Commons:

---

1. **[Kernel Notification Service](kernel/kernel-notification-service)** - Centralized notification service for sending messages such as SMS, emails.
2. **[Kernel Config Server](kernel/kernel-config-server)** - Centralized configuration service used by all MOSIP microservices.
3. **[Kernel RID Generator Service](kernel/kernel-ridgenerator-service)** - Generates globally unique Registration IDs (RID).
4. **[Kernel ID Generator Service](kernel/kernel-idgenerator-service)** - Generates unique IDs required across MOSIP service.
5. **[Kernel PRID Generator Service](kernel/kernel-pridgenerator-service)** - Generates Pre-Registration IDs (PRID).
6. **[Kernel Salt Generator](kernel/kernel-salt-generator)** - Generates cryptographically strong salts for cryptographic operation.


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
Add below runtime dependencies to the classpath, or include it as a Maven dependency:
- Add `kernel-auth-adapter.jar` 
- Add `kernel-smsserviceprovider-msg91.jar`

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
docker pull mosipid/kernel-notification-service:1.3.0

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
