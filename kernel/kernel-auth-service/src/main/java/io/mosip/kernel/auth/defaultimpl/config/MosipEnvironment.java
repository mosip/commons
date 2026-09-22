package io.mosip.kernel.auth.defaultimpl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Resolves authmanager configuration keys from the Spring {@link Environment}.
 * Property-name fragments stored as fields are concatenated or looked up to
 * expose JWT, LDAP, OTP, masterdata, datastore, and per-app client credentials
 * used by services talking to Keycloak IAM.
 */
@Configuration
public class MosipEnvironment implements EnvironmentAware {

	/**
	 * Active Spring environment used to resolve property values.
	 */
	@Autowired
	private Environment environment;

	/**
	 * Property key for the JWT signing secret.
	 */
	private final String jwtSecret = "auth.jwt.secret";
	/**
	 * Property key for the JWT token base/issuer prefix.
	 */
	private final String tokenBase = "auth.jwt.base";
	/**
	 * Property key for access-token expiry in seconds.
	 */
	private final String tokenExpiry = "auth.jwt.expiry";
	/**
	 * Property key for refresh-token expiry.
	 */
	private final String refreshTokenExpiry = "auth.jwt.refresh.expiry";
	/**
	 * Property key for the HTTP header that carries the auth token.
	 */
	private final String authTokenHeader = "auth.token.header";
	/**
	 * Property key for the HTTP header that carries the refresh token.
	 */
	private final String authRefreshTokenHeader = "auth.refreshtoken.header";

	/**
	 * Property key for the LDAP service base URL.
	 */
	private final String ldapSvcUrl = "ldap.svc.url";
	/**
	 * Property key for the LDAP authenticate API path.
	 */
	private final String ldapAuthenticate = "ldap.api.authenticate";
	/**
	 * Property key for the LDAP OTP-user verify API path.
	 */
	private final String ldapVerifyOtpUser = "ldap.api.otp.user.verify";

	/**
	 * Property key for the OTP generate API.
	 */
	private final String generateOtpApi = "otp.manager.api.generate";
	/**
	 * Property key for the OTP verify API.
	 */
	private final String verifyOtpUserApi = "otp.manager.api.verify";

	/**
	 * Property key for the email OTP sender API.
	 */
	private final String otpSenderEmailApi = "otp.sender.api.email.send";
	/**
	 * Property key for the SMS OTP sender API.
	 */
	private final String otpSenderSmsApi = "otp.sender.api.sms.send";

	/**
	 * Property key for the masterdata template API.
	 */
	private final String masterDataTemplateApi = "masterdata.api.template";
	/**
	 * Property key for the masterdata OTP template name.
	 */
	private final String masterDataOtpTemplate = "masterdata.api.template.otp";

	/**
	 * Suffix for datastore JDBC host/IP properties.
	 */
	private final String propurl = ".datastore.ipaddress";
	/**
	 * Suffix for datastore JDBC port properties.
	 */
	private final String propport = ".datastore.port";
	/**
	 * Suffix for datastore JDBC username properties.
	 */
	private final String propusername = ".datastore.username";
	/**
	 * Suffix for datastore JDBC password properties.
	 */
	private final String proppassword = ".datastore.password";
	/**
	 * Suffix for datastore schema properties.
	 */
	private final String propschema = ".datastore.schema";
	/**
	 * Suffix for datastore JDBC driver class properties.
	 */
	private final String propdriver = ".datastore.driverClassName";

	/**
	 * Property key listing configured datastore identifiers.
	 */
	private final String datastores = "datastores";

	/**
	 * Property key for the LDAP roles search base DN.
	 */
	private String rolesSearchBase = "ldap.roles.base";
	/**
	 * Property key for the LDAP roles search filter prefix.
	 */
	private String rolesSearchPrefix = "ldap.roles.search.prefix";
	/**
	 * Property key for the LDAP roles search filter suffix.
	 */
	private String rolesSearchSuffix = "ldap.roles.search.suffix";
	/**
	 * Property key for the LDAP object class used for roles.
	 */
	private String ldapRolesClass = "ldap.roles.class";

	/**
	 * Property key for the primary language used in OTP templates.
	 */
	private String otpPrimaryLanguage = "auth.primary.language";

	/**
	 * Property key for the secondary language used in OTP templates.
	 */
	private String otpSecondaryLanguage = "auth.secondary.language";

