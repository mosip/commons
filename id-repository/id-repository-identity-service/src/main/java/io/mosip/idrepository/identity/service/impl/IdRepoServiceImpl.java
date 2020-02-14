package io.mosip.idrepository.identity.service.impl;

import static io.mosip.idrepository.core.constant.IdRepoConstants.ACTIVE_STATUS;
import static io.mosip.idrepository.core.constant.IdRepoConstants.CBEFF_FORMAT;
import static io.mosip.idrepository.core.constant.IdRepoConstants.DATETIME_PATTERN;
import static io.mosip.idrepository.core.constant.IdRepoConstants.FILE_FORMAT_ATTRIBUTE;
import static io.mosip.idrepository.core.constant.IdRepoConstants.FILE_NAME_ATTRIBUTE;
import static io.mosip.idrepository.core.constant.IdRepoConstants.FMR_ENABLED;
import static io.mosip.idrepository.core.constant.IdRepoConstants.MODULO_VALUE;
import static io.mosip.idrepository.core.constant.IdRepoConstants.MOSIP_PRIMARY_LANGUAGE;
import static io.mosip.idrepository.core.constant.IdRepoConstants.SPLITTER;
import static io.mosip.idrepository.core.constant.IdRepoErrorConstants.FILE_NOT_FOUND;
import static io.mosip.idrepository.core.constant.IdRepoErrorConstants.FILE_STORAGE_ACCESS_ERROR;
import static io.mosip.idrepository.core.constant.IdRepoErrorConstants.ID_OBJECT_PROCESSING_FAILED;
import static io.mosip.idrepository.core.constant.IdRepoErrorConstants.INVALID_INPUT_PARAMETER;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.FieldComparisonFailure;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import io.mosip.idrepository.core.dto.DocumentsDTO;
import io.mosip.idrepository.core.dto.IdRequestDTO;
import io.mosip.idrepository.core.dto.RequestDTO;
import io.mosip.idrepository.core.exception.IdRepoAppException;
import io.mosip.idrepository.core.exception.IdRepoAppUncheckedException;
import io.mosip.idrepository.core.logger.IdRepoLogger;
import io.mosip.idrepository.core.security.IdRepoSecurityManager;
import io.mosip.idrepository.core.spi.IdRepoService;
import io.mosip.idrepository.core.spi.MosipFingerprintProvider;
import io.mosip.idrepository.identity.entity.Uin;
import io.mosip.idrepository.identity.entity.UinBiometric;
import io.mosip.idrepository.identity.entity.UinBiometricHistory;
import io.mosip.idrepository.identity.entity.UinDocument;
import io.mosip.idrepository.identity.entity.UinDocumentHistory;
import io.mosip.idrepository.identity.entity.UinHistory;
import io.mosip.idrepository.identity.repository.UinBiometricHistoryRepo;
import io.mosip.idrepository.identity.repository.UinDocumentHistoryRepo;
import io.mosip.idrepository.identity.repository.UinEncryptSaltRepo;
import io.mosip.idrepository.identity.repository.UinHashSaltRepo;
import io.mosip.idrepository.identity.repository.UinHistoryRepo;
import io.mosip.idrepository.identity.repository.UinRepo;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.fsadapter.exception.FSAdapterException;
import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.UUIDUtils;
import io.mosip.kernel.fsadapter.hdfs.constant.HDFSAdapterErrorCode;

/**
 * The Class IdRepoServiceImpl - Service implementation for Identity service.
 */
@Component
@Transactional(rollbackFor = { IdRepoAppException.class, IdRepoAppUncheckedException.class })
public class IdRepoServiceImpl implements IdRepoService<IdRequestDTO, Uin> {

	/** The Constant GET_FILES. */
	private static final String GET_FILES = "getFiles";

	/** The Constant UPDATE_IDENTITY. */
	private static final String UPDATE_IDENTITY = "updateIdentity";

	/** The Constant ROOT. */
	private static final String ROOT = "$";

	/** The Constant OPEN_SQUARE_BRACE. */
	private static final String OPEN_SQUARE_BRACE = "[";

	/** The Constant LANGUAGE. */
	private static final String LANGUAGE = "language";

