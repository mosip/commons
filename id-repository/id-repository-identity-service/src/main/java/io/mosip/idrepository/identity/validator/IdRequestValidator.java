package io.mosip.idrepository.identity.validator;

import static io.mosip.idrepository.core.constant.IdRepoConstants.MOSIP_KERNEL_IDREPO_JSON_PATH;
import static io.mosip.idrepository.core.constant.IdRepoConstants.ROOT_PATH;
import static io.mosip.idrepository.core.constant.IdRepoErrorConstants.ID_OBJECT_PROCESSING_FAILED;
import static io.mosip.idrepository.core.constant.IdRepoErrorConstants.INVALID_INPUT_PARAMETER;
import static io.mosip.idrepository.core.constant.IdRepoErrorConstants.MISSING_INPUT_PARAMETER;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.idrepository.core.builder.RestRequestBuilder;
import io.mosip.idrepository.core.constant.IdRepoErrorConstants;
import io.mosip.idrepository.core.constant.RestServicesConstants;
import io.mosip.idrepository.core.dto.IdRequestDTO;
import io.mosip.idrepository.core.dto.RestRequestDTO;
import io.mosip.idrepository.core.exception.IdRepoAppException;
import io.mosip.idrepository.core.exception.IdRepoAppUncheckedException;
import io.mosip.idrepository.core.exception.IdRepoDataValidationException;
import io.mosip.idrepository.core.exception.RestServiceException;
import io.mosip.idrepository.core.helper.RestHelper;
import io.mosip.idrepository.core.logger.IdRepoLogger;
import io.mosip.idrepository.core.security.IdRepoSecurityManager;
import io.mosip.idrepository.core.validator.BaseIdRepoValidator;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorErrorConstant;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.idobjectvalidator.constant.IdObjectValidatorConstant;

/**
 * The Class IdRequestValidator - Validator for {@code IdRequestDTO}.
 *
 * @author Manoj SP
 */
@Component
public class IdRequestValidator extends BaseIdRepoValidator implements Validator {

	/** The Constant UIN. */
	private static final String UIN = "uin";

	/** The Constant READ. */
	private static final String READ = "read";

	/** The Constant DOC_VALUE. */
	private static final String DOC_VALUE = "value";

	/** The Constant DOC_TYPE. */
	private static final String DOC_CAT = "category";

	/** The Constant DOCUMENTS. */
	private static final String DOCUMENTS = "documents";

	/** The Constant CREATE. */
	private static final String CREATE = "create";

	/** The Constant CREATE. */
	private static final String UPDATE = "update";

	/** The Constant VALIDATE_REQUEST. */
	private static final String VALIDATE_REQUEST = "validateRequest - \n";

	/** The Constant ID_REQUEST_VALIDATOR. */
	private static final String ID_REQUEST_VALIDATOR = "IdRequestValidator";

	/** The Constant ID_REPO. */
	private static final String ID_REPO = "IdRepo";

	/** The mosip logger. */
	Logger mosipLogger = IdRepoLogger.getLogger(IdRequestValidator.class);

	/** The Constant REQUEST. */
	private static final String REQUEST = "request";

	/** The Constant REGISTRATION_ID. */
	private static final String REGISTRATION_ID = "registrationId";

	/** The Constant STATUS_FIELD. */
	private static final String STATUS_FIELD = "status";
	
	@Value("${mosip.kernel.idobjectvalidator.mandatory-attributes.id-repository.new-registration}")
	private List<String> newRegistrationFields;
	
	@Value("${mosip.kernel.idobjectvalidator.mandatory-attributes.id-repository.update-uin}")
	private List<String> updateUinFields;

	/** The status. */
	@Resource
	private List<String> uinStatus;

	/** The rid validator impl. */
	@Autowired
	private RidValidator<String> ridValidator;

	/** The json validator. */
	@Autowired
	private IdObjectValidator idObjectValidator;

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	/** The allowed types. */
	@Resource
	private List<String> allowedTypes;

	/** The uin validator. */
	@Autowired
	private UinValidator<String> uinValidator;
	
	@Autowired
	private RestHelper restHelper;
	
