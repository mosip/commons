package io.mosip.kernel.idobjectvalidator.impl;

import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.ID_OBJECT_VALIDATION_FAILED;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.INVALID_INPUT_PARAMETER;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.MISSING_INPUT_PARAMETER;
import static io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant.SCHEMA_IO_EXCEPTION;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.ERROR;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.INSTANCE;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.KEYWORD;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.PATH_SEPERATOR;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.POINTER;
import static io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant.VALIDATORS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;
import io.mosip.kernel.idobjectvalidator.helper.IdObjectValidatorHelper;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class IdObjectSchemaValidator implements IdObjectValidator {

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	/** The Constant MISSING. */
	private static final String MISSING = "missing";

	/** The Constant UNWANTED. */
	private static final String UNWANTED = "unwanted";

	private static final String TYPE = "type";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator#validateIdObject
	 * (java.lang.String, java.lang.Object, java.util.List)
	 */
	@Override
	public boolean validateIdObject(String identitySchema, Object identityObject, List<String> requiredFields)
			throws IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		try {
			final JsonSchema jsonSchema = getJsonSchema(identitySchema);
			JsonNode identityJson = mapper.readTree(mapper.writeValueAsString(identityObject));
			ProcessingReport report = jsonSchema.validate(identityJson, true);
			log.debug("schema validation report generated : " + report);

			List<ServiceError> errorList = getErrorList(report, identityJson, requiredFields);

			if (!errorList.isEmpty()) {
				log.error("IdObject Validation Failed with errors : " + errorList);
				throw new IdObjectValidationFailedException(ID_OBJECT_VALIDATION_FAILED, errorList);
			}
			return true;

		} catch (IOException e) {
			ExceptionUtils.logRootCause(e);
			throw new IdObjectIOException(SCHEMA_IO_EXCEPTION, e);
		} catch (ProcessingException e) {
			ExceptionUtils.logRootCause(e);
			throw new IdObjectIOException(ID_OBJECT_VALIDATION_FAILED, e);
		}
	}

	/**
	 * Gets the json schema.
	 *
	 * @param schemaJson the schema json
	 * @return the json schema
	 * @throws InvalidIdSchemaException the invalid id schema exception
	 */
	private JsonSchema getJsonSchema(String schemaJson) throws InvalidIdSchemaException {
		try {
			if (schemaJson == null) {
				throw new InvalidIdSchemaException(IdObjectValidatorErrorConstant.INVALID_ID_SCHEMA.getErrorCode(),
						IdObjectValidatorErrorConstant.INVALID_ID_SCHEMA.getMessage());
			}
			JSONObject schema = new JSONObject(schemaJson);
			JsonNode jsonIdSchemaNode = JsonLoader.fromString(schema.toString());

			if (jsonIdSchemaNode.size() <= 0
					|| !(jsonIdSchemaNode.hasNonNull("$schema") && jsonIdSchemaNode.hasNonNull(TYPE))) {
				throw new InvalidIdSchemaException(IdObjectValidatorErrorConstant.SCHEMA_IO_EXCEPTION.getErrorCode(),
						IdObjectValidatorErrorConstant.SCHEMA_IO_EXCEPTION.getMessage());
			}

			final JsonSchemaFactory factory = IdObjectValidatorHelper.getJSONSchemaFactory();
			return factory.getJsonSchema(jsonIdSchemaNode);
		} catch (IOException | ProcessingException | JSONException e) {
			throw new InvalidIdSchemaException(IdObjectValidatorErrorConstant.SCHEMA_IO_EXCEPTION.getErrorCode(),
					IdObjectValidatorErrorConstant.SCHEMA_IO_EXCEPTION.getMessage());
		}
	}

	/**
	 * Gets the error list.
	 *
	 * @param report         the report
	 * @param requiredFields the required fields
	 * @return the error list
	 */
	private List<ServiceError> getErrorList(ProcessingReport report, JsonNode identityJson,
			List<String> requiredFields) {
		List<ServiceError> errorList = new ArrayList<>();
		if (!report.isSuccess()) {
			report.forEach(processingMessage -> {
				if (processingMessage.getLogLevel().toString().equals(ERROR)) {
					JsonNode errorReport = processingMessage.asJson();
					if (errorReport.hasNonNull(INSTANCE) && errorReport.get(INSTANCE).hasNonNull(POINTER)) {
						if (errorReport.has(MISSING) && !errorReport.get(MISSING).isNull()) {
							if (errorReport.get("schema").get(POINTER).asText().contains("definitions")) {
								buildErrorMessages(identityJson, errorList, errorReport, MISSING_INPUT_PARAMETER,
										MISSING, null);
							} else {
								buildErrorMessages(identityJson, errorList, errorReport, MISSING_INPUT_PARAMETER,
										MISSING, requiredFields);
							}
						} else {
							buildErrorMessages(identityJson, errorList, errorReport, INVALID_INPUT_PARAMETER, UNWANTED,
									null);
						}
						if (errorReport.hasNonNull(KEYWORD)
								&& (errorReport.get(KEYWORD).asText().contentEquals(VALIDATORS)
										|| errorReport.get(KEYWORD).asText().contentEquals(TYPE))) {
							buildErrorMessages(identityJson, errorList, errorReport, INVALID_INPUT_PARAMETER, KEYWORD,
									null);
						}
					}
				}
			});
		}
		return errorList;
	}

	/**
	 * Builds the error message.
	 *
	 * @param errorList               the error list
	 * @param processingMessageAsJson the processing message as json
	 * @param errorConstant           the error constant
	 * @param field                   the field
	 * @param requiredFields          the required fields
	 * @return the string
	 */
	private void buildErrorMessages(JsonNode identityJson, List<ServiceError> errorList,
			JsonNode processingMessageAsJson, IdObjectValidatorErrorConstant errorConstant, String field,
			List<String> requiredFields) {
		log.error("ID SCHEMA ERROR : " + processingMessageAsJson.toString());
		if (Objects.nonNull(field) && processingMessageAsJson.hasNonNull(field)) {
			if (field.contentEquals(KEYWORD)) {
				errorList.add(new ServiceError(errorConstant.getErrorCode(), String.format(errorConstant.getMessage(),
						StringUtils.strip(processingMessageAsJson.get(INSTANCE).get(POINTER).asText(), "/"))));
			} else {
				JsonNode elements = Objects.nonNull(processingMessageAsJson.get(UNWANTED))
						? processingMessageAsJson.get(UNWANTED)
						: processingMessageAsJson.get(MISSING);
				StreamSupport
						.stream(((ArrayNode) (Objects.nonNull(elements) ? elements : mapper.createArrayNode()))
								.spliterator(), false)
						.filter(element -> Objects.isNull(requiredFields) || requiredFields.isEmpty() ? true
								: requiredFields.parallelStream().map(reqField -> Arrays.asList(reqField.split("\\|")))
										.anyMatch(fields -> fields.contains(element.asText())))
						.forEach(element -> buildErrorMessage(identityJson, errorList, processingMessageAsJson,
								errorConstant, element.asText()));
			}
		}
	}

	private void buildErrorMessage(JsonNode identityJson, List<ServiceError> errorList,
			JsonNode processingMessageAsJson, IdObjectValidatorErrorConstant errorConstant, String element) {
		errorList
				.add(new ServiceError(errorConstant.getErrorCode(),
						String.format(errorConstant.getMessage(), StringUtils.strip(
								processingMessageAsJson.get(INSTANCE).get(POINTER).asText() + PATH_SEPERATOR + element,
								"/"))));
	}
}
