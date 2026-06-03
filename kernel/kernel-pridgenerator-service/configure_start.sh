#!/bin/bash

#installs the pre-requisites.
set -e

echo "Downloading pre-requisites install scripts"

wget -q --show-progress "${iam_adapter_url_env}" -O "${loader_path_env}"/kernel-auth-adapter.jar; \

echo "Installating pre-requisites completed."

exec "$@"