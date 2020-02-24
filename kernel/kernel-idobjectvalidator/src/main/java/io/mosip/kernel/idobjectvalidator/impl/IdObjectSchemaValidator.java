package io.mosip.kernel.idobjectvalidator.impl;

import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.ID_OBJECT_PARSING_FAILED;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.ID_OBJECT_VALIDATION_FAILED;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.INVALID_INPUT_PARAMETER;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.MISSING_INPUT_PARAMETER;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.SCHEMA_IO_EXCEPTION;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorPropertySourceConstant.APPLICATION_CONTEXT;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorPropertySourceConstant.CONFIG_SERVER;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorPropertySourceConstant.LOCAL;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.APPLICATION_ID;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.ERROR;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.FIELD_LIST;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.INSTANCE;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.PATH_SEPERATOR;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.POINTER;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.ROOT_PATH;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.IDENTITY_ARRAY_VALUE_FIELD;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorSupportedOperations;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;
import io.mosip.kernel.core.util.StringUtils;

/**
 * This class provides the implementation for JSON validation against the
 * schema.
 * 
 * @author Manoj SP
 * @author Swati Raj
 * 
 */
@Component("schema")
@RefreshScope
public class IdObjectSchemaValidator implements IdObjectValidator {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(IdObjectSchemaValidator.class);

	/** The Constant OPERATION. */
	private static final String OPERATION = "operation";

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	/** The env. */
	@Autowired
	private Environment env;

	/** The Constant MISSING. */
	private static final String MISSING = "missing";

	/** The Constant UNWANTED. */
	private static final String UNWANTED = "unwanted";

	/**
	 * The config server file storage URL.
	 *
	 * Address of Spring cloud config server for getting the schema file
	 */
	@Value("${mosip.kernel.idobjectvalidator.file-storage-uri}")
	private String configServerFileStorageURL;

	/** The schema name. */
	@Value("${mosip.kernel.idobjectvalidator.schema-name}")
	private String schemaName;

	/**
	 * The property source. /* Property source from which schema file has to be
	 * taken, can be either CONFIG_SERVER or LOCAL
	 */
	@Value("${mosip.kernel.idobjectvalidator.property-source}")
	private String propertySource;

	/** The schema. */
	private JsonNode schema;

	/**
	 * Load schema.
	 *
	 * @throws IdObjectIOException the id object IO exception
	 */
	@PostConstruct
	public void loadSchema() throws IdObjectIOException {
		try {
			if (APPLICATION_CONTEXT.getPropertySource().equals(propertySource)) {
				logger.debug("schema loaded from application context");
				schema = JsonLoader.fromURL(new URL(configServerFileStorageURL + schemaName));
			}
		} catch (IOException e) {
			ExceptionUtils.logRootCause(e);
			throw new IdObjectIOException(SCHEMA_IO_EXCEPTION, e);
		}
	}