	/** The Constant ADD_IDENTITY. */
	private static final String ADD_IDENTITY = "addIdentity";

	/** The mosip logger. */
	Logger mosipLogger = IdRepoLogger.getLogger(IdRepoProxyServiceImpl.class);

	/** The Constant DOCUMENTS. */
	private static final String DOCUMENTS = "documents";

	/** The Constant TYPE. */
	private static final String TYPE = "type";

	/** The Constant DOT. */
	private static final String DOT = ".";

	/** The Constant SLASH. */
	private static final String SLASH = "/";

	/** The Constant BIOMETRICS. */
	private static final String BIOMETRICS = "Biometrics";

	/** The Constant ID_REPO_SERVICE_IMPL. */
	private static final String ID_REPO_SERVICE_IMPL = "IdRepoServiceImpl";

	/** The Constant DEMOGRAPHICS. */
	private static final String DEMOGRAPHICS = "Demographics";

	/** The env. */
	@Autowired
	private Environment env;

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	/** The uin repo. */
	@Autowired
	private UinRepo uinRepo;

	/** The uin detail repo. */
	@Autowired
	private UinDocumentHistoryRepo uinDocHRepo;

	/** The uin bio H repo. */
	@Autowired
	private UinBiometricHistoryRepo uinBioHRepo;

	/** The uin history repo. */
	@Autowired
	private UinHistoryRepo uinHistoryRepo;

	/** The fp provider. */
	@Autowired
	private MosipFingerprintProvider<BIR, BIR> fpProvider;

	/** The cbeff util. */
	@Autowired
	private CbeffUtil cbeffUtil;

	/** The dfs provider. */
	@Autowired
	private FileSystemAdapter fsAdapter;

	/** The security manager. */
	@Autowired
	private IdRepoSecurityManager securityManager;

	/** The bio attributes. */
	@Resource
	private List<String> bioAttributes;

	/** The uin hash salt repo. */
	@Autowired
	private UinHashSaltRepo uinHashSaltRepo;

	/** The uin encrypt salt repo. */
	@Autowired
	private UinEncryptSaltRepo uinEncryptSaltRepo;

	/**
	 * Adds the identity to DB.
	 *
	 * @param request the request
	 * @param uin the uin
	 * @return the uin
	 * @throws IdRepoAppException the id repo app exception
	 */
	@Override
	public Uin addIdentity(IdRequestDTO request, String uin) throws IdRepoAppException {
		String uinRefId = UUIDUtils.getUUID(UUIDUtils.NAMESPACE_OID, uin + SPLITTER + DateUtils.getUTCCurrentDateTime())
				.toString();
		byte[] identityInfo = convertToBytes(request.getRequest().getIdentity());
		Integer moduloValue = env.getProperty(MODULO_VALUE, Integer.class);
		int modResult = (int) (Long.parseLong(uin) % moduloValue);
		String hashSalt = uinHashSaltRepo.retrieveSaltById(modResult);
		String uinHash = modResult + SPLITTER + securityManager.hashwithSalt(uin.getBytes(), hashSalt.getBytes());
		String encryptSalt = uinEncryptSaltRepo.retrieveSaltById(modResult);
		String uinToEncrypt = modResult + SPLITTER + uin + SPLITTER + encryptSalt;
        
		List<UinDocument> docList = new ArrayList<>();
		List<UinBiometric> bioList = new ArrayList<>();
		Uin uinEntity;
		if (Objects.nonNull(request.getRequest().getDocuments())
				&& !request.getRequest().getDocuments().isEmpty()) {
			addDocuments(uinHash, identityInfo, request.getRequest().getDocuments(), uinRefId, docList, bioList);
			uinEntity = new Uin(uinRefId, uinToEncrypt, uinHash, identityInfo, securityManager.hash(identityInfo),
					request.getRequest().getRegistrationId(), request.getRequest().getBiometricReferenceId(),
					env.getProperty(ACTIVE_STATUS), env.getProperty(MOSIP_PRIMARY_LANGUAGE),
					IdRepoSecurityManager.getUser(), now(), null, null, false, null, bioList, docList);
			uinEntity = uinRepo.save(uinEntity);
			mosipLogger.debug(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, ADD_IDENTITY,
					"Record successfully saved in db with documents");
		} else {
			uinEntity = new Uin(uinRefId, uinToEncrypt, uinHash, identityInfo, securityManager.hash(identityInfo),
					request.getRequest().getRegistrationId(), request.getRequest().getBiometricReferenceId(),
					env.getProperty(ACTIVE_STATUS), env.getProperty(MOSIP_PRIMARY_LANGUAGE),
					IdRepoSecurityManager.getUser(), now(), null, null, false, null, null, null);
			uinEntity = uinRepo.save(uinEntity);
			mosipLogger.debug(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, ADD_IDENTITY,
					"Record successfully saved in db without documents");
		}

		uinHistoryRepo.save(new UinHistory(uinRefId, now(), uinEntity.getUin(), uinEntity.getUinHash(),
				uinEntity.getUinData(), uinEntity.getUinDataHash(), uinEntity.getRegId(),
				request.getRequest().getBiometricReferenceId(), env.getProperty(ACTIVE_STATUS),
				env.getProperty(MOSIP_PRIMARY_LANGUAGE), IdRepoSecurityManager.getUser(), now(), null, null, false,
				null));
		return uinEntity;
	}

