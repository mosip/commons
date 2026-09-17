#!/bin/bash

#installs the pre-requisites.
set -e

echo "Downloading Ceylon cache for Chime scheduler"
wget --no-check-certificate --no-cache --no-cookies "${artifactory_url_env}/artifactory/libs-release-local/io/mosip/testing/regproc-reprocessor-ceylon-cache-repo.zip" -O regproc-reprocessor-ceylon-cache-repo.zip
unzip -o regproc-reprocessor-ceylon-cache-repo.zip
rm -rf regproc-reprocessor-ceylon-cache-repo.zip
echo "Downloaded Ceylon cache"

exec "$@"