	@Autowired
	private RestRequestBuilder restBuilder;
	
	private Map<String, String> schemaMap = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(IdRequestDTO.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 * org.springframework.validation.Errors)
	 */
	@Override
	public void validate(@Nonnull Object target, Errors errors) {
		IdRequestDTO request = (IdRequestDTO) target;

		validateReqTime(request.getRequesttime(), errors);

		if (!errors.hasErrors()) {
			validateVersion(request.getVersion(), errors);
		}

		if (!errors.hasErrors() && Objects.nonNull(request.getId())) {
			if (request.getId().equals(id.get(CREATE))) {
				validateStatus(request.getRequest().getStatus(), errors, CREATE);
				validateRequest(request.getRequest(), errors, CREATE);
			} else if (request.getId().equals(id.get(UPDATE))) {
				validateStatus(request.getRequest().getStatus(), errors, UPDATE);
				validateRequest(request.getRequest(), errors, UPDATE);
			}

			validateRegId(request.getRequest().getRegistrationId(), errors);
		}

	}

	/**
	 * Validate status.
	 *
	 * @param status the status
	 * @param errors the errors
	 * @param method the method
	 */
	private void validateStatus(String status, Errors errors, String method) {
		if (Objects.nonNull(status) && (method.equals(UPDATE) && !this.uinStatus.contains(status))) {
			errors.rejectValue(REQUEST, INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), STATUS_FIELD));
		}
	}

	/**
	 * Validate reg id.
	 *
	 * @param registrationId the registration id
	 * @param errors         the errors
	 */
	private void validateRegId(String registrationId, Errors errors) {
		if (Objects.isNull(registrationId)) {
			errors.rejectValue(REQUEST, MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(MISSING_INPUT_PARAMETER.getErrorMessage(), REGISTRATION_ID));
		} else {
			try {
				validateRid(registrationId);
			} catch (InvalidIDException e) {
				mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REQUEST_VALIDATOR, "validateRegId",
						"\n" + ExceptionUtils.getStackTrace(e));
				errors.rejectValue(REQUEST, INVALID_INPUT_PARAMETER.getErrorCode(),
						String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), REGISTRATION_ID));
			}
		}
	}

	/**
	 * Validate rid.
	 *
	 * @param registrationId the registration id
	 */
	public void validateRid(String registrationId) {
		ridValidator.validateId(registrationId);
	}

	/**
	 * Validate request.
	 *
	 * @param request the request
	 * @param errors  the errors
	 * @param method  the method
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validateRequest(Object request, Errors errors, String method) {
		try {
			if (Objects.nonNull(request)) {
				Map<String, Object> requestMap = convertToMap(request);
				if (!(requestMap.containsKey(ROOT_PATH) && Objects.nonNull(requestMap.get(ROOT_PATH)))) {
					if (method.equals(CREATE)) {
						errors.rejectValue(REQUEST, MISSING_INPUT_PARAMETER.getErrorCode(),
								String.format(MISSING_INPUT_PARAMETER.getErrorMessage(), ROOT_PATH));
					}
				} else if (((Map) requestMap.get(ROOT_PATH)).isEmpty()) {
					errors.rejectValue(REQUEST, INVALID_INPUT_PARAMETER.getErrorCode(),
							String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), ROOT_PATH));
				} else {
					validateDocuments(requestMap, errors);
					requestMap.keySet().parallelStream()
							.filter(key -> !key.contentEquals(ROOT_PATH))
							.forEach(requestMap::remove);
					if (!errors.hasErrors()) {
						String schemaVersion;
						if (requestMap.get(ROOT_PATH) != null) {
							schemaVersion = String
									.valueOf(((Map<String, Object>) requestMap.get(ROOT_PATH)).get("IDSchemaVersion"));
							if (method.equals(CREATE)) {
								idObjectValidator.validateIdObject(getSchema(schemaVersion), requestMap,
										newRegistrationFields);
							} else {
								idObjectValidator.validateIdObject(getSchema(schemaVersion), requestMap,
										updateUinFields);
							}
						}
					}
				}
			} else if (method.equals(CREATE)) {
				errors.rejectValue(REQUEST, MISSING_INPUT_PARAMETER.getErrorCode(),
						String.format(MISSING_INPUT_PARAMETER.getErrorMessage(), REQUEST));
			}
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO, ID_REQUEST_VALIDATOR,
					(VALIDATE_REQUEST + ExceptionUtils.getStackTrace(e)));
			errors.rejectValue(REQUEST, INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), ROOT_PATH));
		} catch (IdObjectValidationFailedException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO, ID_REQUEST_VALIDATOR,
					(VALIDATE_REQUEST + ExceptionUtils.getStackTrace(e)));
			IntStream.range(0, e.getErrorTexts().size()).boxed().forEach(index ->
				errors.rejectValue(REQUEST,
						e.getCodes().get(index)
								.equals(IdObjectValidatorErrorConstant.INVALID_INPUT_PARAMETER.getErrorCode())
										? INVALID_INPUT_PARAMETER.getErrorCode()
										: MISSING_INPUT_PARAMETER.getErrorCode(),
						String.format(
								e.getCodes().get(index)
										.equals(IdObjectValidatorErrorConstant.INVALID_INPUT_PARAMETER.getErrorCode())
												? INVALID_INPUT_PARAMETER.getErrorMessage()
												: MISSING_INPUT_PARAMETER.getErrorMessage(),
								Arrays.asList(e.getErrorTexts().get(index).split("-")[1].trim().split("\\|")).stream()
										.collect(Collectors.joining(" | "))))
			);
		} catch (InvalidIdSchemaException | IdObjectIOException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO, ID_REQUEST_VALIDATOR,
					VALIDATE_REQUEST + ExceptionUtils.getStackTrace(e));
			errors.rejectValue(REQUEST, ID_OBJECT_PROCESSING_FAILED.getErrorCode(),
					ID_OBJECT_PROCESSING_FAILED.getErrorMessage());
		}
	}

	/**
	 * Validate documents.
	 *
	 * @param requestMap the request map
	 * @param errors     the errors
	 */
	@SuppressWarnings("unchecked")
	private void validateDocuments(Map<String, Object> requestMap, Errors errors) {
		try {
			if (requestMap.containsKey(DOCUMENTS) && requestMap.containsKey(ROOT_PATH)
					&& Objects.nonNull(requestMap.get(ROOT_PATH))) {
				Map<String, Object> identityMap = convertToMap(requestMap.get(ROOT_PATH));
				if (Objects.nonNull(requestMap.get(DOCUMENTS)) && requestMap.get(DOCUMENTS) instanceof List
						&& !((List<Map<String, String>>) requestMap.get(DOCUMENTS)).isEmpty()) {
					if (!((List<Map<String, String>>) requestMap.get(DOCUMENTS)).parallelStream()
							.allMatch(doc -> doc.containsKey(DOC_CAT) && Objects.nonNull(doc.get(DOC_CAT))
									&& doc.containsKey(DOC_VALUE) && Objects.nonNull(doc.get(DOC_VALUE)))) {
						errors.rejectValue(REQUEST, INVALID_INPUT_PARAMETER.getErrorCode(),
								String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), DOCUMENTS));
					} else {
						checkForDuplicates(requestMap, errors);
					}
					((List<Map<String, String>>) requestMap.get(DOCUMENTS)).parallelStream().filter(
							doc -> !errors.hasErrors() && doc.containsKey(DOC_CAT) && Objects.nonNull(doc.get(DOC_CAT)))
							.forEach(doc -> {
								if (!identityMap.containsKey(doc.get(DOC_CAT))) {
									mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO, ID_REQUEST_VALIDATOR,
											(VALIDATE_REQUEST + "- validateDocuments failed for " + doc.get(DOC_CAT)));
									errors.rejectValue(REQUEST, INVALID_INPUT_PARAMETER.getErrorCode(),
											String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), doc.get(DOC_CAT)));
								}
								if (StringUtils.isEmpty(doc.get(DOC_VALUE))) {
									mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO, ID_REQUEST_VALIDATOR,
											(VALIDATE_REQUEST + "- empty doc value failed for " + doc.get(DOC_CAT)));
									errors.rejectValue(REQUEST, INVALID_INPUT_PARAMETER.getErrorCode(),
											String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), doc.get(DOC_CAT)));
								}
							});
				}
			}
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO, ID_REQUEST_VALIDATOR,
					(VALIDATE_REQUEST + ExceptionUtils.getStackTrace(e)));
			errors.rejectValue(REQUEST, INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), ROOT_PATH));
		}
	}

	/**
	 * Check for duplicates.
	 *
	 * @param requestMap the request map
	 * @param errors     the errors
	 */
	@SuppressWarnings("unchecked")
	private void checkForDuplicates(Map<String, Object> requestMap, Errors errors) {
		try {
			((List<Map<String, String>>) requestMap.get(DOCUMENTS)).parallelStream()
					.collect(Collectors.toMap(doc -> doc.get(DOC_CAT), doc -> doc.get(DOC_CAT)));
		} catch (IllegalStateException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO, ID_REQUEST_VALIDATOR,
					(VALIDATE_REQUEST + "  " + e.getMessage()));
			errors.rejectValue(REQUEST, INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), DOCUMENTS + " - "
							+ StringUtils.substringBefore(StringUtils.reverseDelimited(e.getMessage(), ' '), " ")));
		}
	}
	
	private String getSchema(String schemaVersion) {
		if (Objects.isNull(schemaVersion) || schemaVersion.contentEquals("null")) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REQUEST_VALIDATOR, "getSchema",
					"\n" + "schemaVersion is null");
			throw new IdRepoAppUncheckedException(MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(MISSING_INPUT_PARAMETER.getErrorMessage(),
							ROOT_PATH + IdObjectValidatorConstant.PATH_SEPERATOR + "IDSchemaVersion"));
		}
		try {
			if (schemaMap.containsKey(schemaVersion)) {
				return schemaMap.get(schemaVersion);
			} else {
				RestRequestDTO restRequest;
				restRequest = restBuilder.buildRequest(RestServicesConstants.SYNCDATA_SERVICE, null,
						ResponseWrapper.class);
				restRequest.setUri(restRequest.getUri().concat("?schemaVersion=" + schemaVersion));
				ResponseWrapper<Map<String, String>> response = restHelper.requestSync(restRequest);
				schemaMap.put(schemaVersion, response.getResponse().get("schemaJson"));
				return getSchema(schemaVersion);
			}
		} catch (IdRepoDataValidationException | RestServiceException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REQUEST_VALIDATOR, "getSchema",
					"\n" + ExceptionUtils.getStackTrace(e));
			throw new IdRepoAppUncheckedException(IdRepoErrorConstants.SCHEMA_RETRIEVE_ERROR);
		}
	}

	/**
	 * Convert to map.
	 *
	 * @param identity the identity
	 * @return the map
	 * @throws IdRepoAppException the id repo app exception
	 */
	private Map<String, Object> convertToMap(Object identity) throws IdRepoAppException {
		try {
			return mapper.readValue(mapper.writeValueAsBytes(identity), new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REQUEST_VALIDATOR, "convertToMap", "\n" + e.getMessage());
			throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), REQUEST), e);
		}
	}

	/**
	 * Validate uin.
	 *
	 * @param uin the uin
	 * @param idType the id type
	 * @throws IdRepoAppException the id repo app exception
	 */
	public void validateUin(String uin,String idType) throws IdRepoAppException {
		try {
			uinValidator.validateId(uin);
		} catch (InvalidIDException e) {
			if(!idType.equals(READ)) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REQUEST_VALIDATOR, "validateUin",
					"\n" + ExceptionUtils.getStackTrace(e));
				throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
						String.format(INVALID_INPUT_PARAMETER.getErrorMessage(),
								env.getProperty(MOSIP_KERNEL_IDREPO_JSON_PATH)).replace(".", "/"));
			}
			else {
				mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REQUEST_VALIDATOR, "validateUin",
						"\n" + ExceptionUtils.getStackTrace(e));
				throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
						String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), UIN), e);
			}
		}
	}

}
