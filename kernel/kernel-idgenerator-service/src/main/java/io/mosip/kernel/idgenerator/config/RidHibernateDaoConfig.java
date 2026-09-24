package io.mosip.kernel.idgenerator.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;
import io.mosip.kernel.vidgenerator.constant.HibernatePersistenceConstant;
import jakarta.persistence.EntityManagerFactory;

/**
 * Dedicated Hibernate wiring for {@code regprc.rid_seq}.
 * <p>
 * Before the services were consolidated into this module, RID persistence ran
 * as its own Spring Boot app against {@code ridgenerator_database_url}
 * (historically {@code mosip_regprc} — a different physical database than
 * UIN/VID's {@code id_database_url}, which is {@code mosip_kernel}). {@link
 * HibernateDaoConfig}'s single datasource only ever pointed at
 * {@code id_database_url}, so folding RID's entity into that config left RID
 * querying a database that never had {@code regprc.rid_seq}. This restores
 * RID's own connection without touching schema or data: it reads
 * {@code ridgenerator_database_url}/{@code _username}/{@code _password} when
 * config-server defines them (as it already does), and falls back to the
 * {@code id_database_url} triple otherwise, so local H2 and every existing
 * test — none of which set the {@code ridgenerator_database_*} keys — see no
 * change.
 * </p>
 */
@Configuration
@EnableJpaRepositories(basePackages = "io.mosip.kernel.ridgenerator.repository",
		repositoryBaseClass = HibernateRepositoryImpl.class, entityManagerFactoryRef = "ridEntityManagerFactory",
		transactionManagerRef = "ridTransactionManager")
public class RidHibernateDaoConfig implements EnvironmentAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(RidHibernateDaoConfig.class);

	@Autowired
	private Environment env;

	@Value("${mosip.kernel.rid.hikari_maximumPoolSize:10}")
	private int maximumPoolSize;
	@Value("${hikari.validationTimeout:3000}")
	private int validationTimeout;
	@Value("${hikari.connectionTimeout:60000}")
	private int connectionTimeout;
	@Value("${hikari.idleTimeout:200000}")
	private int idleTimeout;
	@Value("${hikari.minimumIdle:0}")
	private int minimumIdle;

	@Override
	public void setEnvironment(final Environment environment) {
		this.env = environment;
	}

	/**
	 * RID's own connection pool, defaulting to the shared UIN/VID datasource
	 * when {@code ridgenerator_database_url} is not configured.
	 *
	 * @return ridDataSource
	 */
	@Bean
	public DataSource ridDataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(env.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_DRIVER));
		hikariConfig.setJdbcUrl(env.getProperty("ridgenerator_database_url",
				env.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_URL)));
		hikariConfig.setUsername(env.getProperty("ridgenerator_database_username",
				env.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_USER)));
		hikariConfig.setPassword(env.getProperty("ridgenerator_database_password",
				env.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_PASS)));
		hikariConfig.setMaximumPoolSize(maximumPoolSize);
		hikariConfig.setValidationTimeout(validationTimeout);
		hikariConfig.setConnectionTimeout(connectionTimeout);
		hikariConfig.setIdleTimeout(idleTimeout);
		hikariConfig.setMinimumIdle(minimumIdle);
		return new HikariDataSource(hikariConfig);
	}

	/**
	 * JPA EntityManagerFactory scoped to {@code io.mosip.kernel.ridgenerator.entity} only.
	 *
	 * @param ridDataSource RID's connection pool
	 * @return ridEntityManagerFactory
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean ridEntityManagerFactory(
			@Qualifier("ridDataSource") final DataSource ridDataSource) {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(ridDataSource);
		entityManagerFactory.setPersistenceUnitName("rid");
		entityManagerFactory.setPackagesToScan("io.mosip.kernel.ridgenerator.entity");
		entityManagerFactory.setJpaPropertyMap(jpaProperties());
		entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		return entityManagerFactory;
	}

	/**
	 * Transaction manager bound to {@link #ridEntityManagerFactory}, referenced
	 * by name from {@code @EnableJpaRepositories} above.
	 *
	 * @param ridEntityManagerFactory RID's entity manager factory
	 * @return ridTransactionManager
	 */
	@Bean(name = "ridTransactionManager")
	public PlatformTransactionManager ridTransactionManager(
			@Qualifier("ridEntityManagerFactory") EntityManagerFactory ridEntityManagerFactory) {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(ridEntityManagerFactory);
		jpaTransactionManager.setDataSource(ridDataSource());
		return jpaTransactionManager;
	}

	private Map<String, Object> jpaProperties() {
		HashMap<String, Object> jpaProperties = new HashMap<>();
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_HBM2DDL_AUTO,
				HibernatePersistenceConstant.UPDATE);
		getProperty(jpaProperties, HibernatePersistenceConstant.HIBERNATE_DIALECT, null);
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

	private void getProperty(HashMap<String, Object> jpaProperties, String property, String defaultValue) {
		if (property.equals(HibernatePersistenceConstant.HIBERNATE_EJB_INTERCEPTOR)) {
			try {
				if (env.containsProperty(property)) {
					jpaProperties.put(property, BeanUtils.instantiateClass(Class.forName(env.getProperty(property))));
				}
			} catch (BeanInstantiationException | ClassNotFoundException e) {
				LOGGER.error(e.getMessage());
			}
		} else {
			jpaProperties.put(property, env.containsProperty(property) ? env.getProperty(property) : defaultValue);
		}
	}

}