	/**
	 * Stores the documents to FileSystem.
	 *
	 * @param uinHash the uin hash
	 * @param identityInfo the identity info
	 * @param documents the documents
	 * @param uinRefId the uin ref id
	 * @param docList the doc list
	 * @param bioList the bio list
	 * @throws IdRepoAppException the id repo app exception
	 */
	private void addDocuments(String uinHash, byte[] identityInfo, List<DocumentsDTO> documents, String uinRefId,
			List<UinDocument> docList, List<UinBiometric> bioList) throws IdRepoAppException {
		ObjectNode identityObject = (ObjectNode) convertToObject(identityInfo, ObjectNode.class);
		documents.stream().filter(doc -> identityObject.has(doc.getCategory())).forEach(doc -> {
			JsonNode docType = identityObject.get(doc.getCategory());
			try {
				if (bioAttributes.contains(doc.getCategory())) {
					addBiometricDocuments(uinHash, uinRefId, bioList, doc, docType);
				} else {
					addDemographicDocuments(uinHash, uinRefId, docList, doc, docType);
				}
			} catch (IdRepoAppException e) {
				mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, ADD_IDENTITY, e.getMessage());
				throw new IdRepoAppUncheckedException(e.getErrorCode(), e.getErrorText(), e);
			} catch (FSAdapterException e) {
				mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, ADD_IDENTITY, e.getMessage());
				throw new IdRepoAppUncheckedException(FILE_STORAGE_ACCESS_ERROR, e);
			}
		});
	}

	/**
	 * Stores the biometric documents to FileSystem.
	 *
	 * @param uinHash the uin hash
	 * @param uinRefId the uin ref id
	 * @param bioList the bio list
	 * @param doc the doc
	 * @param docType the doc type
	 * @throws IdRepoAppException the id repo app exception
	 */
	private void addBiometricDocuments(String uinHash, String uinRefId, List<UinBiometric> bioList, DocumentsDTO doc,
			JsonNode docType) throws IdRepoAppException {
		byte[] data = null;
		String fileRefId = UUIDUtils
				.getUUID(UUIDUtils.NAMESPACE_OID,
						docType.get(FILE_NAME_ATTRIBUTE).asText() + SPLITTER + DateUtils.getUTCCurrentDateTime())
				.toString() + DOT + docType.get(FILE_FORMAT_ATTRIBUTE).asText();

		if (StringUtils.equalsIgnoreCase(docType.get(FILE_FORMAT_ATTRIBUTE).asText(), CBEFF_FORMAT)) {
			data = convertToFMR(doc.getCategory(), doc.getValue());
		} else {
			data = CryptoUtil.decodeBase64(doc.getValue());
		}

		LocalDateTime startTime = DateUtils.getUTCCurrentDateTime();
		fsAdapter.storeFile(uinHash, BIOMETRICS + SLASH + fileRefId,
				new ByteArrayInputStream(securityManager.encrypt(data)));
		mosipLogger.debug(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, "storeFiles",
				"time taken to store file in millis: " + fileRefId + "  - "
						+ Duration.between(startTime, DateUtils.getUTCCurrentDateTime()).toMillis() + "  "
						+ "Start time : " + startTime + "  " + "end time : " + DateUtils.getUTCCurrentDateTime());

		bioList.add(new UinBiometric(uinRefId, fileRefId, doc.getCategory(), docType.get(FILE_NAME_ATTRIBUTE).asText(),
				securityManager.hash(data), env.getProperty(MOSIP_PRIMARY_LANGUAGE), IdRepoSecurityManager.getUser(),
				now(), null, null, false, null));

		uinBioHRepo.save(new UinBiometricHistory(uinRefId, now(), fileRefId, doc.getCategory(),
				docType.get(FILE_NAME_ATTRIBUTE).asText(), securityManager.hash(doc.getValue().getBytes()),
				env.getProperty(MOSIP_PRIMARY_LANGUAGE), IdRepoSecurityManager.getUser(), now(), null, null, false,
				null));
	}

	/**
	 * Stores the demographic documents to FileSystem.
	 *
	 * @param uinHash the uin hash
	 * @param uinRefId the uin ref id
	 * @param docList the doc list
	 * @param doc the doc
	 * @param docType the doc type
	 * @throws IdRepoAppException the id repo app exception
	 */
	private void addDemographicDocuments(String uinHash, String uinRefId, List<UinDocument> docList, DocumentsDTO doc,
			JsonNode docType) throws IdRepoAppException {
		String fileRefId = UUIDUtils
				.getUUID(UUIDUtils.NAMESPACE_OID,
						docType.get(FILE_NAME_ATTRIBUTE).asText() + SPLITTER + DateUtils.getUTCCurrentDateTime())
				.toString() + DOT + docType.get(FILE_FORMAT_ATTRIBUTE).asText();

		LocalDateTime startTime = DateUtils.getUTCCurrentDateTime();
		byte[] data = CryptoUtil.decodeBase64(doc.getValue());
		fsAdapter.storeFile(uinHash, DEMOGRAPHICS + SLASH + fileRefId,
				new ByteArrayInputStream(securityManager.encrypt(data)));
		mosipLogger.debug(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, "storeFiles",
				"time taken to store file in millis: " + fileRefId + "  - "
						+ Duration.between(startTime, DateUtils.getUTCCurrentDateTime()).toMillis() + "  "
						+ "Start time : " + startTime + "  " + "end time : " + DateUtils.getUTCCurrentDateTime());

		docList.add(new UinDocument(uinRefId, doc.getCategory(), docType.get(TYPE).asText(), fileRefId,
				docType.get(FILE_NAME_ATTRIBUTE).asText(), docType.get(FILE_FORMAT_ATTRIBUTE).asText(),
				securityManager.hash(data), env.getProperty(MOSIP_PRIMARY_LANGUAGE), IdRepoSecurityManager.getUser(),
				now(), null, null, false, null));

		uinDocHRepo.save(new UinDocumentHistory(uinRefId, now(), doc.getCategory(), docType.get(TYPE).asText(),
				fileRefId, docType.get(FILE_NAME_ATTRIBUTE).asText(), docType.get(FILE_FORMAT_ATTRIBUTE).asText(),
				securityManager.hash(data), env.getProperty(MOSIP_PRIMARY_LANGUAGE), IdRepoSecurityManager.getUser(),
				now(), null, null, false, null));
	}

	/**
	 * Convert to FMR.
	 *
	 * @param category the category
	 * @param encodedCbeffFile the encoded cbeff file
	 * @return the byte[]
	 * @throws IdRepoAppException the id repo app exception
	 */
	private byte[] convertToFMR(String category, String encodedCbeffFile) throws IdRepoAppException {
		try {
			byte[] cbeffFileData = CryptoUtil.decodeBase64(encodedCbeffFile);
			if (env.getProperty(FMR_ENABLED, Boolean.class, false)) {
				return cbeffUtil.updateXML(
						fpProvider.convertFIRtoFMR(
								cbeffUtil.convertBIRTypeToBIR(cbeffUtil.getBIRDataFromXML(cbeffFileData))),
						cbeffFileData);
			} else if (cbeffUtil.validateXML(cbeffFileData)) {
				return cbeffFileData;
			} else {
				mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, "convertToFMR",
						"\n" + "cbeff validation failed");
				throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
						String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), DOCUMENTS + " - " + category));
			}
		} catch (Exception e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, ADD_IDENTITY,
					"\n" + ExceptionUtils.getStackTrace(e));
			throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), DOCUMENTS + " - " + category));
		}
	}

	/**
	 * Retrieve identity by uin from DB.
	 *
	 * @param uinHash the uin hash
	 * @param type the type
	 * @return the uin
	 * @throws IdRepoAppException the id repo app exception
	 */
	@Override
	public Uin retrieveIdentityByUin(String uinHash, String type) throws IdRepoAppException {
		return uinRepo.findByUinHash(uinHash);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.idrepository.core.spi.IdRepoService#retrieveIdentityByRid(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public Uin retrieveIdentityByRid(String rid, String filter) throws IdRepoAppException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.idrepo.spi.IdRepoService#updateIdentity(java.lang.
	 * Object, java.lang.String)
	 */
	@Override
	public Uin updateIdentity(IdRequestDTO request, String uin) throws IdRepoAppException {
		Integer moduloValue = env.getProperty(MODULO_VALUE, Integer.class);
		int modResult = (int) (Long.parseLong(uin) % moduloValue);
		String hashSalt = uinHashSaltRepo.retrieveSaltById(modResult);
		String uinHash = modResult + SPLITTER + securityManager.hashwithSalt(uin.getBytes(), hashSalt.getBytes());

		try {
			Uin uinObject = retrieveIdentityByUin(uinHash, null);
			uinObject.setRegId(request.getRequest().getRegistrationId());
			if (Objects.nonNull(request.getRequest().getStatus())
					&& !StringUtils.equals(uinObject.getStatusCode(), request.getRequest().getStatus())) {
				uinObject.setStatusCode(request.getRequest().getStatus());
				uinObject.setUpdatedBy(IdRepoSecurityManager.getUser());
				uinObject.setUpdatedDateTime(now());
			}
			if (Objects.nonNull(request.getRequest()) && Objects.nonNull(request.getRequest().getIdentity())) {
				RequestDTO requestDTO = request.getRequest();
				Configuration configuration = Configuration.builder().jsonProvider(new JacksonJsonProvider())
						.mappingProvider(new JacksonMappingProvider()).build();
				DocumentContext inputData = JsonPath.using(configuration).parse(requestDTO.getIdentity());
				DocumentContext dbData = JsonPath.using(configuration).parse(new String(uinObject.getUinData()));
				JSONCompareResult comparisonResult = JSONCompare.compareJSON(inputData.jsonString(),
						dbData.jsonString(), JSONCompareMode.LENIENT);

				if (comparisonResult.failed()) {
					updateIdentityObject(inputData, dbData, comparisonResult);
					uinObject.setUinData(convertToBytes(convertToObject(dbData.jsonString().getBytes(), Map.class)));
					uinObject.setUinDataHash(securityManager.hash(uinObject.getUinData()));
					uinObject.setUpdatedBy(IdRepoSecurityManager.getUser());
					uinObject.setUpdatedDateTime(now());
				}

				if (Objects.nonNull(requestDTO.getDocuments()) && !requestDTO.getDocuments().isEmpty()) {
					updateDocuments(uinHash, uinObject, requestDTO);
					uinObject.setUpdatedBy(IdRepoSecurityManager.getUser());
					uinObject.setUpdatedDateTime(now());
				}
			}
			
			uinObject = uinRepo.save(uinObject);

			uinHistoryRepo.save(new UinHistory(uinObject.getUinRefId(), now(), uinObject.getUin(),
					uinObject.getUinHash(), uinObject.getUinData(), uinObject.getUinDataHash(), uinObject.getRegId(),
					request.getRequest().getBiometricReferenceId(), uinObject.getStatusCode(),
					env.getProperty(MOSIP_PRIMARY_LANGUAGE), IdRepoSecurityManager.getUser(), now(),
					IdRepoSecurityManager.getUser(), now(), false, null));

			return uinObject;
		} catch (JSONException | InvalidJsonException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, UPDATE_IDENTITY, e.getMessage());
			throw new IdRepoAppException(ID_OBJECT_PROCESSING_FAILED, e);
		} catch (IdRepoAppUncheckedException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, UPDATE_IDENTITY, "\n" + e.getErrorText());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		}
	}

	/**
	 * Update identity.
	 *
	 * @param inputData the input data
	 * @param dbData the db data
	 * @param comparisonResult the comparison result
	 * @throws JSONException the JSON exception
	 * @throws IdRepoAppException the id repo app exception
	 */
	private void updateIdentityObject(DocumentContext inputData, DocumentContext dbData,
			JSONCompareResult comparisonResult) throws JSONException, IdRepoAppException {
		if (comparisonResult.isMissingOnField()) {
			updateMissingFields(dbData, comparisonResult);
		}

		comparisonResult = JSONCompare.compareJSON(inputData.jsonString(), dbData.jsonString(),
				JSONCompareMode.LENIENT);
		if (comparisonResult.isFailureOnField()) {
			updateFailingFields(inputData, dbData, comparisonResult);
		}

		comparisonResult = JSONCompare.compareJSON(inputData.jsonString(), dbData.jsonString(),
				JSONCompareMode.LENIENT);
		if (!comparisonResult.getMessage().isEmpty()) {
			updateMissingValues(inputData, dbData, comparisonResult);
		}

		comparisonResult = JSONCompare.compareJSON(inputData.jsonString(), dbData.jsonString(),
				JSONCompareMode.LENIENT);
		if (comparisonResult.failed()) {
			updateIdentityObject(inputData, dbData, comparisonResult);
		}
	}

	/**
	 * Update missing fields.
	 *
	 * @param dbData the db data
	 * @param comparisonResult the comparison result
	 * @throws IdRepoAppException the id repo app exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateMissingFields(DocumentContext dbData, JSONCompareResult comparisonResult)
			throws IdRepoAppException {
		for (FieldComparisonFailure failure : comparisonResult.getFieldMissing()) {
			if (StringUtils.contains(failure.getField(), OPEN_SQUARE_BRACE)) {
				String path = StringUtils.substringBefore(failure.getField(), OPEN_SQUARE_BRACE);
				String key = StringUtils.substringAfterLast(path, DOT);
				path = StringUtils.substringBeforeLast(path, DOT);

				if (StringUtils.isEmpty(key)) {
					key = path;
					path = ROOT;
				}

				List value = dbData.read(path + DOT + key, List.class);
				value.addAll((Collection) Collections
						.singletonList(convertToObject(failure.getExpected().toString().getBytes(), Map.class)));

				dbData.put(path, key, value);
			} else {
				String path = StringUtils.substringBeforeLast(failure.getField(), DOT);
				if (StringUtils.isEmpty(path)) {
					path = ROOT;
				}
				String key = StringUtils.substringAfterLast(failure.getField(), DOT);
				dbData.put(path, (String) failure.getExpected(), key);
			}

		}
	}

	/**
	 * Update failing fields.
	 *
	 * @param inputData the input data
	 * @param dbData the db data
	 * @param comparisonResult the comparison result
	 * @throws IdRepoAppException the id repo app exception
	 */
	private void updateFailingFields(DocumentContext inputData, DocumentContext dbData,
			JSONCompareResult comparisonResult) throws IdRepoAppException {
		for (FieldComparisonFailure failure : comparisonResult.getFieldFailures()) {

			String path = StringUtils.substringBeforeLast(failure.getField(), DOT);
			if (StringUtils.contains(path, OPEN_SQUARE_BRACE)) {
				path = StringUtils.replaceAll(path, "\\[", "\\[\\?\\(\\@\\.");
				path = StringUtils.replaceAll(path, "=", "=='");
				path = StringUtils.replaceAll(path, "\\]", "'\\)\\]");
			}

			String key = StringUtils.substringAfterLast(failure.getField(), DOT);
			if (StringUtils.isEmpty(key)) {
				key = failure.getField();
				path = ROOT;
			}

			if (failure.getExpected() instanceof JSONArray) {
				dbData.put(path, key, convertToObject(failure.getExpected().toString().getBytes(), List.class));
				inputData.put(path, key, convertToObject(failure.getExpected().toString().getBytes(), List.class));
			} else if (failure.getExpected() instanceof JSONObject) {
				Object object = convertToObject(failure.getExpected().toString().getBytes(), ObjectNode.class);
				dbData.put(path, key, object);
				inputData.put(path, key, object);
			} else {
				dbData.put(path, key, failure.getExpected());
				inputData.put(path, key, failure.getExpected());
			}
		}
	}

	/**
	 * Update missing values.
	 *
	 * @param inputData the input data
	 * @param dbData the db data
	 * @param comparisonResult the comparison result
	 */
	@SuppressWarnings("unchecked")
	private void updateMissingValues(DocumentContext inputData, DocumentContext dbData,
			JSONCompareResult comparisonResult) {
		String path = StringUtils.substringBefore(comparisonResult.getMessage(), OPEN_SQUARE_BRACE);
		String key = StringUtils.substringAfterLast(path, DOT);
		path = StringUtils.substringBeforeLast(path, DOT);

		if (StringUtils.isEmpty(key)) {
			key = path;
			path = ROOT;
		}

		List<Map<String, String>> dbDataList = dbData.read(path + DOT + key, List.class);
		List<Map<String, String>> inputDataList = inputData.read(path + DOT + key, List.class);
		inputDataList.stream().filter(
				map -> map.containsKey(LANGUAGE) && dbDataList.stream().filter(dbMap -> dbMap.containsKey(LANGUAGE))
						.allMatch(dbMap -> !StringUtils.equalsIgnoreCase(dbMap.get(LANGUAGE), map.get(LANGUAGE))))
				.forEach(dbDataList::add);
		dbDataList
				.stream().filter(
						map -> map.containsKey(LANGUAGE)
								&& inputDataList.stream().filter(inputDataMap -> inputDataMap.containsKey(LANGUAGE))
										.allMatch(inputDataMap -> !StringUtils
												.equalsIgnoreCase(inputDataMap.get(LANGUAGE), map.get(LANGUAGE))))
				.forEach(inputDataList::add);
	}

	/**
	 * Update documents.
	 *
	 * @param uinHash the uin hash
	 * @param uinObject the uin object
	 * @param requestDTO the request DTO
	 * @throws IdRepoAppException the id repo app exception
	 */
	private void updateDocuments(String uinHash, Uin uinObject, RequestDTO requestDTO) throws IdRepoAppException {
		List<UinDocument> docList = new ArrayList<>();
		List<UinBiometric> bioList = new ArrayList<>();

		if (Objects.nonNull(uinObject.getBiometrics())) {
			updateCbeff(uinObject, requestDTO);
		}

		addDocuments(uinHash, convertToBytes(requestDTO.getIdentity()), requestDTO.getDocuments(),
				uinObject.getUinRefId(), docList, bioList);

		docList.stream().forEach(doc -> uinObject.getDocuments().stream()
				.filter(docObj -> StringUtils.equals(doc.getDoccatCode(), docObj.getDoccatCode())).forEach(docObj -> {
					docObj.setDocId(doc.getDocId());
					docObj.setDocName(doc.getDocName());
					docObj.setDocfmtCode(doc.getDocfmtCode());
					docObj.setDocHash(doc.getDocHash());
					docObj.setUpdatedBy(IdRepoSecurityManager.getUser());
					docObj.setUpdatedDateTime(doc.getUpdatedDateTime());
				}));
		docList.stream()
				.filter(doc -> uinObject.getDocuments().stream()
						.allMatch(docObj -> !StringUtils.equals(doc.getDoccatCode(), docObj.getDoccatCode())))
				.forEach(doc -> uinObject.getDocuments().add(doc));
		bioList.stream()
				.forEach(bio -> uinObject.getBiometrics().stream()
						.filter(bioObj -> StringUtils.equals(bio.getBiometricFileType(), bioObj.getBiometricFileType()))
						.forEach(bioObj -> {
							bioObj.setBioFileId(bio.getBioFileId());
							bioObj.setBiometricFileName(bio.getBiometricFileName());
							bioObj.setBiometricFileHash(bio.getBiometricFileHash());
							bioObj.setUpdatedBy(IdRepoSecurityManager.getUser());
							bioObj.setUpdatedDateTime(bio.getUpdatedDateTime());
						}));
		bioList.stream()
				.filter(bio -> uinObject.getBiometrics().stream()
						.allMatch(bioObj -> !StringUtils.equals(bio.getBioFileId(), bioObj.getBioFileId())))
				.forEach(bio -> uinObject.getBiometrics().add(bio));
	}

	/**
	 * Update cbeff.
	 *
	 * @param uinObject the uin object
	 * @param requestDTO the request DTO
	 * @throws IdRepoAppException the id repo app exception
	 */
	private void updateCbeff(Uin uinObject, RequestDTO requestDTO) throws IdRepoAppException {
		ObjectNode identityMap = (ObjectNode) convertToObject(uinObject.getUinData(), ObjectNode.class);

		uinObject.getBiometrics().stream().forEach(bio -> requestDTO.getDocuments().stream()
				.filter(doc -> StringUtils.equals(bio.getBiometricFileType(), doc.getCategory())).forEach(doc -> {
					try {
						String fileName = BIOMETRICS + SLASH + bio.getBioFileId();
						byte[] data = securityManager
								.decrypt(IOUtils.toByteArray(fsAdapter.getFile(uinObject.getUinHash(), fileName)));
						if (StringUtils.equalsIgnoreCase(
								identityMap.get(bio.getBiometricFileType()).get(FILE_FORMAT_ATTRIBUTE).asText(),
								CBEFF_FORMAT) && fileName.endsWith(CBEFF_FORMAT)) {
							doc.setValue(CryptoUtil.encodeBase64(cbeffUtil.updateXML(
									cbeffUtil.convertBIRTypeToBIR(
											cbeffUtil.getBIRDataFromXML(CryptoUtil.decodeBase64(doc.getValue()))),
									data)));
						}
					} catch (FSAdapterException e) {
						mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, GET_FILES, e.getMessage());
						throw new IdRepoAppUncheckedException(
								e.getErrorCode().equals(HDFSAdapterErrorCode.FILE_NOT_FOUND_EXCEPTION.getErrorCode())
										? FILE_NOT_FOUND
										: FILE_STORAGE_ACCESS_ERROR,
								e);
					} catch (Exception e) {
						mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, ADD_IDENTITY,
								"\n" + ExceptionUtils.getStackTrace(e));
						throw new IdRepoAppUncheckedException(INVALID_INPUT_PARAMETER.getErrorCode(), String.format(
								INVALID_INPUT_PARAMETER.getErrorMessage(), DOCUMENTS + " - " + doc.getCategory()));
					}
				}));
	}

	/**
	 * Get the current time.
	 *
	 * @return the local date time
	 * @throws IdRepoAppException the id repo app exception
	 */
	private LocalDateTime now() throws IdRepoAppException {
		try {
			return DateUtils.parseUTCToLocalDateTime(
					DateUtils.formatDate(new Date(), env.getProperty(DATETIME_PATTERN)),
					env.getProperty(DATETIME_PATTERN));
		} catch (ParseException | io.mosip.kernel.core.exception.IllegalArgumentException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, "now()", e.getMessage());
			throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorCode(), "DATETIME_PATTERN"), e);
		}
	}

	/**
	 * Convert to object.
	 *
	 * @param identity the identity
	 * @param clazz the clazz
	 * @return the object
	 * @throws IdRepoAppException the id repo app exception
	 */
	private Object convertToObject(byte[] identity, Class<?> clazz) throws IdRepoAppException {
		try {
			return mapper.readValue(identity, clazz);
		} catch (IOException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, "convertToObject", e.getMessage());
			throw new IdRepoAppException(ID_OBJECT_PROCESSING_FAILED, e);
		}
	}

	/**
	 * Convert to bytes.
	 *
	 * @param identity the identity
	 * @return the byte[]
	 * @throws IdRepoAppException the id repo app exception
	 */
	private byte[] convertToBytes(Object identity) throws IdRepoAppException {
		try {
			return mapper.writeValueAsBytes(identity);
		} catch (JsonProcessingException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), ID_REPO_SERVICE_IMPL, "convertToBytes", e.getMessage());
			throw new IdRepoAppException(ID_OBJECT_PROCESSING_FAILED, e);
		}
	}

}
