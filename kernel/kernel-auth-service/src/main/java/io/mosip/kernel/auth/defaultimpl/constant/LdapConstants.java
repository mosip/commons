package io.mosip.kernel.auth.defaultimpl.constant;

/**
 * Leftover LDAP attribute, object-class, and JNDI constants from the pre-Keycloak
 * directory implementation (inetOrgPerson schema, password-policy attributes, and
 * boolean string tokens).
 */
public class LdapConstants {

	/**
	 * Prevents instantiation of this constants holder.
	 */
	private LdapConstants() {

	}

	/**
	 * LDAP common name attribute {@code cn}.
	 */
	public static final String CN = "cn";

	/**
	 * LDAP surname attribute {@code sn}.
	 */
	public static final String SN = "sn";

	/**
	 * LDAP mail attribute.
	 */
	public static final String MAIL = "mail";

	/**
	 * LDAP mobile attribute.
	 */
	public static final String MOBILE = "mobile";

	/**
	 * Date-of-birth attribute used on leftover user entries.
	 */
	public static final String DOB = "dob";

	/**
	 * Given-name attribute {@code firstName}.
	 */
	public static final String FIRST_NAME = "firstName";

	/**
	 * Family-name attribute {@code lastName}.
	 */
	public static final String LAST_NAME = "lastName";

	/**
	 * Gender-code attribute on leftover user entries.
	 */
	public static final String GENDER_CODE = "genderCode";

	/**
	 * LDAP {@code objectClass} attribute.
	 */
	public static final String OBJECT_CLASS = "objectClass";

	/**
	 * {@code inetOrgPerson} object class.
	 */
	public static final String INET_ORG_PERSON = "inetOrgPerson";

	/**
	 * LDAP {@code userPassword} attribute.
	 */
	public static final String USER_PASSWORD = "userPassword";

	/**
	 * Registration id attribute {@code rid}.
	 */
	public static final String RID = "rid";

	/**
	 * {@code organizationalPerson} object class.
	 */
	public static final String ORGANIZATIONAL_PERSON = "organizationalPerson";

	/**
	 * Active-flag attribute {@code isActive}.
	 */
	public static final String IS_ACTIVE = "isActive";

	/**
	 * {@code person} object class.
	 */
	public static final String PERSON = "person";

	/**
	 * {@code top} object class.
	 */
	public static final String TOP = "top";

	/**
	 * Custom {@code userDetails} object class or attribute.
	 */
	public static final String USER_DETAILS = "userDetails";

	/**
	 * LDAP {@code roleOccupant} attribute linking a role to a user DN.
	 */
	public static final String ROLE_OCCUPANT = "roleOccupant";

	/**
	 * String token for boolean true in directory attributes.
	 */
	public static final String TRUE = "true";

	/**
	 * String token for boolean false in directory attributes.
	 */
	public static final String FALSE = "false";

	/**
	 * LDAP {@code uid} attribute.
	 */
	public static final String UID = "uid";

	/**
	 * JNDI initial context factory class for LDAP binds.
	 */
	public static final String LDAP_INITAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

	/**
	 * LDAP password-policy attribute for account lock timestamp.
	 */
	public static final String PWD_ACCOUNT_LOCKED_TIME_ATTRIBUTE = "pwdAccountLockedTime";

	/**
	 * LDAP password-policy attribute for failed bind timestamps.
	 */
	public static final String PWD_FAILURE_TIME_ATTRIBUTE = "pwdFailureTime";
}
