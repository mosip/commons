[![Maven Package upon a push](https://github.com/mosip/commons/actions/workflows/push-trigger.yml/badge.svg?branch=release-1.3.x)](https://github.com/mosip/commons/actions/workflows/push-trigger.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=mosip_commons&metric=alert_status)](https://sonarcloud.io/dashboard?branch=release-1.3.x&id=mosip_commons)


# Commons

## Overview
As the name suggests, Commons refers to all the common services (also called "kernel") that are used by other modules of MOSIP.

## Databases
Refer to [SQL scripts](db_scripts).

## Build & run (for developers)
The project requires JDK 21.0.3.
and mvn version - 3.9.6
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
## Deployment in K8 cluster with other MOSIP services:
### Pre-requisites
* Set KUBECONFIG variable to point to existing K8 cluster kubeconfig file:
    * ```
        export KUBECONFIG=~/.kube/<my-cluster.config>
     ```
### Install
  ```
    $ cd deploy
    $ ./install.sh
   ```
### Delete
  ```
    $ cd deploy
    $ ./delete.sh
   ```
### Restart
  ```
    $ cd deploy
    $ ./restart.sh
   ```


## Deploy
To deploy Commons-Services on Kubernetes cluster using Dockers refer to [Sandbox Deployment](https://docs.mosip.io/1.2.0/deploymentnew/v3-installation).

## Test
Automated functional tests available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests).

## APIs
API documentation is available [here](https://mosip.github.io/documentation/1.2.0/1.2.0.html).

## License
This project is licensed under the terms of [Mozilla Public License 2.0](LICENSE).


