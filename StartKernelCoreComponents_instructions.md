### Introduction

If you want to start any service in MOSIP, you need to start the kernel core components. Following are the core components of the Kernel,
  

&nbsp;&nbsp;_a. Auth server_

  
&nbsp;&nbsp;_b. Config server_

  
&nbsp;&nbsp;_c. Audit Service_

  
&nbsp;&nbsp;_d. Master data_


Let's go through each one of the item and see how to make them up and running before you start your service. Following items have to be run in the sequential order. In this exercise, we are going to start Auth server --> Start Config server --> Audit service --> Master data service
  
### Prerequisite:

Before you start any of the steps, you should be aware of the following technical stuff, 

1. Kernel architecture

2. How OAuth2 and JWT works?

3. Springboot

4. Postman tool or any such similar tools to test the web service

5. Basic knowledge about PostgreSQL 10 server 

6. Basic knowledge about pgadmin4. This is a client tool to connect to PostgreSQL 10 server

7. Java 8 should have been installed in your development machine

8. Maven 3.3+ build tool should be installed in your development machine
  

NOTE: You can edit "\commons\kernel\pom.xml" file and give <skipTests>true</skipTests> to skip the unit test cases for faster builds. 

#### a. Auth server

Every service is protected. You must pass the Auth token in the request to access the service. Hence, start the Auth service first. 

 
Step 1. Run auth server in local profile which will give a mock token which will be used for validation in all other services.(It will give a token which has all roles so you will be abstracted from the auth part)


Step 2. Optional Step : You can also create your own JWT token using JWT algorithm as NONE and pass to any service which will be validated without checking signature(This will be if you want to work with specific roles in mosip platform)

 
To run auth service 

	Step 2.a Clone the https://github.com/mosip/commons repository
	
	Step 2.b Build the entire "\commons\kernel" project, 
	
	```
	mvn clean install
	```

	Step 2.b Start the "\commons\kernel\kernel-auth-service" by the following command, 
  
	```

	java -jar -Dspring.profiles.active=local {authservice jar name}

	```

	For example, 
	```

	java -jar -Dspring.profiles.active=local kernel-auth-service-1.0.7.jar

	```

Step 3. To verify the auth service, use the following swagger URL in browser,
 

```
http://localhost:8091/v1/authmanager/swagger-ui.html
```
Go to -> authmanager -> authenticate/useridPwd

Try the request below

```
http://localhost:8091/v1/authmanager/authenticate/useridPwd

  

{

	"id": "string",

	"metadata": {},

	"request": {

		"appId": "IDA", 

		"password": "anyusername", 

		"userName": "anypassword" 
	},

	"requesttime": "2018-12-10T06:12:52.994Z",

	"version": "string"

}

  

You will get a success message

  

You have to go to developer console and check if you got a authorization cookie in Application section

```

  

#### b. Config server

  

All the configuration values are retrieved from the config server. Start the local config server through the following steps,

  

**Steps to start your local config server**

  

Step i: Clone the https://github.com/mosip/mosip-config to your local machine. For example, clone to "D:\mosipgit\" folder.

  

Step ii: Start the "\commons\kernel\kernel-config-server" by the following command, 

  

```

https://repo.maven.apache.org/maven2/io/mosip/kernel/kernel-config-server/{version}/kernel-config-server-{version}.jar

```

  

For example,

```

https://repo.maven.apache.org/maven2/io/mosip/kernel/kernel-config-server/1.0.6/kernel-config-server-1.0.6.jar

```

  

Step iii: Run the jar use below command. {mosip-config-mt_folder_path} is the path of your cloned mosip-config-mt repository as in the 'Step i'

  

```

java -jar -Dspring.profiles.active=native -Dspring.cloud.config.server.native.search-locations=file:{mosip-config-mt_folder_path}/config -Dspring.cloud.config.server.accept-empty=true -Dspring.cloud.config.server.git.force-pull=false -Dspring.cloud.config.server.git.cloneOnStart=false -Dspring.cloud.config.server.git.refreshRate=0 {jarName}

```

For example,

  

```

java -jar -Dspring.profiles.active=native -Dspring.cloud.config.server.native.search-locations=file:D:\mosipgit\mosip-config\config-templates -Dspring.cloud.config.server.accept-empty=true -Dspring.cloud.config.server.git.force-pull=false -Dspring.cloud.config.server.git.cloneOnStart=false -Dspring.cloud.config.server.git.refreshRate=0 kernel-config-server-1.0.6.jar

```

  

