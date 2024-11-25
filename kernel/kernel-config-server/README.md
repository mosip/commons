## kernel-config-server

[Background & Design]( https://github.com/mosip/mosip/wiki/MOSIP-Configuration-Server )

Default Port and Context Path

```
server.port=51000
server.servlet.path=/config

```

**For Encryption Decryption of properties** <br/>
<br/>
Create keystore with following command: <br/>
`keytool -genkeypair -alias <your-alias> -keyalg RSA -keystore server.keystore -storepass <store-password> --dname "CN=<your-CN>,OU=<OU>,O=<O>,L=<L>,S=<S>,C=<C>"`

When you run the above command it will ask you for password for < your-alias > , choose your password or press enter for same password as < store-password >

The JKS keystore uses a proprietary format. It is recommended to migrate to PKCS12 which is an industry standard format, migrate it using following command:
`keytool -importkeystore -srckeystore server.keystore -destkeystore server.keystore -deststoretype pkcs12` <br/>
For more information look [here]( https://cloud.spring.io/spring-cloud-config/single/spring-cloud-config.html#_creating_a_key_store_for_testing )

**How To Run**
<br/>
To run the application: <br/>
Make sure you have configured ssh keys to connect to git, because it will take ssh keys from default location (${user.home}/.ssh) .

Set environment variables to support git repos for composite profile. Here 0,1 indicates list items.
If any property exists in multiple repositories then repo at 0 index will have high priority and value will be referred from that repo.
```
export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI=<git-repo-ssh-url>
export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=git
export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_DEFAULT_LABEL=<branch-to-refer>

export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_URI=<git-repo-ssh-url>
export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_TYPE=git
export SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_DEFAULT_LABEL=<branch-to-refer>
```
Now run the jar using the following command: <br/>
<br/>
`java -jar -Dencrypt.keyStore.location=file:///< file-location-of-keystore > -Dencrypt.keyStore.password=< keystore-passowrd > -Dencrypt.keyStore.alias=< keystore-alias > -Dencrypt.keyStore.secret=< keystore-secret > < jar-name >`
<br/>
<br/>
To run it inside Docker container provide the following run time arguments:
1. SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI
   The URL of your Git repo

2. SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE
   Repo type, which is git

3. SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_DEFAULT_LABEL
   branch to refer in git repo. If not provided, it will default to `main` branch

4. SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_SEARCH_PATHS
   The folder inside your git repo which contains the configuration

5. encrypt_keyStore_location_env
   The encrypt keystore location

6. encrypt_keyStore_password_env
   The encryption keystore password

7. encrypt_keyStore_alias_env
   The encryption keystore alias

8. encrypt_keyStore_secret_env
   The encryption keyStore secret

The final docker run command should look like:

`docker run --name=<name-the-container> -d -v <location-of-encrypt-keystore>/server.keystore:<mount-keystore-location-inside-container>/server.keystore:z -v /home/madmin/<location of folder containing git ssh keys>:<mount-ssh-location-inside-container>/.ssh:z -e SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI=<git_ssh_url_env> -e SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=git -e SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_DEFAULT_LABEL=<branch-for-repo> -e encrypt_keyStore_location_env=file:///<mount-keystore-location-inside-container>/server.keystore -e encrypt_keyStore_password_env=<encrypt_keyStore_password_env> -e encrypt_keyStore_alias_env=<encrypt_keyStore_alias_env> -e encrypt_keyStore_secret_env=<encrypt_keyStore_secret_env> -p 51000:51000 <name-of-docker-image-you-built>`
<br/>
<br/>
**To Encrypt any property:** <br/>
Run the following command : <br/>
`curl http://<your-config-server-address>/<application-context-path-if-any>/encrypt -d <value-to-encrypt>`

And place the encrypted value in client application properties file with the format: <br/>
`password={cipher}<encrypted-value>`

**To Decrypt any property manually:** <br/>

`curl http://<your-config-server-address>/<application-context-path-if-any>/decrypt -d <encrypted-value-to-decrypt>`

**NOTE** There is no need to write decryption mechanism in client applications for encrypted values. They will be automatically decrypted by config server.



**Application Properties**

``` 
#Port where mosip spring cloud config server needs to run
server.port = 51000

#adding context path
server.servlet.path=/config

spring.profiles.active=composite

#Server would return a HTTP 404 status, if the application is not found.By default, this flag is set to true.
spring.cloud.config.server.accept-empty=false

## As spring.profiles.active is composite, use env variable to provide values for git configuration as below
##########################
##Git repository location where configuration files are stored
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI=<your-git-repository-URL>

##Type of repository, possible types are git, svn, native
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=git

##Branch/label to refer for in config repository
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_DEFAULT_LABEL=<your-git-repository-branch>

#Spring Cloud Config Server makes a clone of the remote git repository and if somehow the local copy gets
#dirty (e.g. folder content changes by OS process) so Spring Cloud Config Server cannot update the local copy
#from remote repository. For Force-pull in such case, we are setting the flag to true.
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_FORCE_PULL=true

# Setting up refresh rate to 5 seconds so that config server will check for updates in Git repo after every 5 seconds,
#can be lowered down for production.
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_REFRESH_RATE=5

# adding provision to clone on start of server instead of first request
# SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_CLONE_ON_START=true

#Path inside the GIT repo where config files are stored, in our case they are inside config directory
#SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_SEARCH_PATHS=<folder-in-git-repository-containing-configuration>

# Disabling health endpoints to improve performance of config server while in development, can be commented out in production.
health.config.enabled=false

#For encryption of properties
###########################################
#pass at runtime
#encrypt.keyStore.location=file:///<your-encryption-keyStore-path>
#encrypt.keyStore.password=<your-encryption-keyStore-password>
#encrypt.keyStore.alias=<your-encryption-keyStore-alias>
#encrypt.keyStore.secret=<your-encryption-keyStore-secret>



```

**Config hierarchy**

![Config Properties](../../docs/design/kernel/_images/GlobalProperties_1.jpg)



**Maven dependency for Config client**

```
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-config</artifactId>
			<version>${spring-cloud-config.version}</version>
		</dependency>

```


**Config client bootstrap.properties**

```
spring.cloud.config.uri=http://<config-host-url>:<config-port>
spring.cloud.config.label=<git-branch>
spring.application.name=<application-name>
spring.cloud.config.name=<property-file-to-pick-up-configuration-from>
spring.profiles.active=composite
management.endpoints.web.exposure.include=refresh
#management.security.enabled=false

#disabling health check so that client doesnt try to load properties from sprint config server every
# 5 minutes (should not be done in production)
spring.cloud.config.server.health.enabled=false

```

**cloud config supported for git type repository**

```
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI=<your-git-repository-URL>
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=git
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_DEFAULT_LABEL=<your-git-repository-branch>
```

```
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_URI=<your-another-git-repository-URL>
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_TYPE=git
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_1_DEFAULT_LABEL=<your-another-git-repository-branch>
```

**cloud config supported for native**

```
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_URI=<file-path-for-local-properties>
SPRING_CLOUD_CONFIG_SERVER_COMPOSITE_0_TYPE=native
```