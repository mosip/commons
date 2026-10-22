#!/bin/bash
# Copy configmap and secret from one namespace to another.
# ./copy_cm_func.sh <resource> <configmap_name> <source_namespace> <destination_namespace> [name]
# Parameters:
#   resource: configmap|secret
#   name: Optional new name of the configmap or secret in destination namespace.  This may be needed if there is
#         clash of names

if [ "$1" = "configmap" ]
then
  RESOURCE=configmap
elif [ "$1" = "secret" ]
then
  RESOURCE=secret
else
  echo "Incorrect resource $1. Exiting.."
  exit 1
fi

SRC_NAME=$2
SRC_NS=$3
DST_NS=$4
DST_NAME=${5:-$2}

if [ "$SRC_NS" = "$DST_NS" ] && [ "$SRC_NAME" = "$DST_NAME" ]
then
  echo "Source and destination $RESOURCE $SRC_NS/$SRC_NAME are the same. Skipping.."
  exit 0
fi

# Read the source before touching the destination, so a missing source does not delete an existing copy.
if ! SRC_YAML=$(kubectl -n "$SRC_NS" get "$RESOURCE" "$SRC_NAME" -o yaml)
then
  echo "Failed to read $RESOURCE $SRC_NAME from namespace $SRC_NS. Exiting.."
  exit 1
fi

# Rewrite only the top-level metadata.namespace and metadata.name; leave data untouched.
DST_YAML=$(echo "$SRC_YAML" | sed "/^metadata:/,/^[^ ]/{
s/^  namespace: .*/  namespace: $DST_NS/
s/^  name: .*/  name: $DST_NAME/
}")

kubectl -n "$DST_NS" delete --ignore-not-found=true "$RESOURCE" "$DST_NAME"
echo "$DST_YAML" | kubectl -n "$DST_NS" create -f -
