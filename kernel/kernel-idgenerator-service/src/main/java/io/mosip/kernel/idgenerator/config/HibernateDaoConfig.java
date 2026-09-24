package io.mosip.kernel.idgenerator.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;
import io.mosip.kernel.vidgenerator.constant.HibernatePersistenceConstant;
import jakarta.persistence.EntityManagerFactory;

/**
 * Spring Hibernate configuration for the Vert.x ID generator process.
 * <p>
 * Scans UIN and VID repositories and entities against {@code id_database_url}.
 * RID has its own datasource in {@link RidHibernateDaoConfig} (see there for
 * why). Spring MVC RID types are excluded; RID HTTP is served by Vert.x
 * {@code RidFetcherRouter}.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @author Raj Jha
 * @since 1.0.0
 *
 */

@Configuration
@PropertySource({ "classpath:bootstrap.properties" })
@PropertySource(value = "classpath:application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
@EnableJpaRepositories(basePackages = { "io.mosip.kernel.vidgenerator.repository", "io.mosip.kernel.uingenerator.repository" },
		repositoryBaseClass = HibernateRepositoryImpl.class)
@EnableAutoConfiguration(excludeName = {
		"io.mosip.kernel.applicanttype.api.impl.ApplicantTypeImpl",
		"io.mosip.kernel.idobjectvalidator.config.IdObjectValidatorConfig",
		"io.mosip.kernel.websub.api.config.IntentVerificationConfig",
		"io.mosip.kernel.websub.api.config.WebSubClientConfig",
		"io.mosip.kernel.websub.api.config.publisher.WebSubPublisherClientConfig",
		"io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper",
		"io.mosip.kernel.idgenerator.rid.impl.RidGeneratorImpl",
		"io.mosip.kernel.idgenerator.tokenid.impl.TokenIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.machineid.impl.MachineIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.regcenterid.impl.RegistrationCenterIdGeneratorImpl",
		"io.mosip.kernel.idgenerator.mispid.impl.MispIdGeneratorImpl",
		"io.mosip.kernel.idvalidator.mispid.impl.MispIdValidatorImpl",
		"io.mosip.kernel.idvalidator.rid.impl.RidValidatorImpl",
		"io.mosip.kernel.idvalidator.prid.impl.PridValidatorImpl",
		"io.mosip.kernel.licensekeygenerator.misp.impl.MISPLicenseKeyGeneratorImpl",
		"io.mosip.kernel.licensekeygenerator.misp.util.MISPLicenseKeyGeneratorUtil"
})
@ComponentScan(basePackages = { "io.mosip.kernel.vidgenerator.*","io.mosip.kernel.uingenerator.*", "io.mosip.kernel.idgenerator.vid.*",
		"io.mosip.kernel.crypto.*", "${mosip.auth.adapter.impl.basepackage}.*","io.mosip.kernel.cryptosignature.*","io.mosip.kernel.idgenerator.*",
		"io.mosip.kernel.ridgenerator.service","io.mosip.kernel.ridgenerator.router","io.mosip.kernel.ridgenerator.repository",
		"io.mosip.kernel.ridgenerator.entity","io.mosip.kernel.ridgenerator.config","io.mosip.kernel.keygenerator.bouncycastle"}, 
excludeFilters = {
		@Filter(type=FilterType.REGEX,pattern="io\\.mosip\\.kernel\\.idgenerator\\.test\\..*"),
		@Filter(type=FilterType.REGEX,pattern="io\\.mosip\\.kernel\\.idgenerator\\.rid\\..*"),
		@Filter(type=FilterType.REGEX,pattern="io\\.mosip\\.kernel\\.idgenerator\\.tokenid\\..*"),
		@Filter(type=FilterType.REGEX,pattern="io\\.mosip\\.kernel\\.idgenerator\\.machineid\\..*"),
		@Filter(type=FilterType.REGEX,pattern="io\\.mosip\\.kernel\\.idgenerator\\.regcenterid\\..*"),
		@Filter(type=FilterType.REGEX,pattern="io\\.mosip\\.kernel\\.idgenerator\\.mispid\\..*"),
		@Filter(type=FilterType.REGEX,pattern="io\\.mosip\\.kernel\\.idgenerator\\.partnerid\\..*"),
		@Filter(type=FilterType.ASSIGNABLE_TYPE, classes = {
				io.mosip.kernel.ridgenerator.exception.ApiExceptionalHandler.class,
				io.mosip.kernel.ridgenerator.dto.AuthorizedRolesDto.class,
				// springdoc is servlet-only; Vert.x / AnnotationConfig tests must not load these
				io.mosip.kernel.ridgenerator.config.SwaggerConfig.class,
				io.mosip.kernel.ridgenerator.config.OpenApiProperties.class
		})
})
@EnableTransactionManagement
public class HibernateDaoConfig implements EnvironmentAware {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateDaoConfig.class);

	/**
	 * Field for {@link #env}
	 */
	@Autowired
	private Environment env;

	/**
	 * Hikari maximum pool size ({@code mosip.kernel.vid.hikari_maximumPoolSize}).
	 */
	@Value("${mosip.kernel.vid.hikari_maximumPoolSize:10}")
	private int maximumPoolSize;
	/**
	 * Hikari connection validation timeout in milliseconds ({@code hikari.validationTimeout}).
	 */
	@Value("${hikari.validationTimeout:3000}")
	private int validationTimeout;
	/**
	 * Hikari connection acquisition timeout in milliseconds ({@code hikari.connectionTimeout}).
	 */
	@Value("${hikari.connectionTimeout:60000}")
	private int connectionTimeout;
	/**
	 * Hikari idle timeout in milliseconds ({@code hikari.idleTimeout}).
	 */
	@Value("${hikari.idleTimeout:200000}")
	private int idleTimeout;
	/**
	 * Hikari minimum idle connections ({@code hikari.minimumIdle}).
	 */
	@Value("${hikari.minimumIdle:0}")
	private int minimumIdle;

	/**
	 * Stores the Spring {@link Environment} used to resolve JDBC and Hibernate properties.
	 *
	 * @param environment active Spring environment
	 */
	@Override
	public void setEnvironment(final Environment environment) {
		this.env = environment;
	}

	/**
	 * A factory for connections to the physical data source that this DataSource
	 * object represents.
	 * 
	 * @return dataSource
	 */
	@Bean
	@Primary
	public DataSource dataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(env.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_DRIVER));
		hikariConfig.setJdbcUrl(env.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_URL));
		hikariConfig.setUsername(env.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_USER));
		hikariConfig.setPassword(env.getProperty(HibernatePersistenceConstant.JAVAX_PERSISTENCE_JDBC_PASS));
		hikariConfig.setMaximumPoolSize(maximumPoolSize);
		hikariConfig.setValidationTimeout(validationTimeout);
		hikariConfig.setConnectionTimeout(connectionTimeout);
		hikariConfig.setIdleTimeout(idleTimeout);
		hikariConfig.setMinimumIdle(minimumIdle);
		return new HikariDataSource(hikariConfig);
	}

	/**
	 * Set up a shared JPA EntityManagerFactory in a Spring application context
	 * 
	 * @param dataSource dataSource
	 * @return LocalContainerEntityManagerFactoryBean
	 */
	@Bean
	@Primary
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("dataSource") final DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactory.setDataSource(dataSource);
		entityManagerFactory.setPackagesToScan("io.mosip.kernel.vidgenerator.entity","io.mosip.kernel.uingenerator.entity");
		entityManagerFactory.setJpaPropertyMap(jpaProperties());
		entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		return entityManagerFactory;
	}

	/**
	 * This is the central interface in Spring's transaction infrastructure.
	 * 
	 * @param entityManagerFactory entityManagerFactory
	 * @return PlatformTransactionManager
	 */
	@Bean(name = "transactionManager")
	@Primary
	public PlatformTransactionManager transactionManager(
			@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(entityManagerFactory);
		jpaTransactionManager.setDataSource(dataSource());
		return jpaTransactionManager;
	}

	/**
	 * Builds Hibernate JPA properties from the environment, applying MOSIP defaults when a key is absent.
	 *
	 * @return Hibernate property map used by the entity manager factory
	 */
	public Map<String, Object> jpaProperties() {
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

	/**
	 * Puts {@code property} from the environment into {@code jpaProperties}, or {@code defaultValue} when absent.
	 *
	 * @param jpaProperties accumulator map
	 * @param property      Hibernate / JPA property key
	 * @param defaultValue  fallback when the environment does not define {@code property}
	 * @return {@code jpaProperties} for chaining
	 */
	private HashMap<String, Object> getProperty(HashMap<String, Object> jpaProperties, String property,
			String defaultValue) {
		/**
		 * if property found in properties file then add that interceptor to the jpa
		 * properties.
		 */
		if (property.equals(HibernatePersistenceConstant.HIBERNATE_EJB_INTERCEPTOR)) {
			try {
				if (env.containsProperty(property)) {
					jpaProperties.put(property, BeanUtils.instantiateClass(Class.forName(env.getProperty(property))));
				}
				/**
				 * We can add a default interceptor whenever we require here.
				 */
			} catch (BeanInstantiationException | ClassNotFoundException e) {
				LOGGER.error(e.getMessage());
			}
		} else {
			jpaProperties.put(property, env.containsProperty(property) ? env.getProperty(property) : defaultValue);
		}
		return jpaProperties;
	}

}