#!/bin/bash

set -e

# Parse arguments
if [[ $# -lt 3 ]]; then
  echo "Usage: $0 <properties_file> <current_version> <target_version> <upgrade|revoke>"
  exit 1
fi

properties_file="$1"
current_version="$2"
target_version="$3"
action="$4"

echo "Properties file: $properties_file"
echo "Current version: $current_version"
echo "Target version: $target_version"
echo "Action: $action"

# Read properties file
if [ -f "$properties_file" ]; then
  echo "Reading properties from $properties_file"
  while IFS='=' read -r key value; do
    key=$(echo $key | tr '.' '_')
    eval ${key}=\${value}
  done < "$properties_file"
else
  echo "Property file not found, pass property file name as argument."
  exit 1
fi

# Terminate existing connections
echo "Terminating active connections"
CONN=$(PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$DEFAULT_DB_NAME -t -c "SELECT count(pg_terminate_backend(pg_stat_activity.pid)) FROM pg_stat_activity WHERE datname = '$MOSIP_DB_NAME' AND pid <> pg_backend_pid()";exit;)
echo "Terminated connections"

# Execute upgrade or revoke
if [ "$action" == "upgrade" ]; then
  echo "Upgrading database from $current_version to $target_version"
  UPGRADE_SCRIPT_FILE="sql/${current_version}_to_${target_version}_${UPGRADE_SCRIPT_FILENAME}"
  if [ -f "$UPGRADE_SCRIPT_FILE" ]; then
    echo "Executing upgrade script $UPGRADE_SCRIPT_FILE"
    PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$DEFAULT_DB_NAME -a -b -f $UPGRADE_SCRIPT_FILE
  else
    echo "Upgrade script not found, exiting."
    exit 1
  fi
elif [ "$action" == "revoke" ]; then
  echo "Revoking database from $current_version to $target_version"
  REVOKE_SCRIPT_FILE="sql/${current_version}_to_${target_version}_${REVOKE_SCRIPT_FILENAME}"
  if [ -f "$REVOKE_SCRIPT_FILE" ]; then
    echo "Executing revoke script $REVOKE_SCRIPT_FILE"
    PGPASSWORD=$SU_USER_PWD psql -v ON_ERROR_STOP=1 --username=$SU_USER --host=$DB_SERVERIP --port=$DB_PORT --dbname=$DEFAULT_DB_NAME -a -b -f $REVOKE_SCRIPT_FILE
  else
    echo "Revoke script not found, exiting."
    exit 1
  fi
else
  echo "Unknown action: $action, must be 'upgrade' or 'revoke'."
  exit 1
fi
