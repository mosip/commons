## Kernel Authcodeflowproxy Api

## Overview
This library provides server side functions related login using authorization code flow. The Authorization Code grant type is used by confidential and public clients to exchange an authorization code for an access token. For an overview on Authorization Code grant type [refer](https://oauth.net/2/grant-types/authorization-code/).

## Technical features
- Provides REST APIs for login, logout and online token validate funtionalities.

## Usage
1. To use this api, add this to dependency list:

```
		<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-authcodeflowproxy-api</artifactId>
			<version>1.2.0</version>
		</dependency>
```


2. Properties to be added:

```
auth.server.admin.validate.url=https://<host>/v1/authmanager/authorize/admin/validateToken
mosip.iam.module.clientID=<module-client-id>
mosip.iam.module.clientsecret=<module-client-secret>
mosip.iam.module.redirecturi=https://<host>/<context-path>/login-redirect/	
mosip.iam.module.admin_realm_id=<realm-id>	
mosip.iam.base-url=<iam-bas-url>	
mosip.iam.authorization_endpoint=${mosip.iam.base-url}/auth/realms/{realmId}/protocol/openid-connect/auth
mosip.iam.token_endpoint=${mosip.iam.base-url}/auth/realms/{realmId}/protocol/openid-connect/token
```

3. Add following package to scan for beans

```
io.mosip.kernel.authcodeflowproxy.api.*
```

4. When the server is up it will have 4 new rest apis.

