#!/bin/bash

#installs the pre-requisites.
set -e

echo "Downloading pre-requisites install scripts"

wget "${artifactory_url_env}"/artifactory/libs-release-local/io/mosip/testing/regproc-reprocessor-ceylon-cache-repo.zip ; \
unzip regproc-reprocessor-ceylon-cache-repo.zip ; \
rm -rf regproc-reprocessor-ceylon-cache-repo.zip ; \
wget "${iam_adapter_url_env}" -O "${loader_path_env}"/kernel-auth-adapter.jar; \

echo "Installating pre-requisites completed."

exec "$@"