	/**
	 * Property key for the ID repository get-UIN-details URL.
	 */
	private String uinGetDetailsUrl = "idrepo.api.getuindetails";

	/**
	 * Property key for sliding-window token expiry seconds.
	 */
	private String authSlidingWindowExp = "auth.token.sliding.window.exp";

	/**
	 * Prefix for per-application auth client properties ({@code mosip.kernel.auth.}).
	 */
	private String authPrefix = "mosip.kernel.auth.";

	/**
	 * Suffix for per-application Keycloak app id properties.
	 */
	private String authAppId = ".app.id";

	/**
	 * Suffix for per-application OAuth client id properties.
	 */
	private String authAppUserId = ".client.id";

	/**
	 * Suffix for per-application OAuth secret properties.
	 */
	private String authSecretKey = ".secret.key";

	/**
	 * ID repository URL used to load UIN / individual details for OTP flows.
	 *
	 * @return configured get-UIN-details URL
	 */
	public String getUinGetDetailsUrl() {
		return environment.getProperty(uinGetDetailsUrl);
	}

	/**
	 * OAuth client id for the given MOSIP application id.
	 *
	 * @param appId MOSIP application identifier
	 * @return client id from {@code mosip.kernel.auth.{appId}.client.id}
	 */
	public String getAppUserId(String appId) {
		return environment.getProperty(authPrefix + appId + authAppUserId);
	}

	/**
	 * Keycloak / IAM application id mapped for the given MOSIP application.
	 *
	 * @param appId MOSIP application identifier
	 * @return app id from {@code mosip.kernel.auth.{appId}.app.id}
	 */
	public String getAppId(String appId) {
		return environment.getProperty(authPrefix + appId + authAppId);
	}

	/**
	 * OAuth client secret for the given MOSIP application id.
	 *
	 * @param appId MOSIP application identifier
	 * @return secret from {@code mosip.kernel.auth.{appId}.secret.key}
	 */
	public String getSecretKey(String appId) {
		return environment.getProperty(authPrefix + appId + authSecretKey);
	}

	/**
	 * Sliding-window expiry used when re-issuing tokens.
	 *
	 * @return expiry in seconds
	 */
	public Integer getAuthSlidingWindowExp() {
		return Integer.valueOf(environment.getProperty(authSlidingWindowExp));
	}

	/**
	 * Primary language code for OTP notification templates.
	 *
	 * @return language code (for example {@code eng})
	 */
	public String getPrimaryLanguage() {
		return environment.getProperty(otpPrimaryLanguage);
	}

	/**
	 * Secondary language code for OTP notification templates.
	 *
	 * @return language code
	 */
	public String getSecondaryLanguage() {
		return environment.getProperty(otpSecondaryLanguage);
	}

	/**
	 * Injects the Spring environment after the context is aware of it.
	 *
	 * @param environment active environment
	 */
	@Override
	public void setEnvironment(final Environment environment) {
		this.environment = environment;
	}

	/**
	 * HTTP header name used to send the access token (typically Authorization).
	 *
	 * @return header name
	 */
	public String getAuthTokenHeader() {
		return environment.getProperty(authTokenHeader);
	}

	/**
	 * HTTP header name used to send the refresh token.
	 *
	 * @return header name
	 */
	public String getRefreshTokenHeader() {
		return environment.getProperty(authRefreshTokenHeader);
	}

	/**
	 * JDBC driver class for the named datastore.
	 *
	 * @param datasource datastore identifier prefix
	 * @return driver class name
	 */
	public String getDriverName(String datasource) {
		return environment.getProperty(datasource + propdriver);
	}

	/**
	 * Datastore host/IP for the named datastore.
	 *
	 * @param dataStore datastore identifier prefix
	 * @return host or IP address
	 */
	public String getUrl(String dataStore) {
		return environment.getProperty(dataStore + propurl);
	}

	/**
	 * Datastore port for the named datastore.
	 *
	 * @param dataStore datastore identifier prefix
	 * @return port as a string
	 */
	public String getPort(String dataStore) {
		return environment.getProperty(dataStore + propport);
	}

	/**
	 * Resolves a datastore property for an application key.
	 *
	 * @param app application property key
	 * @return datastore identifier or related value
	 */
	public String getDataStore(String app) {
		return environment.getProperty(app);
	}

