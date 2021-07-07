package io.mosip.kernel.idobjectvalidator.config;

import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.REFERENCE_VALIDATOR;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;

/**
 * The Class IdObjectValidatorConfig.
 *
 * @author Manoj SP
 */
@Configuration
public class IdObjectValidatorConfig {

	@Value("${mosip.kernel.idobjectvalidator.enabled:true}")
	private boolean isEnabled;

	private static final Logger logger = LoggerFactory.getLogger(IdObjectValidatorConfig.class);

	/** The env. */
	@Autowired
	private Environment env;

	/**
	 * Validate reference validator.
	 *
	 * @throws ClassNotFoundException the class not found exception
	 */
	@PostConstruct
	public void validateReferenceValidator() throws ClassNotFoundException {
		if (isEnabled && StringUtils.isNotBlank(env.getProperty(REFERENCE_VALIDATOR))) {
			logger.debug("validating referenceValidator Class is present or not");
			Class.forName(env.getProperty(REFERENCE_VALIDATOR));
		}
		logger.debug("validateReferenceValidator: referenceValidator Class is not provided");
	}

	/**
	 * Reference validator.
	 *
	 * @return the id object validator
	 * @throws ClassNotFoundException    the class not found exception
	 * @throws InstantiationException    the instantiation exception
	 * @throws IllegalAccessException    the illegal access exception
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	@Bean
	@Lazy
	public IdObjectValidator referenceValidator() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (isEnabled && StringUtils.isNotBlank(env.getProperty(REFERENCE_VALIDATOR))) {
			logger.debug("instance of referenceValidator is created");
			return (IdObjectValidator) Class.forName(env.getProperty(REFERENCE_VALIDATOR)).getDeclaredConstructor().newInstance();
		} else {
			logger.debug("no reference validator is provided");
			return new IdObjectValidator() {

				@Override
				public boolean validateIdObject(String identitySchema, Object identityObject, List<String> requiredFields)
						throws IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
					return true;
				}
			};
		}
	}
}