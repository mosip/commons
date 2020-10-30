package io.mosip.kernel.keymanagerservice.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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

import io.mosip.kernel.keymanagerservice.constant.HibernatePersistenceConstant;


@ConditionalOnProperty(value = "mosip.keymanager.dao.enabled", matchIfMissing = true)
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = {"io.mosip.kernel.keymanagerservice.repository", "io.mosip.kernel.lkeymanager.repository"}, 
		entityManagerFactoryRef = "keymanagerEntityManagerFactory", 
		transactionManagerRef = "keymanagerTransactionManager")
public class KeymanagerDaoConfig {
	
	@Autowired
	private Environment environment;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KeymanagerDaoConfig.class);

	@Value("${keymanager.hikari.maximumPoolSize:25}")
	private int maximumPoolSize;
	@Value("${keymanager.hikari.validationTimeout:3000}")
	private int validationTimeout;
	@Value("${keymanager.hikari.connectionTimeout:60000}")
	private int connectionTimeout;
	@Value("${keymanager.hikari.idleTimeout:200000}")
	private int idleTimeout;
	@Value("${keymanager.hikari.minimumIdle:0}")
	private int minimumIdle;

	@Primary
	@Bean
	public DataSource keymanagerDataSource() {

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(environment.getProperty("keymanager.persistence.jdbc.driver"));
		hikariConfig.setJdbcUrl(environment.getProperty("keymanager_database_url"));
		hikariConfig.setUsername(environment.getProperty("keymanager_database_username"));
		hikariConfig.setPassword(environment.getProperty("keymanager_database_password"));
		if (environment.containsProperty(HibernatePersistenceConstant.KEYMANAGER_JDBC_SCHEMA)) {
			hikariConfig.setSchema(environment.getProperty(HibernatePersistenceConstant.KEYMANAGER_JDBC_SCHEMA));
		}
		hikariConfig.setMaximumPoolSize(maximumPoolSize);
		hikariConfig.setValidationTimeout(validationTimeout);
		hikariConfig.setConnectionTimeout(connectionTimeout);
		hikariConfig.setIdleTimeout(idleTimeout);
		hikariConfig.setMinimumIdle(minimumIdle);

		return new HikariDataSource(hikariConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#entityManagerFactory()
	 */
	@Primary
	@Bean
	public LocalContainerEntityManagerFactoryBean keymanagerEntityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(keymanagerDataSource());
		entityManagerFactory.setPackagesToScan(new String [] 
													{"io.mosip.kernel.keymanagerservice.entity",
													 "io.mosip.kernel.lkeymanager.entity" });
		entityManagerFactory.setPersistenceUnitName(HibernatePersistenceConstant.HIBERNATE);
		entityManagerFactory.setJpaPropertyMap(keymanagerJpaProperties());
		entityManagerFactory.setJpaVendorAdapter(keymanagerJpaVendorAdapter());
		entityManagerFactory.setJpaDialect(keymanagerJpaDialect());
		return entityManagerFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#jpaVendorAdapter()
	 */
	@Primary
	@Bean
	public JpaVendorAdapter keymanagerJpaVendorAdapter() {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);
		vendorAdapter.setShowSql(true);
		return vendorAdapter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#jpaDialect()
	 */
	@Primary
	@Bean
	public JpaDialect keymanagerJpaDialect() {
		return new HibernateJpaDialect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#transactionManager(javax.
	 * persistence.EntityManagerFactory)
	 */
	@Primary
	@Bean
	public PlatformTransactionManager keymanagerTransactionManager() {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
		jpaTransactionManager.setEntityManagerFactory(keymanagerEntityManagerFactory().getObject());
		jpaTransactionManager.setDataSource(keymanagerDataSource());
		jpaTransactionManager.setJpaDialect(keymanagerJpaDialect());
		return jpaTransactionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#jpaProperties()
	 */
	public Map<String, Object> keymanagerJpaProperties() {
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
