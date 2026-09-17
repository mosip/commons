## Kernel

Kernel provides the shared library and HTTP services used across MOSIP:

- **kernel-core** — published library (SPIs + implementations)
- **kernel-idgenerator-service** — RID and UIN/VID HTTP APIs
- **kernel-notification-service** — SMS and email
- **kernel-config-server** — Spring Cloud Config for all MOSIP microservices

Salt-table population Jobs are **not** part of this repo; they live with the modules that own those tables.

Retired standalone libraries and the separate RID HTTP module are listed in the [root README](../README.md#retired-modules). Depend on `kernel-core` instead of the old `kernel-*` artifacts. PRID generation moved to Pre-Registration.

All MOSIP modules depend on Kernel for common Java APIs and, where applicable, these REST services.

[Kernel REST APIs](https://github.com/mosip/mosip-docs/wiki/Kernel-APIs)

**Configuration** lives in [mosip-config](https://github.com/mosip/mosip-config).

### Build

From `kernel/`:

```text
mvn clean install -Dmaven.javadoc.skip=true -Dgpg.skip=true
```

### Deploy

Local config-server (Windows / Linux / macOS): [`kernel-config-server/run-local.sh`](kernel-config-server/run-local.sh), [`run-local.bat`](kernel-config-server/run-local.bat). Full setup: [`kernel-config-server/README.md`](kernel-config-server/README.md).

Local notifier (Windows / Linux / macOS): [`kernel-notification-service/run-local.sh`](kernel-notification-service/run-local.sh), [`run-local.bat`](kernel-notification-service/run-local.bat). Full setup: [`kernel-notification-service/README.md`](kernel-notification-service/README.md).

Local idgenerator (Windows / Linux / macOS): [`kernel-idgenerator-service/run-local.sh`](kernel-idgenerator-service/run-local.sh), [`run-local.bat`](kernel-idgenerator-service/run-local.bat). Full setup: [`kernel-idgenerator-service/README.md`](kernel-idgenerator-service/README.md).

Local run with a profile:

```text
java -Dspring.profiles.active=<profile> -jar <jar-name>.jar
```

Remote config:

```text
java -Dspring.profiles.active=<profile> -Dspring.cloud.config.uri=<config-url> -Dspring.cloud.config.label=<config-label> -jar <jar-name>.jar
```

Cluster install: [`deploy/kernel`](../deploy/kernel). See [`AGENTS.md`](AGENTS.md).
