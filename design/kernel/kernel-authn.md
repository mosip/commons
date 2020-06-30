# Authentication and Authorization in MOSIP platform

#### Background

The authentication and authorization in the MOSIP platform is handled in the a centralized place. OAuth 2.0 authorization framework is used as the standards.JSON Web Token(JWT) is used as a mechanism to implement OAuth2.0 framework. 

#### Solution



**The key solution considerations are**


- A centralized AUTH SERVER (Auth Service with an IAM example Keycloak) handles the authorization request from the platform. 

- Once authenticated, the Auth Server sends back an AuthToken.

- Auth token contains the information about the authenticated user and the meta data such as the expiration time, subject, issuer etc., 

- The Tokens are stored in the IAM for an individual user. In case of force logout scenario, this record will be deleted from this datastore.  

**Tokens Overview**

![Tokens Overview](_images/kernel-authn-tokensoverview.jpg)

**MOSIP Authentication and Authorization Principles**

- No resource in the MOSIP can be accessed without Authentication and Authorization. 

- All the auth requests will go via the Auth Server. 

- The user data stores are abstracted behind the IAM. 

- The user data stores should be pluggable(If supported by configured IAM). 

- The platform's authentication method should support the heterogenous technologies and authentication should happen seamlessly. 


**Different kinds of Actors in MOSIP platform**

- Following are the identified actors in the authentication, 

1. Human users

- These are the actual human users. 

- Users are provided with Username Password

2. Clients

- These are the applications like Registration client, IDA etc., 

- Applications are provided with client_credentials like clientid & secret-key


**Implementing Auth in the web services**

- To ease out the development effort, an Auth Adapter component is distributed to the Web service developers. 

- The developer have to do 3 steps to plug in the Auth Adapter, 

1. Include the Auth Adapter jar file in the classpath. 

2. Inject the Auth Server details 

3. Annotate the server method which have to be protected with the "Role" information. 

Reference: [Auth Springboot User Guide](https://docs.mosip.io/platform/contribute/coding-standards/auth-springboot-user-guide)


**Implementing Auth in JavaFX application**


1. If the actual human user login, pass the username and password to the Auth server.

2. In case of background serivces, pass the clientid & secret-key. 

3. Once authenticated, the client receives the Auth Token. Client stores them in a secured local store.

4. In all the REST service calls to server, include the Auth Token. 

5. If the Auth token is expired, login again. 


**Implementing Auth in Angular application**

1. The human user logs in using the username and the password. 

2. Once authenticated, the client receives the Auth Token. Client stores them in a HTTP-only cookie. 

3. In all the REST service calls to server, Auth Token is included.  

4. If the Auth Token is expired, the page is redirected to the login page.

5. A timer runs in the client side, which is lesser than the session timeout. This timer calls the "/authorize/validateToken" service.

6. If the Auth Token comes insides the Sliding Window Threshold, a new Auth Token will be issued. 

![Angular authentication keeep alive flow](_images/kernel-authentication-keepalive-prereg.jpg)

Reference:  [Auth Angular User Guide](https://docs.mosip.io/platform/contribute/coding-standards/auth-angular-user-guide)


**Different realms in MOSIP(IN KEYCLOAK)**

1. Pre-Registration --> preregistration

2. Registration --> mosip

3. Registration processor --> mosip

4. IDA --> mosip

5. Partner Management --> partnermanagement


**Pluggable IAM with Auth Service**

1. Auth Service communicates with any plugable IAM with OPENID Endpoints.
2. All the features like token storage,user details storage, sessions etc are handled by IAM.
3. In case supported by IAM Auth Service will connect with different realms and do AUTHN and AUTHZ smoothly.


**Class Diagram**



![Class Diagram](_images/auth-service-cd.png)