Step v: To verify the config-server, hit the below url from browser

  

```

http://localhost:51000/config/{spring.profiles.active}/{spring.cloud.config.name}/{spring.cloud.config.label}

```

  

For example,

  

```

http://localhost:51000/config/kernel/env/master

```

  

NOTE:

i. The global properties are present in "application-{profile}.properties". For example, "application-local.properties".

ii. The properties for the module will be present in their respective "bootstrap.properties". If you are working on Kernel module, you can find the properties in "kernel-{profile}.properties". For example, "kernel-local.properties".

  

#### c. Audit Services

  

Most of the components uses Audit services. If the audit service is not running, the Springboot component will fail. So, it is a must for a Springboot service contributor to run the Audit service, before he start his service.
 

**Steps to start Audit manager in your local machine**
  

Step i: Take the code from https://github.com/mosip/commons/tree/master/kernel/kernel-auditmanager-service

Step ii: Change the following properties in "\commons\kernel\kernel-auditmanager-service\src\main\resources\bootstrap.properties"

  
spring.profiles.active=local
  

Step iii: Verify the following properties in "\commons\kernel\kernel-auditmanager-service\src\main\resources\application-local.properties", 
  
```
mosip.kernel.auditmanager-service-logs-location=logs/audit.log

spring.h2.console.enabled=true

spring.h2.console.path={spring.h2.console.path}

javax.persistence.jdbc.driver=org.h2.Driver

audit_database_url=jdbc:h2\:mem\:app_audit_log;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS audit

audit_database_username={auditdatabase username}

audit_database_password={audit database password}

hibernate.dialect=org.hibernate.dialect.H2Dialect

hibernate.jdbc.lob.non_contextual_creation=true

hibernate.hbm2ddl.auto=update

hibernate.show_sql=false

hibernate.format_sql=false

hibernate.connection.charSet=utf8

hibernate.cache.use_second_level_cache=false

hibernate.cache.use_query_cache=false

hibernate.cache.use_structured_entries=false

hibernate.generate_statistics=false

auth.server.validate.url=http://localhost:8091/authmanager/authorize/validateToken

auth.role.prefix=ROLE_

auth.header.name=Authorization

```

Step iv: Do a clean install and start the jar file
For example, 

```
mvn clean install
java -jar -Dspring.profiles.active=local kernel-auditmanager-service-1.0.7.jar
```
  

Step v: Optionally, if you want to open the H2DB console, open the following URL,

```

http://localhost:{server.port}/{spring.h2.console.path}

``` 

For example,

```

http://localhost:8081/admin/h2-console/

```

  
#### d. Master data

Enabling master data will make the environment live. The steps will be i. Install PostgreSQL, ii. Execute DDL and DML sql scripts

  

**Prerequisite**

a. Config server should be up and running, as mentioned in the previous 'b. Config server' section.

  

**Populate the data in PostgreSQL DB**

  

Step i: Install PostgresSQL 10.12 and pgAdmin 4. Give the password as 'root'. In fact, use 'root' as password in all places in this document. 

  

Step ii: Execute the DDL and DML scripts necessary for the master data. Reference:

  

https://github.com/mosip/commons/tree/master/db_scripts/mosip_master/dml

  

https://github.com/mosip/commons/blob/master/db_scripts/mosip_master/mosip_master_dml_deploy.sql

  

Steps to import data


If you are using Linux Operating System:

Step 1. Go to commons/db_scripts/mosip_master/mosip_master_deploy.properties update properties as per your localdatebase server you can use commons/db_scripts/Readme.md to read about each property

Step 2. run ./mosip_master_db_deploy.sh with mosip_master_deploy.properties as parameter

  

If you are using Windows Operating System:

Way 1. You can use Linux windows subsystem and run sh file as in linux section
  

