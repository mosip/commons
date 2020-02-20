package io.mosip.kernel.idobjectvalidator.impl;

import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.ID_OBJECT_PARSING_FAILED;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.ID_OBJECT_VALIDATION_FAILED;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.INVALID_INPUT_PARAMETER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import static com.jayway.jsonpath.Option.*;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorSupportedOperations;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;
import net.minidev.json.JSONArray;

/**
 * The Class IdObjectPatternValidator.
 *
 * @author Manoj SP
 */
@Component("pattern")
@ConfigurationProperties("mosip.id")
public class IdObjectPatternValidator implements IdObjectValidator {

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	/** The validation. */
	private Map<String, String> validation;

	/**
	 * Sets the validation.
	 *
	 * @param validation the validation to set
	 */
	public void setValidation(Map<String, String> validation) {
		this.validation = validation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator#validateIdObject
	 * (java.lang.Object)
	 */
	@Override
	public boolean validateIdObject(Object identityObject, IdObjectValidatorSupportedOperations operation)
			throws IdObjectIOException, IdObjectValidationFailedException {
		try {
			String identityString = mapper.writeValueAsString(identityObject);
			List<ServiceError> errorList = new ArrayList<>();
			validateAttributes(identityString, errorList);
			if (errorList.isEmpty()) {
				return true;
			} else {
				throw new IdObjectValidationFailedException(ID_OBJECT_VALIDATION_FAILED, errorList);
			}
		} catch (JsonProcessingException e) {
			ExceptionUtils.logRootCause(e);
			throw new IdObjectIOException(ID_OBJECT_PARSING_FAILED, e);
		}
	}

	/**
	 * Validates json attributes configured in the external properties.
	 *
	 * @param identity  the request
	 * @param errorList the error list
	 */
	private void validateAttributes(String identity, List<ServiceError> errorList) {
		validation.entrySet().parallelStream().forEach(entry -> {
			JsonPath jsonPath = JsonPath.compile(entry.getKey());
			Pattern pattern = Pattern.compile(entry.getValue());
			JSONArray data = jsonPath.read(identity,
					Configuration.defaultConfiguration().addOptions(SUPPRESS_EXCEPTIONS, ALWAYS_RETURN_LIST));
			if (Objects.nonNull(data) && !data.isEmpty()) {
				IntStream.range(0, data.size()).parallel()
						.filter(index -> !pattern.matcher(String.valueOf(data.get(index))).matches()).forEach(index -> {
							JSONArray pathList = jsonPath.read(identity, Configuration.defaultConfiguration()
									.addOptions(SUPPRESS_EXCEPTIONS, ALWAYS_RETURN_LIST, AS_PATH_LIST));
							errorList.add(new ServiceError(INVALID_INPUT_PARAMETER.getErrorCode(),
									String.format(INVALID_INPUT_PARAMETER.getMessage(),
											convertToPath(String.valueOf(pathList.get(index))))));
						});
			}
		});
	}

	/**
	 * Convert to path.
	 *
	 * @param jsonPath the json path
	 * @return the string
	 */
	private String convertToPath(String jsonPath) {
		String path = String.valueOf(jsonPath.replaceAll("[$']", ""));
		return path.substring(1, path.length() - 1).replace("][", "/");
	}

}
