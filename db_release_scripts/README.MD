# MOSIP Commons Module Database Release Guide

## Prerequisities

* DB Server and access details.

* Postgres client (psql) should be installed on the deployment servers.

## Process

### Release script transfer to deployment server

*  Pull the DB deployment scripts to the deployment server directly and start deploying.

*  For pulling in local system and pushing them back to deployment server using WinSCP then make a note to modify the following encoding settings in WinSCP before pushing the files to deployment server  ``` Open WinSCP --> Options --> Preferences --> Transfer --> Edit --> In "Transfer mode" section --> select "Text" --> Click Ok --> Click Ok.```

#### Release scripts folder structure

 Database release scripts related to MOSIP modules are placed in ```**mosip_base_directory**>>db_release_scripts>>mosip_<schema_name>``` folder on git/repository.

**Example:** For commons module release script folder is **mosip_base_directory**>>db_release_scripts>>mosip_kernel.

Each release database folder has the following files / folders

* **mosip_<schema_name>/ddl folder:** Contains table creation scripts for the release.

* **mosip_<schema_name>/dml folder:** Contains seed data CSVs for the release.

* **mosip_<schema_name>/sql folder:** SQL alter script files for the release and revoke, Specific to release version.

* **mosip_<schema_name>/<schema_name>_release_db_deploy.sh:**  Script to deploy alter scripts.

* **mosip_<schema_name>/<schema_name>_release_deploy.properties:** Parameters required for release DB deployment.

* **mosip_<schema_name>/<schema_name>_revoke_db_deploy.sh:**  Script to revoke database deployment.


### Log File Directory

Create a log file directory if it doesn't exist on DB deployment server. Follow the steps:
 
``` bash-4.2$mkdir /mosip_base_directory/<folder_name> ```

Log files can be placed in a different folder by specifying the path of the directory in the properties file.

**Note:** SQL script files name under sql folder are appended with release version numbers( like **1.2.0-SNAPSHOT**_kernel-scripts_release.sql), that version number should be passed as argument while executing release/revoke shell scripts. Ex. **$bash master_release_db_deploy.sh master_release_deploy.properties 1.2.0-SNAPSHOT**.

**Note :** No need to update shell script unless any error or any further implementation is being introduced.


### Modifying Properties.

Properties file variable details:

**DB_SERVERIP=** Contains details of Destination SERVER_IP(Eg:10.0.0.1) where the deployment is targeted.

**DB_PORT=** Contains the postgres server port details where the postgres is allowed to connect. Eg: 5433.

**SU_USER=** Contains the postgres super user name to connect to the postgres database i.e. postgres.

**DEFAULT_DB_NAME=** Default database name to connect with respective postgres server i.e. Eg: postgres.

**MOSIP_DB_NAME=** Database name for which the deployment is scheduled.

**SYSADMIN_USER=** Variable contains the sysadmin user name which is super user for the remaining actions that are going to be performed by shell script.

**BASEPATH=** Path for DB scrips which are kept in the Deployment server. Eg: /home/madmin/database_release.

**LOG_PATH=** Path where deployment log file will be created. Eg: /home/madmin/logs/ .

**ALTER_SCRIPT_FLAG=** Flag variable which contains value as 0 or 1 to determine any alter scripts existance for the particular DB. If flag=0 then no alterscripts else flag=1.

**ALTER_SCRIPT_FILENAME=** Alter scripts sql filename, This file is kept under sql directory, Eg: <release_version_number>_<schema_name>-scripts_release.sql.

**REVOKE_SCRIPT_FLAG=** Flag variable which contains value as 0 or 1 to determine any revoke scripts existance for the particular DB. if flag=0 then no revoke scripts else flag=1.

**REVOKE_SCRIPT_FILENAME=** Revoke scripts sql filename, This file is kept under sql directory, Eg: <release_version_number>_<schema_name>-scripts_revoke.sql.

**Note -There should be an empty line at the end of the .properties files and no spaces in the beginning and at the end of the parameter values.**

### Set Password For User

**SU_USER_PWD=** Contains the password for postgres super user.
**SYSADMIN_PWD=** Contains the credential details for SYSADMIN_USER.
Example:
	`export SU_USER_PWD=supassword
	 export SYSADMIN_PWD=sysadminpassword`

### DB release deployment:
		
**Step 1** -> Modify respective database properties files ```**(<<schema>>_release_deploy.properties)**``` in the respective database directories.

**Step 2** -> Run the **"<<schema>>_release_db_deploy.sh"** script with release version as parameter to deploy all databases. To access and exeute **"<<schema>>_release_db_deploy.sh"** script, follow the below given sample commands:

```
bash-4.2$ cd /home/madmin/database_release/<<database_name>>

bash-4.2$** bash <<schema>>_release_db_deploy.sh  <<schema>>_release_deploy.properties <release_version_number>

```

**No modification required to be done on any of the <>.sql or <>.sh files in the database_release folder. If required, reach out to database team.**

### DB Deployment Revoke 

**Execute only if DB version release deployment revoking required.**

**Step 1** -> Run the DB Revoke deployment **"mosip_revoke_commons_db_deployment.sh "** script with release version as parameter.To access and exeute **"mosip_revoke_commons_db_deployment.sh "** script, follow the below given commands:

```
bash-4.2$** cd /home/madmin/database_release/<<database_name>> 

bash-4.2$** bash bash <<schema>>_revoke_db_deploy.sh  <<schema>>_release_deploy.properties <release_version_number>
```

### Post Deployment Validation

 Recheck the details(ip address, port number, database name, password) in the properties file in case of any following error messages:

```
<psql: could not translate host name "52.172.12.285" to address: Name or service not known>.

<psql: FATAL:  password authentication failed for user "postgress">

<psql: FATAL:  database "postgress" does not exist>
```
### Key points during or after the script execution

 * Properties file found message.

 * Server status.

 * Accessing the right path to deploy DB.

 * Creates respective roles.

 * Check for any active connections.

 * Creates roles, creating database, schemas, granting access, creating respective tables.

 * Loading data or DML operations valid only for those DB's which carries DML actions.

 * End of sourcing or deployment process.
		
* Database deployment log file captures all stages of deployment. Check Log files for any errors. Ignore **NOTICE** or **SKIPPING** messages as these  states that particular action is already done.