Way 2. Open pgadmin --> Query Tool 


 Step 1. \commons\db_scripts\mosip_master\mosip_role_common.sql 
		(change the passwords for sysadminpwd, dbadminpwd, appadminpwd : Example, ```PASSWORD 'root'```).
    
 Step 2. \commons\db_scripts\mosip_master\mosip_role_masteruser.sql 
		(change the passwords: Example,  ``` PASSWORD 'root' ```)
    
 Step 3. \commons\db_scripts\mosip_master\mosip_master_db.sql
		(depending on os and postgres configuration you may have to remove the entire line containing ```LC_COLLATE = 'en_US.UTF-8'``` and ```LC_CTYPE ='en_US.UTF-8'``` and ```/c ``` commands from sql file. 
		You might have to execute the queries one by one
		Once the database is created, right click and refresh the "Servers --> PostgreSQL 10 --> Databases" from the left menu
		Right click on "Servers --> PostgreSQL 10 --> Databases --> mosip_master" and select "Query Tool..."
		Open the mosip_master_db.sql file and start executing from "DROP SCHEMA .." line)
    
 Step 4. In Query Tool pointing to "mosip_master" DB, open \commons\db_scripts\mosip_master\mosip_master_grants.sql
		(depending on os and postgres configuration you may have to remove /c commands from sql file)

 Step 5. In command prompt, go to 'commons\db_scripts\mosip_master\' location

NOTE: 
 a. Make sure psql is set in the PATH before you execute the below commands. 
 b. Refer the "Step 1" for the passwords which will be asked during execution of the below commands. 
 c. If you using Windows, you have to delete the "\c mosip_master sysadmin" text in both "mosip_master_ddl_deploy.sql" and "mosip_master_dml_deploy.sql" files. 
     
  This command will execute all DDL scripts. During execution, it will ask for password. Refer "Step 1"
 - psql --username={username} --host={database host} --port={database port}
   --dbname=mosip_master -f mosip_master_ddl_deploy.sql
   
   For example, 
   ```
   psql --username=sysadmin --host=127.0.0.1 --port=5432 --dbname=mosip_master -f mosip_master_ddl_deploy.sql
   
   ```
   
  This command will execute all DDL scripts. During execution, it will ask for password. Refer "Step 1"
 - psql --username={username} --host={database host} --port={database port}   
   --dbname=mosip_master -a -b -f mosip_master_dml_deploy.sql

  For example, 
  ```
  psql --username=sysadmin --host=127.0.0.1 --port=5432 --dbname=mosip_master -a -b -f mosip_master_dml_deploy.sql
  ```
  

**Configure and start master data service**

a. Take the code from https://github.com/mosip/commons/tree/master/kernel/kernel-masterdata-service 
  

b. Change following properties in "kernel-env.properties" in the config location. For example, in Windows, the location would be "D:\mosipgit\mosip-config\config-templates\kernel-env.properties"

```

mosip.kernel.database.hostname=127.0.0.1

mosip.kernel.database.port=5432

masterdata_database_url=jdbc:postgresql://${mosip.kernel.database.hostname}:${mosip.kernel.database.port}/mosip_master

masterdata_database_username=masteruser

masterdata_database_password=root

mosip.kernel.masterdata.audit-url=http://localhost:8081/v1/auditmanager/audits

```

c. Remove the following property if exist in kernel-env

```

spring.datasource.initialization-mode.

```

Change following property values in "D:\mosipgit\mosip-config\config-templates\application-env.properties".

```

auth.server.validate.url=http://localhost:8091/authmanager/authorize/admin/validateToken

auth.server.admin.validate.url=http://localhost:8091/v1/authmanager/authorize/admin/validateToken

```

d. Edit the properties in masterdata sevice to connect to config server, in the "\commons\kernel\kernel-masterdata-service\src\main\resources\bootstrap.properties" file.

```

spring.cloud.config.uri=http://127.0.0.1:51000/config 

spring.cloud.config.label=master

spring.profiles.active=env 

spring.cloud.config.name=kernel

spring.application.name=kernel-masterdata-service

```

e.  Do a maven Clean install and start the kernel-masterdata-service by the following command, 
```
mvn clean install
java -jar kernel-masterdata-service-1.0.7.jar 
```

f.  To verify the auth service, use the following swagger url from browser. NOTE: Make sure the Auth token is set in your cookie by trying out http://localhost:8091/v1/authmanager/authenticate/useridPwd . This is already explained in "a. Auth server" section. 
 

```
http://localhost:8086/v1/masterdata/swagger-ui.html
```

Improvement points:
 . Have to remove the PostgreSQL dependecy and use the H2 database in masterdata. 
 . Audit manager dependencies have to be removed from other components, in case if the environment is 'local'
 . Every component README.md file should have the build instructions for that component only
 . Every component README.md file should contain a link to the service and component dependency
 . Add windows installation instruction for PostgreSQL and pgadmin in Getting-started guide. 
 . Dockerized version of the document
 . Fix locale or encoding issue
 . For masterdata-service, all the properties should have been populated in a local properties file. Just by changing the environment, everything should work. 