	/**
	 * Validates a JSON object passed as string with the schema provided.
	 *
	 * @param idObject  the id object
	 * @param operation the operation
	 * @return true, if successful
	 * @throws IdObjectValidationFailedException the id object validation failed
	 *                                           exception
	 * @throws IdObjectIOException               the id object IO exception
	 */
	@Override
	public boolean validateIdObject(Object idObject, IdObjectValidatorSupportedOperations operation)
			throws IdObjectValidationFailedException, IdObjectIOException {
		JsonNode jsonObjectNode = null;
		JsonNode jsonSchemaNode = null;
		ProcessingReport report = null;
		try {
			jsonObjectNode = mapper.readTree(mapper.writeValueAsString(idObject));
			jsonSchemaNode = getJsonSchemaNode();
			final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
			final JsonSchema jsonSchema = factory.getJsonSchema(jsonSchemaNode);
			report = jsonSchema.validate(jsonObjectNode);
			logger.debug("schema validation report generated : " + report);
			List<ServiceError> errorList = new ArrayList<>();
			if (!report.isSuccess()) {
				report.forEach(processingMessage -> {
					if (processingMessage.getLogLevel().toString().equals(ERROR)) {
						JsonNode processingMessageAsJson = processingMessage.asJson();
						if (processingMessageAsJson.hasNonNull(INSTANCE)
								&& processingMessageAsJson.get(INSTANCE).hasNonNull(POINTER)) {
							if (processingMessageAsJson.has(MISSING)
									&& !processingMessageAsJson.get(MISSING).isNull()) {
								errorList.add(new ServiceError(MISSING_INPUT_PARAMETER.getErrorCode(),
										buildErrorMessage(processingMessageAsJson, MISSING_INPUT_PARAMETER.getMessage(),
												MISSING)));
							} else {
								errorList.add(new ServiceError(INVALID_INPUT_PARAMETER.getErrorCode(),
										buildErrorMessage(processingMessageAsJson, INVALID_INPUT_PARAMETER.getMessage(),
												UNWANTED)));
							}
						}
					}
				});
			}
			validateMandatoryFields(jsonObjectNode, operation, errorList);
			if (!errorList.isEmpty()) {
				logger.debug("IdObjectValidationFailedException thrown with errors : " + errorList);
				throw new IdObjectValidationFailedException(ID_OBJECT_VALIDATION_FAILED, errorList);
			}
			return true;
		} catch (IOException e) {
			ExceptionUtils.logRootCause(e);
			throw new IdObjectIOException(ID_OBJECT_PARSING_FAILED, e);
		} catch (ProcessingException e) {
			ExceptionUtils.logRootCause(e);
			throw new IdObjectIOException(ID_OBJECT_VALIDATION_FAILED, e);
		}
	}

	/**
	 * Validate mandatory fields.
	 *
	 * @param jsonObjectNode the json object node
	 * @param operation      the operation
	 * @param errorList      the error list
	 * @throws IdObjectIOException the id object IO exception
	 */
	private void validateMandatoryFields(JsonNode jsonObjectNode, IdObjectValidatorSupportedOperations operation,
			List<ServiceError> errorList) throws IdObjectIOException {
		if (Objects.isNull(operation)) {
			logger.debug("mandatory field input operation is null");
			throw new IdObjectIOException(MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(MISSING_INPUT_PARAMETER.getMessage(), OPERATION));
		}
		String appId = env.getProperty(APPLICATION_ID);
		if (Objects.isNull(appId)) {
			logger.debug("mandatory field input appId is null");
			throw new IdObjectIOException(MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(MISSING_INPUT_PARAMETER.getMessage(), APPLICATION_ID));
		}
		String fields = env.getProperty(String.format(FIELD_LIST, appId, operation.getOperation()));
		Optional.ofNullable(fields).ifPresent(fieldList -> Arrays.asList(StringUtils.split(fields, ','))
				.parallelStream().map(StringUtils::normalizeSpace).forEach(field -> {
					List<String> fieldNames = Arrays.asList(field.split("\\|"));
					fieldNames = fieldNames.stream()
							.map(fieldName -> PATH_SEPERATOR
									.concat(ROOT_PATH.concat(PATH_SEPERATOR.concat(fieldName.replace('.', '/')))))
							.collect(Collectors.toList());
					validateMissingFields(jsonObjectNode, errorList, fieldNames);
					validateInvalidFields(jsonObjectNode, errorList, fieldNames);
				}));
	}

	/**
	 * Validate missing fields.
	 *
	 * @param jsonObjectNode the json object node
	 * @param errorList      the error list
	 * @param fieldNames     the field names
	 */
	private void validateMissingFields(JsonNode jsonObjectNode, List<ServiceError> errorList, List<String> fieldNames) {
		if (fieldNames.parallelStream().allMatch(fieldName -> isMissingOrEmpty(jsonObjectNode, fieldName))) {
			errorList.add(new ServiceError(MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(MISSING_INPUT_PARAMETER.getMessage(),
							fieldNames.parallelStream().map(fieldName -> fieldName.replaceFirst(PATH_SEPERATOR, ""))
									.collect(Collectors.joining(" | ")))));
		}
	}

