package io.mosip.kernel.dataaccess.hibernate.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.mosip.kernel.core.dataaccess.spi.config.BaseDaoConfig;
import io.mosip.kernel.dataaccess.hibernate.constant.HibernatePersistenceConstant;
import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;

/**
 * Hibernate / JPA {@link BaseDaoConfig} beans for MOSIP services.
 * <p>
 * Active only when {@code javax.persistence.jdbc.url} is set. Registers a
 * HikariCP {@link DataSource}, {@link LocalContainerEntityManagerFactoryBean},
 * Hibernate vendor adapter, dialect, and {@link JpaTransactionManager}, and
 * scans {@code io.mosip.*} with {@link HibernateRepositoryImpl} as the
 * repository base class.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @author Raj Jha
 * @since 1.0.0
 * @see BaseDaoConfig
 */
@Configuration
@ConditionalOnProperty(name = HibernatePersistenceConstant.JDBC_URL)
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = HibernatePersistenceConstant.MOSIP_PACKAGE, repositoryBaseClass = HibernateRepositoryImpl.class)
public class HibernateDaoConfig implements BaseDaoConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateDaoConfig.class);

	/**
	 * Field for interface representing the environment in which the current
	 * application is running.
	 */
	@Autowired
	private Environment environment;

	/** Hikari maximum pool size from {@code hikari.maximumPoolSize}. */
	@Value("${hikari.maximumPoolSize:25}")
	private int maximumPoolSize;
	/** Hikari validation timeout in milliseconds from {@code hikari.validationTimeout}. */
	@Value("${hikari.validationTimeout:3000}")
	private int validationTimeout;
	/** Hikari connection-acquire timeout in milliseconds from {@code hikari.connectionTimeout}. */
	@Value("${hikari.connectionTimeout:60000}")
	private int connectionTimeout;
	/** Hikari idle timeout in milliseconds from {@code hikari.idleTimeout}. */
	@Value("${hikari.idleTimeout:200000}")
	private int idleTimeout;
	/** Hikari minimum idle connections from {@code hikari.minimumIdle}. */
	@Value("${hikari.minimumIdle:0}")
	private int minimumIdle;

	/**
	 * Builds a HikariCP {@link DataSource} from JDBC environment properties.
	 *
	 * @return pooled data source using {@code javax.persistence.jdbc.*} settings
	 */
	@Override
	@Bean
	public DataSource dataSource() {
		// DriverManagerDataSource dataSource = new DriverManagerDataSource();
		// dataSource.setDriverClassName(environment.getProperty(HibernatePersistenceConstant.JDBC_DRIVER));
		// dataSource.setUrl(environment.getProperty(HibernatePersistenceConstant.JDBC_URL));
		// dataSource.setUsername(environment.getProperty(HibernatePersistenceConstant.JDBC_USER));
		// dataSource.setPassword(environment.getProperty(HibernatePersistenceConstant.JDBC_PASS));

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(environment.getProperty(HibernatePersistenceConstant.JDBC_DRIVER));
		hikariConfig.setJdbcUrl(environment.getProperty(HibernatePersistenceConstant.JDBC_URL));
		hikariConfig.setUsername(environment.getProperty(HibernatePersistenceConstant.JDBC_USER));
		hikariConfig.setPassword(environment.getProperty(HibernatePersistenceConstant.JDBC_PASS));
		if (environment.containsProperty(HibernatePersistenceConstant.JDBC_SCHEMA)) {
			hikariConfig.setSchema(environment.getProperty(HibernatePersistenceConstant.JDBC_SCHEMA));
		}
		hikariConfig.setMaximumPoolSize(maximumPoolSize);
		hikariConfig.setValidationTimeout(validationTimeout);
		hikariConfig.setConnectionTimeout(connectionTimeout);
		hikariConfig.setIdleTimeout(idleTimeout);
		hikariConfig.setMinimumIdle(minimumIdle);
		HikariDataSource dataSource = new HikariDataSource(hikariConfig);

		return dataSource;
	}

	/**
	 * Builds the JPA entity manager factory for {@code io.mosip.*} entities.
	 *
	 * @return container {@link LocalContainerEntityManagerFactoryBean}
	 */
	@Override
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(dataSource());
		entityManagerFactory.setPackagesToScan(HibernatePersistenceConstant.MOSIP_PACKAGE);
		entityManagerFactory.setPersistenceUnitName(HibernatePersistenceConstant.HIBERNATE);
		entityManagerFactory.setJpaPropertyMap(jpaProperties());
		entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter());
		entityManagerFactory.setJpaDialect(jpaDialect());
		return entityManagerFactory;
	}

	/**
	 * Returns a Hibernate vendor adapter with DDL generation and SQL logging on.
	 *
	 * @return Hibernate {@link JpaVendorAdapter}
	 */
	@Override
	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);
		vendorAdapter.setShowSql(true);
		return vendorAdapter;
	}

	/**
	 * Returns the Hibernate JPA dialect.
	 *
	 * @return {@link HibernateJpaDialect}
	 */
	@Override
	@Bean
	public JpaDialect jpaDialect() {
		return new HibernateJpaDialect();
	}

	/**
	 * Builds a JPA transaction manager bound to {@code entityManagerFactory} and {@link #dataSource()}.
	 *
	 * @param entityManagerFactory JPA factory produced by {@link #entityManagerFactory()}
	 * @return JPA {@link PlatformTransactionManager}
	 */
	@Override
	@Bean
	public PlatformTransactionManager transactionManager(jakarta.persistence.EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(entityManagerFactory);
		jpaTransactionManager.setDataSource(dataSource());
		jpaTransactionManager.setJpaDialect(jpaDialect());
		return jpaTransactionManager;
	}

	/**
	 * Collects Hibernate properties from the environment, applying MOSIP defaults.
	 *
	 * @return map of Hibernate / JPA property names to values
	 */
	@Override
	public Map<String, Object> jpaProperties() {
		HashMap<String, Object> jpaProperties = new HashMap<>();
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_HBM2DDL_AUTO,
				HibernatePersistenceConstant.UPDATE);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_DIALECT,
				HibernatePersistenceConstant.MY_SQL5_DIALECT);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_SHOW_SQL, HibernatePersistenceConstant.TRUE);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_FORMAT_SQL,
				HibernatePersistenceConstant.TRUE);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_CONNECTION_CHAR_SET,
				HibernatePersistenceConstant.UTF8);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_CACHE_USE_SECOND_LEVEL_CACHE,
				HibernatePersistenceConstant.FALSE);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_CACHE_USE_QUERY_CACHE,
				HibernatePersistenceConstant.FALSE);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_CACHE_USE_STRUCTURED_ENTRIES,
				HibernatePersistenceConstant.FALSE);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_GENERATE_STATISTICS,
				HibernatePersistenceConstant.FALSE);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_NON_CONTEXTUAL_CREATION,
				HibernatePersistenceConstant.FALSE);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_CURRENT_SESSION_CONTEXT,
				HibernatePersistenceConstant.JTA);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_EJB_INTERCEPTOR,
				HibernatePersistenceConstant.EMPTY_INTERCEPTOR);
		return jpaProperties;
	}

