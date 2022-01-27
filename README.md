
[![Build Status](https://travis-ci.org/mosip/registration.svg?branch=master)](https://travis-ci.org/mosip/registration)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_registration&metric=alert_status)](https://sonarcloud.io/dashboard?id=mosip_registration)
[![Join the chat at https://gitter.im/mosip-community/registration](https://badges.gitter.im/mosip-community/registration.svg)](https://gitter.im/mosip-community/registration?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Commons

## Overview
As the name suggests, Commons refers to all the common services (also called "kernel") that are used by other modules of MOSIP. The Kernel services are listed below:

## Databases
Refer to [SQL scripts](db_scripts).

## Build & run (for developers)
The project requires JDK 1.11. 
1. To build jars:
    ```
    $ cd registration
    $ mvn clean install 
    ```
1. To skip JUnit tests and Java Docs:
    ```
    $ mvn install -DskipTests=true -Dmaven.javadoc.skip=true
    ```
1. To build Docker for a service:
    ```
    $ cd <service folder>
    $ docker build -f Dockerfile
    ```

## Deploy
To deploy Commons services on Kubernetes cluster using Dockers refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deployment/sandbox-deployment).

## Configuration
Refer to the [configuration guide](docs/configuration.md).

## Test
Automated functaionl tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).