	/**
	 * Validate invalid fields.
	 *
	 * @param jsonObjectNode the json object node
	 * @param errorList      the error list
	 * @param fieldNames     the field names
	 */
	private void validateInvalidFields(JsonNode jsonObjectNode, List<ServiceError> errorList, List<String> fieldNames) {
		if (fieldNames.parallelStream().anyMatch(
				fieldName -> !isMissingOrEmpty(jsonObjectNode, fieldName) && (jsonObjectNode.at(fieldName).isArray()
						? jsonObjectNode.findValuesAsText(IDENTITY_ARRAY_VALUE_FIELD).stream()
								.allMatch(StringUtils::isBlank)
						: StringUtils.isBlank(jsonObjectNode.at(fieldName).toString())))) {
			errorList.add(new ServiceError(INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getMessage(),
							fieldNames.parallelStream().map(fieldName -> fieldName.replaceFirst(PATH_SEPERATOR, ""))
									.collect(Collectors.joining(" | ")))));
		}
	}

	/**
	 * Checks if is missing or empty.
	 *
	 * @param jsonObjectNode the json object node
	 * @param fieldName      the field name
	 * @return true, if is missing or empty
	 */
	private boolean isMissingOrEmpty(JsonNode jsonObjectNode, String fieldName) {
		return jsonObjectNode.at(fieldName).isMissingNode()
				|| StringUtils.isEmpty(jsonObjectNode.at(fieldName).toString());
	}

	/**
	 * Builds the error message.
	 *
	 * @param processingMessageAsJson the processing message as json
	 * @param messageBody             the message body
	 * @param field                   the field
	 * @return the string
	 */
	private String buildErrorMessage(JsonNode processingMessageAsJson, String messageBody, String field) {
		return String.format(messageBody, StringUtils.strip(
				processingMessageAsJson.get(INSTANCE).get(POINTER).asText() + (processingMessageAsJson.hasNonNull(field)
						? PATH_SEPERATOR
								+ StringUtils.removeAll(processingMessageAsJson.get(field).toString(), "[\\[\"\\]]")
						: ""),
				"/"));
	}

	/**
	 * Gets the json schema node. If the property source selected is CONFIG_SERVER.
	 * In this scenario schema is coming from Config Server, whose location has to
	 * be mentioned in the bootstrap.properties by the application using this JSON
	 * validator API. If the property source selected is local. In this scenario
	 * schema is coming from local resource location. If the property source is
	 * APPLICATION_CONTEXT, schema is loaded using PostConstruct for one time, and
	 * loaded schema is reused for validation.
	 * 
	 * @return the json schema node
	 * @throws IdObjectIOException the id object IO exception
	 */
	private JsonNode getJsonSchemaNode() throws IdObjectIOException {
		logger.debug("propertySource is set to " + propertySource);
		JsonNode jsonSchemaNode = null;
		if (CONFIG_SERVER.getPropertySource().equals(propertySource)) {
			try {
				jsonSchemaNode = JsonLoader.fromURL(new URL(configServerFileStorageURL + schemaName));
				logger.debug("schema is loaded from config server");
			} catch (IOException e) {
				ExceptionUtils.logRootCause(e);
				throw new IdObjectIOException(SCHEMA_IO_EXCEPTION, e);
			}
		} else if (LOCAL.getPropertySource().equals(propertySource)) {
			try {
				jsonSchemaNode = JsonLoader.fromResource(PATH_SEPERATOR + schemaName);
				logger.debug("schema is loaded from LOCAL");
			} catch (IOException e) {
				ExceptionUtils.logRootCause(e);
				throw new IdObjectIOException(SCHEMA_IO_EXCEPTION.getErrorCode(), SCHEMA_IO_EXCEPTION.getMessage(),
						e.getCause());
			}
		} else if (APPLICATION_CONTEXT.getPropertySource().equals(propertySource)) {
			jsonSchemaNode = schema;
			logger.debug("schema is loaded from APPLICATION_CONTEXT");
		}
		return jsonSchemaNode;
	}
}