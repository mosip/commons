#!/bin/bash

#installs the pkcs11 libraries.
set -e

DEFAULT_ZIP_PATH=artifactory/libs-release-local/hsm/client.zip
[ -z "$zip_file_path" ] && zip_path="$DEFAULT_ZIP_PATH" || zip_path="$zip_file_path"

echo "Download the client from $artifactory_url_env"
echo "Zip File Path: $zip_path"
wget $artifactory_url_env/$zip_path
echo "Downloaded $artifactory_url_env/$zip_file_path"
unzip client.zip
echo "Attempting to install"
cd ./client && ./install.sh 
echo "Installation complete"

exec "$@"
