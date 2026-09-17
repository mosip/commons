package io.mosip.kernel.dataaccess.hibernate.constant;

/**
 * JDBC, Hibernate, and JPA property keys and default values used by {@link io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig}.
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public class HibernatePersistenceConstant {

	/**
	 * Private constructor for HibernatePersistenceConstants
	 */
	private HibernatePersistenceConstant() {
	}

	/**
	 * JPA JDBC driver class property ({@code javax.persistence.jdbc.driver}).
	 */
	public static final String JDBC_DRIVER = "javax.persistence.jdbc.driver";

	/**
	 * JPA JDBC URL property ({@code javax.persistence.jdbc.url}).
	 */
	public static final String JDBC_URL = "javax.persistence.jdbc.url";

	/**
	 * JPA JDBC password property ({@code javax.persistence.jdbc.password}).
	 */
	public static final String JDBC_PASS = "javax.persistence.jdbc.password";

	/**
	 * JPA JDBC schema property ({@code javax.persistence.jdbc.schema}).
	 */
	public static final String JDBC_SCHEMA = "javax.persistence.jdbc.schema";

	/**
	 * JPA JDBC user property ({@code javax.persistence.jdbc.user}).
	 */
	public static final String JDBC_USER = "javax.persistence.jdbc.user";
	/**
	 * JPA second-level cache store mode ({@code javax.persistence.cache.storeMode}).
	 */
	public static final String CACHE_QUERY_PROPERTY = "javax.persistence.cache.storeMode";
	/**
	 * Hibernate MySQL 5 dialect class name.
	 */
	public static final String MY_SQL5_DIALECT = "org.hibernate.dialect.MySQL5Dialect";
	/**
	 * Hibernate PostgreSQL dialect class name.
	 */
	public static final String POSTGRESQL_95_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";
	/**
	 * Hibernate statistics toggle ({@code hibernate.generate_statistics}).
	 */
	public static final String HIBERNATE_GENERATE_STATISTICS = "hibernate.generate_statistics";
	/**
	 * Structured cache entries toggle ({@code hibernate.cache.use_structured_entries}).
	 */
	public static final String HIBERNATE_CACHE_USE_STRUCTURED_ENTRIES = "hibernate.cache.use_structured_entries";
	/**
	 * Query cache toggle ({@code hibernate.cache.use_query_cache}).
	 */
	public static final String HIBERNATE_CACHE_USE_QUERY_CACHE = "hibernate.cache.use_query_cache";
	/**
	 * Second-level cache toggle ({@code hibernate.cache.use_second_level_cache}).
	 */
	public static final String HIBERNATE_CACHE_USE_SECOND_LEVEL_CACHE = "hibernate.cache.use_second_level_cache";
	/**
	 * JDBC connection character set ({@code hibernate.connection.charSet}).
	 */
	public static final String HIBERNATE_CONNECTION_CHAR_SET = "hibernate.connection.charSet";
	/**
	 * Pretty-print SQL toggle ({@code hibernate.format_sql}).
	 */
	public static final String HIBERNATE_FORMAT_SQL = "hibernate.format_sql";
	/**
	 * Log SQL toggle ({@code hibernate.show_sql}).
	 */
	public static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
	/**
	 * Hibernate dialect property ({@code hibernate.dialect}).
	 */
	public static final String HIBERNATE_DIALECT = "hibernate.dialect";
	/**
	 * Schema generation mode ({@code hibernate.hbm2ddl.auto}).
	 */
	public static final String HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
	/**
	 * Non-contextual LOB creation ({@code hibernate.jdbc.lob.non_contextual_creation}).
	 */
	public static final String HIBERNATE_NON_CONTEXTUAL_CREATION = "hibernate.jdbc.lob.non_contextual_creation";
	/**
	 * Current session context class ({@code hibernate.current_session_context_class}).
	 */
	public static final String HIBERNATE_CURRENT_SESSION_CONTEXT = "hibernate.current_session_context_class";

	/**
	 * Entity scan pattern for MOSIP packages ({@code io.mosip.*}).
	 */
	public static final String MOSIP_PACKAGE = "io.mosip.*";

	/**
	 * Boolean {@code false} as a Hibernate property value.
	 */
	public static final String FALSE = "false";
	/**
	 * UTF-8 charset name used for Hibernate connections.
	 */
	public static final String UTF8 = "utf8";
	/**
	 * Boolean {@code true} as a Hibernate property value.
	 */
	public static final String TRUE = "true";

	/**
	 * {@code hbm2ddl.auto} value {@code update}.
	 */
	public static final String UPDATE = "update";
	/**
	 * JTA transaction-type token.
	 */
	public static final String JTA = "jta";
	/**
	 * Persistence-provider name {@code hibernate}.
	 */
	public static final String HIBERNATE = "hibernate";
	/**
	 * Hibernate interceptor class name property ({@code hibernate.ejb.interceptor}).
	 */
	public static final String HIBERNATE_EJB_INTERCEPTOR = "hibernate.ejb.interceptor";
	/**
	 * Default interceptor placeholder ({@code hibernate.empty.interceptor}).
	 */
	public static final String EMPTY_INTERCEPTOR = "hibernate.empty.interceptor";

}
