#!/bin/bash

#installs the pre-requisites.
set -e

echo "Downloading pre-requisites install scripts"

wget -q --show-progress "${runtime_dep_url_env}/kernel-auth-adapter.jar" -O "${loader_path_env}"/kernel-auth-adapter.jar; \
wget -q --show-progress "${runtime_dep_url_env}/kernel-smsserviceprovider-msg91.jar" -O "${loader_path_env}"/kernel-smsserviceprovider-msg91.jar; \

echo "Installating pre-requisites completed."

exec "$@"