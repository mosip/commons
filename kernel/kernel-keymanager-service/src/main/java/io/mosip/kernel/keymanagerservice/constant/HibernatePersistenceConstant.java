package io.mosip.kernel.keymanagerservice.constant;

/**
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public class HibernatePersistenceConstant {

	/**
	 * Private constructor for HibernatePersistenceConstants
	 */
	private HibernatePersistenceConstant() {
	}

	/**

	/**
	 * The string constant jdbc schema
	 */
	public static final String KEYMANAGER_JDBC_SCHEMA = "keymanager.persistence.jdbc.schema";
	
	/**
	 * The string constant jdbc schema
	 */
	public static final String LKEYMANAGER_JDBC_SCHEMA = "licensekeymanager.persistence.jdbc.schema";

	/**
	 * The string constant cache query property
	 */
	public static final String CACHE_QUERY_PROPERTY = "javax.persistence.cache.storeMode";
	/**
	 * The string constant my sql dialect
	 */
	public static final String MY_SQL5_DIALECT = "org.hibernate.dialect.MySQL5Dialect";
	/**
	 * The string constant my sql dialect
	 */
	public static final String POSTGRESQL_95_DIALECT = "org.hibernate.dialect.PostgreSQL95Dialect";
	/**
	 * The string constant for hibernate statistics
	 */
	public static final String HIBERNATE_GENERATE_STATISTICS = "hibernate.generate_statistics";
	/**
	 * The string constant for use_structured_entries
	 */
	public static final String HIBERNATE_CACHE_USE_STRUCTURED_ENTRIES = "hibernate.cache.use_structured_entries";
	/**
	 * The string constant for use_query_cache
	 */
	public static final String HIBERNATE_CACHE_USE_QUERY_CACHE = "hibernate.cache.use_query_cache";
	/**
	 * The string constant for use_second_level_cache
	 */
	public static final String HIBERNATE_CACHE_USE_SECOND_LEVEL_CACHE = "hibernate.cache.use_second_level_cache";
	/**
	 * The string constant for charSet
	 */
	public static final String HIBERNATE_CONNECTION_CHAR_SET = "hibernate.connection.charSet";
	/**
	 * The string constant for format_sql
	 */
	public static final String HIBERNATE_FORMAT_SQL = "hibernate.format_sql";
	/**
	 * The string constant for show_sql
	 */
	public static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
	/**
	 * The string constant for dialect
	 */
	public static final String HIBERNATE_DIALECT = "hibernate.dialect";
	/**
	 * The string constant for hbm2ddl
	 */
	public static final String HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
	/**
	 * The string constant for non_contextual_creation
	 */
	public static final String HIBERNATE_NON_CONTEXTUAL_CREATION = "hibernate.jdbc.lob.non_contextual_creation";
	/**
	 * The string constant for current_session_context_class
	 */
	public static final String HIBERNATE_CURRENT_SESSION_CONTEXT = "hibernate.current_session_context_class";



	/**
	 * The string constant false
	 */
	public static final String FALSE = "false";
	/**
	 * The string constant utf8
	 */
	public static final String UTF8 = "utf8";
	/**
	 * The string constant true
	 */
	public static final String TRUE = "true";

	/**
	 * The string constant update
	 */
	public static final String UPDATE = "update";
	/**
	 * The string constant jta
	 */
	public static final String JTA = "jta";
	/**
	 * The string constant hibernate
	 */
	public static final String HIBERNATE = "hibernate";
	/**
	 * 
	 */
	public static final String HIBERNATE_EJB_INTERCEPTOR = "hibernate.ejb.interceptor";
	/**
	 * 
	 */
	public static final String EMPTY_INTERCEPTOR = "hibernate.empty.interceptor";

}