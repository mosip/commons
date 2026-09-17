package io.mosip.kernel.idobjectvalidator.config;

import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.REFERENCE_VALIDATOR;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;

/**
 * Spring configuration for the optional identity-object reference validator.
 * <p>
 * When {@code mosip.kernel.idobjectvalidator.referenceValidator} is set, the
 * named class is loaded at startup and exposed as the {@code referenceValidator}
 * bean. Otherwise a no-op validator that always returns {@code true} is used.
 * </p>
 *
 * @author Manoj SP
 */
@Configuration
public class IdObjectValidatorConfig {

	private static final Logger logger = LoggerFactory.getLogger(IdObjectValidatorConfig.class);

	/**
	 * Environment used to read {@code mosip.kernel.idobjectvalidator.referenceValidator}.
	 */
	@Autowired
	private Environment env;

	/**
	 * Ensures {@code mosip.kernel.idobjectvalidator.referenceValidator} names a loadable class when set.
	 *
	 * @throws ClassNotFoundException if the configured class cannot be loaded
	 */
	@PostConstruct
	public void validateReferenceValidator() throws ClassNotFoundException {
		if (StringUtils.isNotBlank(env.getProperty(REFERENCE_VALIDATOR))) {
			logger.debug("validating referenceValidator Class is present or not");
			Class.forName(env.getProperty(REFERENCE_VALIDATOR));
		}
		logger.debug("validateReferenceValidator: referenceValidator Class is not provided");
	}

	/**
	 * Instantiates the configured reference {@link IdObjectValidator}, or a no-op that always returns {@code true}.
	 *
	 * @return reference validator bean named {@code referenceValidator}
	 * @throws ClassNotFoundException if the configured class cannot be loaded
	 * @throws InstantiationException if the class cannot be instantiated
	 * @throws IllegalAccessException if the no-arg constructor is not accessible
	 */
	@Bean
	@Lazy
	public IdObjectValidator referenceValidator()
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (StringUtils.isNotBlank(env.getProperty(REFERENCE_VALIDATOR))) {
			logger.debug("instance of referenceValidator is created");
			return (IdObjectValidator) Class.forName(env.getProperty(REFERENCE_VALIDATOR)).newInstance();
		} else {
			logger.debug("no reference validator is provided");
			return new IdObjectValidator() {

				@Override
				public boolean validateIdObject(String identitySchema, Object identityObject,
						List<String> requiredFields)
						throws IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
					return true;
				}
			};
		}
	}
}
