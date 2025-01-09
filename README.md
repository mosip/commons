[![Maven Package upon a push](https://github.com/mosip/commons/actions/workflows/push-trigger.yml/badge.svg?branch=master)](https://github.com/mosip/commons/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_commons&metric=alert_status)](https://sonarcloud.io/dashboard?branch=master&id=mosip_commons)


# Commons

## Overview
As the name suggests, Commons refers to all the common services (also called "kernel") that are used by other modules of MOSIP. The Kernel services are listed below:

## Databases
Refer to [SQL scripts](db_scripts).

## Build & run (for developers)
The project requires JDK 1.11. 
1. Build and install:
    ```
    $ cd kernel
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
    ```
1. Build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```

## Deploy
To deploy Commons services on Kubernetes cluster using Dockers refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deployment/sandbox-deployment).

## Test
Automated functaionl tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).