	/**
	 * JDBC username for the named datastore.
	 *
	 * @param dataStore datastore identifier prefix
	 * @return username
	 */
	public String getUserName(String dataStore) {
		return environment.getProperty(dataStore + propusername);
	}

	/**
	 * JDBC password for the named datastore.
	 *
	 * @param dataStore datastore identifier prefix
	 * @return password
	 */
	public String getPassword(String dataStore) {
		return environment.getProperty(dataStore + proppassword);
	}

	/**
	 * Schema name for the given schema property prefix.
	 *
	 * @param schema schema property prefix
	 * @return schema name
	 */
	public String getSchemas(String schema) {
		return environment.getProperty(schema + propschema);
	}

	/**
	 * JWT signing secret.
	 *
	 * @return secret value
	 */
	public String getJwtSecret() {
		return environment.getProperty(jwtSecret);
	}

	/**
	 * JWT token base/issuer prefix.
	 *
	 * @return token base
	 */
	public String getTokenBase() {
		return environment.getProperty(tokenBase);
	}

	/**
	 * Access-token cookie/max-age expiry in seconds.
	 *
	 * @return expiry seconds
	 */
	public Integer getTokenExpiry() {
		return Integer.parseInt(environment.getProperty(tokenExpiry));
	}

	/**
	 * Refresh-token expiry.
	 *
	 * @return expiry value as {@link Long}
	 */
	public Long getRefreshTokenExpiry() {
		return Long.valueOf(environment.getProperty(refreshTokenExpiry));
	}

	/**
	 * LDAP service base URL.
	 *
	 * @return LDAP URL
	 */
	public String getLdapSvcUrl() {
		return environment.getProperty(ldapSvcUrl);
	}

	/**
	 * LDAP authenticate API path.
	 *
	 * @return authenticate path
	 */
	public String getLdapAuthenticate() {
		return environment.getProperty(ldapAuthenticate);
	}

	/**
	 * LDAP OTP-user verify API path.
	 *
	 * @return verify path
	 */
	public String getLdapVerifyOtpUser() {
		return environment.getProperty(ldapVerifyOtpUser);
	}

	/**
	 * OTP generate API URL.
	 *
	 * @return generate API
	 */
	public String getGenerateOtpApi() {
		return environment.getProperty(generateOtpApi);
	}

	/**
	 * OTP verify API URL.
	 *
	 * @return verify API
	 */
	public String getVerifyOtpUserApi() {
		return environment.getProperty(verifyOtpUserApi);
	}

	/**
	 * Email OTP sender API URL.
	 *
	 * @return email send API
	 */
	public String getOtpSenderEmailApi() {
		return environment.getProperty(otpSenderEmailApi);
	}

	/**
	 * SMS OTP sender API URL.
	 *
	 * @return SMS send API
	 */
	public String getOtpSenderSmsApi() {
		return environment.getProperty(otpSenderSmsApi);
	}

	/**
	 * Masterdata template API URL.
	 *
	 * @return template API
	 */
	public String getMasterDataTemplateApi() {
		return environment.getProperty(masterDataTemplateApi);
	}

	/**
	 * Masterdata OTP template identifier.
	 *
	 * @return OTP template name
	 */
	public String getMasterDataOtpTemplate() {
		return environment.getProperty(masterDataOtpTemplate);
	}

	/**
	 * Comma-separated or configured list of datastore identifiers.
	 *
	 * @return datastores property value
	 */
	public String getDataStores() {
		return environment.getProperty(datastores);
	}

	/**
	 * LDAP object class used when searching roles.
	 *
	 * @return object class name
	 */
	public String getLdapRolesClass() {
		return environment.getProperty(ldapRolesClass);
	}

	/**
	 * LDAP search base for roles.
	 *
	 * @return base DN
	 */
	public String getRolesSearchBase() {
		return environment.getProperty(rolesSearchBase);
	}

	/**
	 * LDAP roles search filter prefix.
	 *
	 * @return filter prefix
	 */
	public String getRolesSearchPrefix() {
		return environment.getProperty(rolesSearchPrefix);
	}

	/**
	 * LDAP roles search filter suffix.
	 *
	 * @return filter suffix
	 */
	public String getRolesSearchSuffix() {
		return environment.getProperty(rolesSearchSuffix);
	}

}
