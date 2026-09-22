# Authmanager

Helm chart for Kernel Auth Manager (`kernel-auth-service`). Moved from [mosip-openid-bridge](https://github.com/mosip/mosip-openid-bridge) `helm/authmanager`.

## TL;DR

```console
$ helm repo add mosip https://mosip.github.io
$ helm install authmanager mosip/authmanager
```

## Introduction

Authmanager exposes `/v1/authmanager` (port `8091`). Image: `kernel-auth-service`.

## Prerequisites

- Kubernetes 1.12+
- Helm 3.1.0
- conf-secrets and config-server installed first

## Installing the Chart

```console
helm install authmanager mosip/authmanager
```

Cluster install via [`deploy/kernel`](../../deploy/kernel).
