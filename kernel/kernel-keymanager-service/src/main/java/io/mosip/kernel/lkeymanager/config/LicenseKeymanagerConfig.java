package io.mosip.kernel.lkeymanager.config;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import io.mosip.kernel.keymanagerservice.constant.HibernatePersistenceConstant;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
		basePackages = "io.mosip.kernel.lkeymanager.repository",
		entityManagerFactoryRef = "lKeymanagerEntityManagerFactory", 
		transactionManagerRef = "lKeymanagerTransactionManager"
		)
public class LicenseKeymanagerConfig {
	
	@Autowired
	private Environment environment;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LicenseKeymanagerConfig.class);

	@Value("${lkeymanager.hikari.maximumPoolSize:25}")
	private int maximumPoolSize;
	@Value("${lkeymanager.hikari.validationTimeout:3000}")
	private int validationTimeout;
	@Value("${lkeymanager.hikari.connectionTimeout:60000}")
	private int connectionTimeout;
	@Value("${lkeymanager.hikari.idleTimeout:200000}")
	private int idleTimeout;
	@Value("${lkeymanager.hikari.minimumIdle:0}")
	private int minimumIdle;

	@Bean
	public DataSource lKeymanagerDataSource() {

		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(environment.getProperty("licensekeymanager.persistence.jdbc.driver"));
		hikariConfig.setJdbcUrl(environment.getProperty("licensekeymanager_database_url"));
		hikariConfig.setUsername(environment.getProperty("licensekeymanager_database_username"));
		hikariConfig.setPassword(environment.getProperty("licensekeymanager_database_password"));
		if (environment.containsProperty(HibernatePersistenceConstant.LKEYMANAGER_JDBC_SCHEMA)) {
			hikariConfig.setSchema(environment.getProperty(HibernatePersistenceConstant.LKEYMANAGER_JDBC_SCHEMA));
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
	@Bean
	public LocalContainerEntityManagerFactoryBean lKeymanagerEntityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(lKeymanagerDataSource());
		entityManagerFactory.setPackagesToScan("io.mosip.kernel.lkeymanager.entity");
		entityManagerFactory.setPersistenceUnitName(HibernatePersistenceConstant.HIBERNATE);
		entityManagerFactory.setJpaPropertyMap(jpaProperties());
		entityManagerFactory.setJpaVendorAdapter(lKeymanagerJpaVendorAdapter());
		entityManagerFactory.setJpaDialect(lKeymanagerJpaDialect());
		return entityManagerFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#jpaVendorAdapter()
	 */
	@Bean
	public JpaVendorAdapter lKeymanagerJpaVendorAdapter() {
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
	@Bean
	public JpaDialect lKeymanagerJpaDialect() {
		return new HibernateJpaDialect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#transactionManager(javax.
	 * persistence.EntityManagerFactory)
	 */
	@Bean
	public PlatformTransactionManager lKeymanagerTransactionManager() {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
		jpaTransactionManager.setEntityManagerFactory(lKeymanagerEntityManagerFactory().getObject());
		jpaTransactionManager.setDataSource(lKeymanagerDataSource());
		jpaTransactionManager.setJpaDialect(lKeymanagerJpaDialect());
		return jpaTransactionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.dao.config.BaseDaoConfig#jpaProperties()
	 */
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