//	@Bean
//	public RestTemplate restTemplate()
//	{
//		return new RestTemplate();
//	}
//	@Profile("!test")
//	@Bean
//	public EncryptionInterceptor encryptionInterceptor() {
//		return new EncryptionInterceptor();
//	}

	/**
	 * Function to associate the specified value with the specified key in the map.
	 * If the map previously contained a mapping for the key, the old value is
	 * replaced.
	 * 
	 * @param jpaProperties The map of jpa properties
	 * @param property      The property whose value is to be set
	 * @param defaultValue  The default value to set
	 * @return The map of jpa properties with properties set
	 */
	private HashMap<String, Object> getProperty(HashMap<String, Object> jpaProperties, String property,
			String defaultValue) {
		/**
		 * if property found in properties file then add that interceptor to the jpa
		 * properties.
		 */
		if (property.equals(HibernatePersistenceConstant.HIBERNATE_EJB_INTERCEPTOR)) {
			try {
				if (environment.containsProperty(property)) {
					jpaProperties.put(property,
							// encryptionInterceptor());
							BeanUtils.instantiateClass(Class.forName(environment.getProperty(property))));
				}
				/**
				 * We can add a default interceptor whenever we require here.
				 */
			} catch (BeanInstantiationException | ClassNotFoundException e) {
				LOGGER.error("Error while configuring Interceptor.");
			}
		} else {
			jpaProperties.put(property,
					environment.containsProperty(property) ? environment.getProperty(property) : defaultValue);
		}
		return jpaProperties;
	}

}
