# ID Repository

ID Repository acts as a repository of Identity details of an Individual, and provides API based mechanism to store and retrieve Identity details by Registration Processor module.

Following are the pre-requisites for storing or retrieving Identity authentication of an individual

1. ID Repository accepts ID JSON in the format as provided by the country in ID Schema
2. ID JSON present in ID Repository APIs gets validated against IdObjectValidator.

[ID Repository Documentation](https://mosipdocs.gitbook.io/platform/quick-links/modules/id-repository)

# Dependencies
ID Repository dependencies are mentioned below.  For all Kernel services refer to [commons repo](https://github.com/mosip/commons)

* id-repository-identity-service
    *  kernel-auditmanager-service 
    *  kernel-auth-service 
    *  kernel-config-server
    *  kernel-cryptomanager-service
    *  kernel-masterdata-service
    *  kernel-fsadapter-hdfs

* id-repository-vid-service
    *  kernel-auditmanager-service 
    *  kernel-auth-service 
    *  kernel-config-server
    *  kernel-cryptomanager-service
    *  kernel-vidgenerator-service
    *  id-repository-identity-service
    
# Configuration
Configurations used for ID Repo are available in [mosip-config](https://github.com/mosip/mosip-config)

# Build
Below command should be run in the parent project **id-repository**
`mvn clean install`

# Deploy
Below command should be executed to run any service locally in specific profile and local configurations - 
```
java -Dspring.profiles.active=<profile> -jar <jar-name>.jar
```

Below command should be executed to run any service locally in specific profile and `remote` configurations - 
```
java -Dspring.profiles.active=<profile> -Dspring.cloud.config.uri=<config-url> -Dspring.cloud.config.label=<config-label> -jar <jar-name>.jar
```

Below command should be executed to run a docker image - 
```
docker run -it -p <host-port>:<container-port> -e active_profile_env={profile} -e spring_config_label_env= {branch} -e spring_config_url_env={config_server_url} <docker-registry-IP:docker-registry-port/<docker-image>
```

**Sample Build and Deployment commands:**

```
docker run -it -d -p 8090:8090 -e active_profile_env={profile}  -e spring_config_label_env= {branch} -e spring_config_url_env={config_server_url} docker-registry.mosip.io:5000/id-repository-identity-service

docker run -it -d -p 8091:8091 -e active_profile_env={profile}  -e spring_config_label_env= {branch} -e spring_config_url_env={config_server_url} docker-registry.mosip.io:5000/id-repository-vid-service
```

# Test
Automated functional tests are available in [Functional Tests repo](https://github.com/mosip/mosip-functional-tests)

# Documentation
MOSIP documentation is available on [Wiki](https://mosipdocs.gitbook.io/platform)

ID Repository documentation is available on : [ID Repository Documentation](https://mosipdocs.gitbook.io/platform/quick-links/modules/id-repository)

ID Repository API documentation available on Wiki: [ID Repository APIs](https://mosipdocs.gitbook.io/platform/quick-links/apis/id-repository-apis)

# License
This project is licensed under the terms of [Mozilla Public License 2.0](https://github.com/mosip/commons/blob/master/LICENSE)
