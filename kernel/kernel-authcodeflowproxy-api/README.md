# Kernel Auth Code Flow Proxy API

## Overview

**Kernel Auth Code Flow Proxy API** provides server-side login using the OAuth 2.0 [Authorization Code](https://oauth.net/2/grant-types/authorization-code/) grant: login, login-redirect, logout, and token validation against Keycloak (or a compatible IdP).

It is a **library** (not a Docker service). Do not fold it into `kernel-auth-service` or `kernel-auth-adapter`. It depends on `kernel-auth-adapter` (OpenID bridge API packages) with **no** `<version>`.

Moved into this reactor from [mosip-openid-bridge](https://github.com/mosip/mosip-openid-bridge).

Parent: [`../README.md`](../README.md)

---

# Local Setup

## Prerequisites
- **JDK:** 21
- **Maven:** 3.9+
- commons **`kernel-core`** installed first

## Usage

```xml
<dependency>
    <groupId>io.mosip.kernel</groupId>
    <artifactId>kernel-authcodeflowproxy-api</artifactId>
    <version>$version</version>
</dependency>
```

When the host application starts, the library exposes login / logout / validate-token REST APIs.

## Installation

From `kernel/`:

```text
mvn -pl kernel-authcodeflowproxy-api -am clean install -Dmaven.javadoc.skip=true "-Dgpg.skip=true"
```

## Configuration

### Properties

From [mosip-config](https://github.com/mosip/mosip-config) or the host `application` / `kernel` files:

```properties
auth.server.admin.validate.url=https://<host>/v1/authmanager/authorize/admin/validateToken
mosip.iam.module.clientID=<module-client-id>
mosip.iam.module.clientsecret=<module-client-secret>
mosip.iam.module.redirecturi=https://<host>/<context-path>/login-redirect/
mosip.iam.module.admin_realm_id=<realm-id>
mosip.iam.base-url=<iam-base-url>
mosip.iam.authorization_endpoint=${mosip.iam.base-url}/auth/realms/{realmId}/protocol/openid-connect/auth
mosip.iam.token_endpoint=${mosip.iam.base-url}/auth/realms/{realmId}/protocol/openid-connect/token
auth.allowed.urls=https://<ui-host>/
```

### Bean scanning

```text
io.mosip.kernel.authcodeflowproxy.api
```

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

• For any GitHub issues: [Report here](https://github.com/mosip/commons/issues)

---

## License

This project is licensed under the [Mozilla Public License 2.0](../../LICENSE).
