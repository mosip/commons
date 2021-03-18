#!/bin/sh
find .  -type f -name '*_grants.sql' -execdir mv {} grants.sql ';'
find .  -type f -name '*_db_deploy.sh' -execdir mv {} deploy.sh ';'
find .  -type f -name '*_role_common.sql' -execdir mv {} role_common.sql ';'
find .  -type f -name '*_ddl_deploy.sql' -execdir mv {} ddl.sql ';'
find .  -type f -name '*user.sql' -execdir mv {} role_dbuser.sql ';'
find .  -type f -name '*deploy.properties' -execdir mv {} deploy.properties ';'
find .  -type f -name '*_db.sql' -execdir mv {} db.sql ';'
find .  -type f -name '*dml_deploy.sql' -execdir mv {} dml.sql ';'



