# Kernel Auth Adapter

## Overview

**Kernel Auth Adapter** is the Spring Security adapter injected into MOSIP HTTP services. It validates inbound tokens (online or offline JWKS) and forwards service or requester tokens on outbound calls.

It also contains the former **`kernel-openid-bridge-api`** sources under `io.mosip.kernel.openid.bridge.*` (DTOs, SPIs, JWT helpers). Depend on this artifact only — do not add a separate openid-bridge-api dependency.

It is a **Maven dependency**, not a Docker wget extra. `kernel-core` is `provided` (the host service already has it). Vert.x **3.9.16** stays `provided`. The published artifact is the fat-jar (`appendAssemblyId=false`). Commons hosts unpack `io/mosip/kernel/auth/**` and `io/mosip/kernel/openid/**`.

Parent: [`../README.md`](../README.md)

Do not merge this module into `kernel-auth-service`. Spring Security 7 matchers: `AnyRequestMatcher` + `PathPatternSupport`. Same Boot 3.4 switch as MVC: `spring.mvc.pathmatch.matching-strategy=PATH_PATTERN_PARSER` (default) or `ANT_PATH_MATCHER`.

---

# Local Setup

## Prerequisites
- **JDK:** 21
- **Maven:** 3.9+
- commons **`kernel-core`** installed first

## Usage

Add the adapter to the host service:

```xml
<dependency>
    <groupId>io.mosip.kernel</groupId>
    <artifactId>kernel-auth-adapter</artifactId>
    <version>$version</version>
</dependency>
```

Reactor siblings omit `<version>` (parent `dependencyManagement` = `${project.version}`).

## Installation

From `kernel/`:

```text
mvn -pl kernel-auth-adapter -am clean install -Dmaven.javadoc.skip=true "-Dgpg.skip=true"
```

## Configuration

Host services take IAM and validate-token URLs from [mosip-config](https://github.com/mosip/mosip-config):
- [application-default.properties](https://github.com/mosip/mosip-config/blob/master/application-default.properties)
- [kernel-default.properties](https://github.com/mosip/mosip-config/blob/master/kernel-default.properties)

Typical keys: `auth.server.admin.validate.url`, `auth.server.admin.issuer.uri`, `auth.allowed.urls`, `mosip.auth.adapter.impl.basepackage`.

Path matching uses the same Boot 3.4 key as MVC, `spring.mvc.pathmatch.matching-strategy`:
- `PATH_PATTERN_PARSER` (omit or set) — Boot 4 default
- `ANT_PATH_MATCHER` — 3.4 Ant (including a double-star in the middle of a path)

mosip-config `application-default.properties` can still set `ANT_PATH_MATCHER`; change it to `PATH_PATTERN_PARSER` (or remove it) to disable Ant.

---

## Documentation

### API Documentation

[MOSIP Kernel Authentication Manager Service](https://mosip.github.io/documentation/1.2.0/kernel-authentication-manager-service.html)

### Product Documentation

[OpenID Bridge developer guide](https://docs.mosip.io/1.2.0/modules/commons/openid-bridge-developer-guide)

---

## Contribution & Community

• To learn how you can contribute code to this application, [click here](https://docs.mosip.io/1.2.0/community/code-contributions).

• If you have questions or encounter issues, visit the [MOSIP Community](https://community.mosip.io/) for support.

• For any GitHub issues: [Report here](https://github.com/mosip/mosip-openid-bridge/issues)

---

## License

This project is licensed under the [Mozilla Public License 2.0](../../LICENSE).
