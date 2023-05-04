#!/bin/bash

set -e

# Parse arguments
if [[ $# -lt 3 ]]; then
  echo "Usage: $0 <properties_file> <CURRENT_VERSION> <TARGET_VERSION> <upgrade|revoke>"
  exit 1
fi

properties_file="$1"
CURRENT_VERSION="$2"
TARGET_VERSION="$3"
ACTION="$4"

# Read properties file
if [ -f "$properties_file" ]; then
  echo "Reading properties from $properties_file"
  while IFS='=' read -r key value; do
    key=$(echo $key | tr '.' '_')
    eval ${key}=\${value}
  done < "$properties_file"
else
  echo "Property file not found, pass property file name as argument."
fi

echo "Properties file: $properties_file"
echo "Current version: $CURRENT_VERSION"
echo "Target version: $TARGET_VERSION"
echo "Action: $ACTION"

# Terminate existing connections
echo "Terminating active connections"
CONN=$(PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$DEFAULT_DB_NAME -t -c "SELECT count(pg_terminate_backend(pg_stat_activity.pid)) FROM pg_stat_activity WHERE datname = '$MOSIP_DB_NAME' AND pid <> pg_backend_pid()";exit;)
echo "Terminated connections"

# Execute upgrade or rollback
if [ "$ACTION" == "upgrade" ]; then
  echo "Upgrading database from $CURRENT_VERSION to $TARGET_VERSION"
  UPGRADE_SCRIPT_FILE="sql/${CURRENT_VERSION}_to_${TARGET_VERSION}_upgrade.sql"
  if [ -f "$UPGRADE_SCRIPT_FILE" ]; then
    echo "Executing upgrade script $UPGRADE_SCRIPT_FILE"
    PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$DEFAULT_DB_NAME -a -b -f $UPGRADE_SCRIPT_FILE
  else
    echo "Upgrade script not found, exiting."
    exit 1
  fi
elif [ "$ACTION" == "rollback" ]; then
  echo "Rolling back database for $CURRENT_VERSION to $TARGET_VERSION"
  REVOKE_SCRIPT_FILE="sql/${CURRENT_VERSION}_to_${TARGET_VERSION}_rollback.sql"
  if [ -f "$REVOKE_SCRIPT_FILE" ]; then
    echo "Executing rollback script $REVOKE_SCRIPT_FILE"
    PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$DEFAULT_DB_NAME -a -b -f $REVOKE_SCRIPT_FILE
  else
    echo "rollback script not found, exiting."
    exit 1
  fi
else
  echo "Unknown action: $ACTION, must be 'upgrade' or 'rollback'."
  exit 1
fi
