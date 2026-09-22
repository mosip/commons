#!/bin/bash

#installs the pre-requisites.
set -e

echo "Auth adapter and SMS provider are Maven dependencies"

exec "$@"
