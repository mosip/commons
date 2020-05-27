package io.mosip.kernel.masterdata.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.SchemaErrorCode;
import io.mosip.kernel.masterdata.dto.IdSchemaPublishDto;
import io.mosip.kernel.masterdata.dto.IdentitySchemaDto;
import io.mosip.kernel.masterdata.dto.SchemaDto;
import io.mosip.kernel.masterdata.dto.getresponse.IdSchemaResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.entity.IdentitySchema;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.IdentitySchemaRepository;
import io.mosip.kernel.masterdata.service.IdentitySchemaService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;


@Service
public class IdentitySchemaServiceImpl implements IdentitySchemaService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IdentitySchemaServiceImpl.class);
	
	private static List<String> PRIMTIVE_TYPES;
	private static List<String> PRIMTIVE_NUMBER_TYPES;
	
	private static final String SCHEMA_TITLE = "MOSIP ID schema";
	private static final String SCHEMA_DESCRIPTION = "MOSIP ID schema";
	private static final String SCHEMA_VERSION = "http://json-schema.org/draft-07/schema#";
	
	private static final String KEY_SCHEMA= "$schema";
	private static final String KEY_TITLE = "title";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_PROPS = "properties";
	private static final String KEY_TYPE = "type";
	private static final String KEY_ADD_PROPS = "additionalProperties";
	private static final String KEY_IDENTITY = "identity";	
	private static final String KEY_TYPE_OBJECT = "object";	
	private static final String KEY_DEFINITIONS = "definitions";
	private static final String KEY_REQUIRED = "required";
	
	private static final String ATTR_REFTYPE = "$ref";
	private static final String ATTR_FORMAT = "format";
	private static final String ATTR_MIN = "minimum";
	private static final String ATTR_MAX = "maximum";
	private static final String ATTR_FIELDTYPE = "fieldType";
	private static final String ATTR_FIELD_CATEGORY = "fieldCategory";
	private static final String ATTR_VALIDATORS = "validators";
	private static final String ATTR_BIOS = "bioAttributes";
	
	private static final String REF_DATATYPE = "#/definitions/%s";	
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	private IdentitySchemaRepository identitySchemaRepository;
	
	@Autowired
	private SchemaDefinitionServiceImpl schemaDefinitionServiceImpl;
	
	static {
		PRIMTIVE_TYPES = new ArrayList<String>();
		PRIMTIVE_TYPES.add("string");
		PRIMTIVE_TYPES.add("number");
		PRIMTIVE_TYPES.add("integer");
		
		PRIMTIVE_NUMBER_TYPES = new ArrayList<String>();
		PRIMTIVE_NUMBER_TYPES.add("number");
		PRIMTIVE_NUMBER_TYPES.add("integer");
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.IdentitySchemaService#getAllSchema()
	 */
	@Override
	public PageDto<IdSchemaResponseDto> getAllSchema(int pageNumber, int pageSize, String sortBy, String orderBy) {
		List<IdSchemaResponseDto> list = new ArrayList<>();		
		PageDto<IdSchemaResponseDto> pagedSchema = new PageDto<>(pageNumber, 0, 0, list);		
		Page<IdentitySchema> pagedResult = null;
		
		try {
			pagedResult = identitySchemaRepository.findAllIdentitySchema(true, PageRequest.of(pageNumber, pageSize, 
					Sort.by(Direction.fromString(orderBy), sortBy)));
			
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_FETCH_EXCEPTION.getErrorCode(),
					SchemaErrorCode.SCHEMA_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		
		if(pagedResult != null && pagedResult.getContent() != null) {
			pagedResult.getContent().forEach(entity -> {
				list.add(getIdentitySchemaDto(entity));
			});
			
			pagedSchema.setPageNo(pagedResult.getNumber());
			pagedSchema.setTotalPages(pagedResult.getTotalPages());
			pagedSchema.setTotalItems(pagedResult.getTotalElements());
		}		
		return pagedSchema;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.IdentitySchemaService#getLatestSchema()
	 */
	@Override
	public IdSchemaResponseDto getLatestSchema() {
		IdentitySchema identitySchema = null;
		try {
			identitySchema = identitySchemaRepository.findLatestPublishedIdentitySchema();
		} catch (DataAccessException | DataAccessLayerException e) {
			LOGGER.error("Error while fetching latest schema", e);
			throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_FETCH_EXCEPTION.getErrorCode(),
					SchemaErrorCode.SCHEMA_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		
		if(identitySchema == null)
			throw new DataNotFoundException(SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorCode(),
					SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorMessage());
		
		return getIdentitySchemaDto(identitySchema);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.IdentitySchemaService#getIdentitySchema()
	 */
	@Override
	public IdSchemaResponseDto getIdentitySchema(double version) {
		IdentitySchema identitySchema = null;
		try {
			identitySchema = identitySchemaRepository.findPublishedIdentitySchema(version);
		} catch (DataAccessException | DataAccessLayerException e) {
			LOGGER.error("Error while fetching schema ver: " + version, e);
			throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_FETCH_EXCEPTION.getErrorCode(),
					SchemaErrorCode.SCHEMA_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		
		if(identitySchema == null)
			throw new DataNotFoundException(SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorCode(),
					SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorMessage());
		
		return getIdentitySchemaDto(identitySchema);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.IdentitySchemaService#createSchema()
	 */
	@Override
	@Transactional
	public IdSchemaResponseDto createSchema(IdentitySchemaDto dto) {
		
		validateDuplicateFields(dto.getSchema());
		validateDocumentFields(dto.getSchema());
		validateBiometricFields(dto.getSchema());
		
		IdentitySchema entity = MetaDataUtils.setCreateMetaData(dto, IdentitySchema.class);
		
		entity.setIsActive(true);
		entity.setStatus(STATUS_DRAFT);
		entity.setIdVersion(0);
		entity.setIdAttributeJson(getIdAttributeJsonString(dto));
		entity.setSchemaJson("{}");
		entity.setId(UUID.randomUUID().toString());
		entity.setLangCode("eng");
	
		try {
			entity = identitySchemaRepository.create(entity);			
				
		} catch (DataAccessLayerException | DataAccessException e) {
			LOGGER.error("Error while creating identity schema", e);
			throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_INSERT_EXCEPTION.getErrorCode(),
					ExceptionUtils.parseException(e));
		}
		return getIdentitySchemaDto(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.IdentitySchemaService#publishSchema()
	 */
	@Override
	@Transactional
	public String publishSchema(IdSchemaPublishDto dto) {
		
		if(dto.getEffectiveFrom().isBefore(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()))))
			throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_EFFECTIVE_FROM_IS_OLDER.getErrorCode(),
					SchemaErrorCode.SCHEMA_EFFECTIVE_FROM_IS_OLDER.getErrorMessage());
		
		try {
			IdentitySchema identitySchema = identitySchemaRepository.findIdentitySchemaById(dto.getId());
			if(identitySchema == null)
				throw new RequestException(SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorCode(),
						SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorMessage());
			
			if(STATUS_PUBLISHED.equalsIgnoreCase(identitySchema.getStatus()))
				throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_ALREADY_PUBLISHED.getErrorCode(),
						SchemaErrorCode.SCHEMA_ALREADY_PUBLISHED.getErrorMessage());
			
			String schemaJson = getSchemaJsonString(identitySchema);
			
			IdentitySchema publishedSchema = identitySchemaRepository.findLatestPublishedIdentitySchema();
			double currentVersion = publishedSchema == null ? 0.1 : (publishedSchema.getIdVersion() + 0.1);
			
			LOGGER.info("Current published version >> " + currentVersion);
						
			int updatedRows = identitySchemaRepository.publishIdentitySchema(dto.getId(), schemaJson, dto.getEffectiveFrom(), 
					MetaDataUtils.getCurrentDateTime(), MetaDataUtils.getContextUser(), currentVersion);
			
			if (updatedRows < 1) {
				throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorCode(),
						SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
		} catch (DataAccessException | DataAccessLayerException e) {
			LOGGER.error("Error while publishing identity schema : " + dto.getId(), e);
			throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_UPDATE_EXCEPTION.getErrorCode(),
					SchemaErrorCode.SCHEMA_UPDATE_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		return dto.getId();
	}	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.IdentitySchemaService#updateSchema()
	 */
	@Override
	@Transactional
	public IdSchemaResponseDto updateSchema(String id, IdentitySchemaDto dto) {	
		
		validateDuplicateFields(dto.getSchema());
		validateDocumentFields(dto.getSchema());
		validateBiometricFields(dto.getSchema());
		
		IdentitySchema entity = null;
		String jsonString = getIdAttributeJsonString(dto);
		
		try {
			int updatedRows = identitySchemaRepository.updateIdentitySchema(id, jsonString, true, 
					MetaDataUtils.getCurrentDateTime(), MetaDataUtils.getContextUser());
			
			entity = identitySchemaRepository.findIdentitySchemaById(id);
			
			if (updatedRows < 1 || entity == null) {
				throw new RequestException(SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorCode(),
						SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorMessage());
			}		
						
		} catch (DataAccessException | DataAccessLayerException e) {
			LOGGER.error("Error while updating identity schema : " + id, e);
			throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_UPDATE_EXCEPTION.getErrorCode(),
					SchemaErrorCode.SCHEMA_UPDATE_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		
		return getIdentitySchemaDto(entity);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.IdentitySchemaService#deleteSchema()
	 */
	@Override
	@Transactional
	public String deleteSchema(String id) {
		try {
			int updatedRows = identitySchemaRepository.deleteIdentitySchema(id, MetaDataUtils.getCurrentDateTime(),
					MetaDataUtils.getContextUser());
			
			if (updatedRows < 1) {
				throw new RequestException(SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorCode(),
						SchemaErrorCode.SCHEMA_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
			
		} catch (DataAccessException | DataAccessLayerException e) {
			LOGGER.error("Error while deleting identity schema : " + id, e);
			throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_UPDATE_EXCEPTION.getErrorCode(),
					SchemaErrorCode.SCHEMA_UPDATE_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		return id;
	}
	
	private String getIdAttributeJsonString(IdentitySchemaDto dto) {
		List<SchemaDto> list = dto.getSchema() == null ? new ArrayList<SchemaDto>() : dto.getSchema();
		try {
			return objectMapper.writeValueAsString(list);
		} catch (IOException e) {
			throw new MasterDataServiceException(SchemaErrorCode.VALUE_PARSE_ERROR.getErrorCode(),
					ExceptionUtils.parseException(e));
		}
	}
	
	private String getSchemaJsonString(IdentitySchema entity) {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(KEY_SCHEMA, SCHEMA_VERSION);
			jsonObject.put(KEY_TITLE, entity.getTitle() != null ? entity.getTitle() : SCHEMA_TITLE);
			jsonObject.put(KEY_DESCRIPTION, entity.getDescription() != null ? entity.getDescription() : SCHEMA_DESCRIPTION);
			jsonObject.put(KEY_TYPE, KEY_TYPE_OBJECT);
			jsonObject.put(KEY_ADD_PROPS, entity.isAdditionalProperties());
			
			JSONArray requiredFields = new JSONArray();
			JSONObject identityProperties = new JSONObject();
			List<SchemaDto> list = convertJSONStringToSchemaDTO(entity.getIdAttributeJson());
			for(SchemaDto schemaDto : list) {		
				identityProperties.put(schemaDto.getId(), getSchemaAttributes(schemaDto));
				
				if(schemaDto.isRequired())
					requiredFields.put(schemaDto.getId());
			}
			JSONObject identityProps = new JSONObject();
			identityProps.put(KEY_TYPE, KEY_TYPE_OBJECT);			
			identityProps.put(KEY_ADD_PROPS, false);
			identityProps.put(KEY_REQUIRED, requiredFields);
			identityProps.put(KEY_PROPS, identityProperties);			
			
			JSONObject properties = new JSONObject();
			properties.put(KEY_IDENTITY, identityProps);			
			jsonObject.put(KEY_PROPS, properties);
			jsonObject.put(KEY_DEFINITIONS, schemaDefinitionServiceImpl.getAllSchemaDefinitions().get(KEY_DEFINITIONS));
			return jsonObject.toString();
			
		} catch(JSONException | IOException e) {
			LOGGER.error(SchemaErrorCode.SCHEMA_JSON_EXCEPTION.getErrorMessage(), e);
			throw new MasterDataServiceException(SchemaErrorCode.SCHEMA_JSON_EXCEPTION.getErrorCode(),
					ExceptionUtils.parseException(e));
		}		
	}
	
	private JSONObject getSchemaAttributes(SchemaDto schemaDto) throws JSONException, IOException {
		JSONObject schemaAttributes = new JSONObject();
		schemaAttributes.put(ATTR_FIELDTYPE, schemaDto.getFieldType());
		schemaAttributes.put(ATTR_FIELD_CATEGORY, schemaDto.getFieldCategory());
		JSONArray bioAttributes = new JSONArray();
		if(schemaDto.getBioAttributes() != null) {
			schemaDto.getBioAttributes().forEach(bioAttr -> { 
				bioAttributes.put(bioAttr);
			});
		}		
		schemaAttributes.put(ATTR_BIOS, bioAttributes);
		
		if(isPrimitive(schemaDto.getType()))
			schemaAttributes.put(KEY_TYPE, schemaDto.getType());
		else
			schemaAttributes.put(ATTR_REFTYPE, String.format(REF_DATATYPE, schemaDto.getType()));
		
		if(PRIMTIVE_NUMBER_TYPES.contains(schemaDto.getType()))
			schemaAttributes.put(ATTR_MIN, schemaDto.getMinimum());		
		
		if(schemaDto.getMaximum() > 0)
			schemaAttributes.put(ATTR_MAX, schemaDto.getMaximum());
		
		if(schemaDto.getFormat() != null)	
			schemaAttributes.put(ATTR_FORMAT, schemaDto.getFormat());		
		
		if(schemaDto.getValidators() != null && !schemaDto.getValidators().isEmpty())
			schemaAttributes.put(ATTR_VALIDATORS, new JSONArray(objectMapper.writeValueAsString(schemaDto.getValidators())));
		
		return schemaAttributes;
	}
	
	private boolean isPrimitive(String type) {
		return PRIMTIVE_TYPES.contains(type);
	}
	
	private List<SchemaDto> convertJSONStringToSchemaDTO(String schemaJson) {
		List<SchemaDto> list = new ArrayList<>();
		try {
			list = objectMapper.readValue(schemaJson == null ? "[]" : schemaJson, new TypeReference<List<SchemaDto>>() {});
		} catch (IOException e) {
			throw new MasterDataServiceException(SchemaErrorCode.VALUE_PARSE_ERROR.getErrorCode(),
					ExceptionUtils.parseException(e));
		}
		return list;
	}	
	
	
	private IdSchemaResponseDto getIdentitySchemaDto(IdentitySchema entity) {
		IdSchemaResponseDto dto = new IdSchemaResponseDto();
		dto.setId(entity.getId());
		dto.setIdVersion(entity.getIdVersion());
		dto.setTitle(entity.getTitle());
		dto.setDescription(entity.getDescription());
		dto.setSchema(convertJSONStringToSchemaDTO(entity.getIdAttributeJson()));
		dto.setSchemaJson(entity.getSchemaJson());		
		dto.setStatus(entity.getStatus());
		dto.setCreatedBy(entity.getCreatedBy());
		dto.setCreatedOn(entity.getCreatedDateTime());		
		dto.setUpdatedBy(entity.getUpdatedBy());
		dto.setUpdatedOn(entity.getUpdatedDateTime());
		dto.setEffectiveFrom(entity.getEffectiveFrom());
		return dto;
	}
	
	private void validateDuplicateFields(List<SchemaDto> list) {
		List<String> duplicates = list.stream()
			.collect(Collectors.groupingBy(SchemaDto::caseIgnoredId))
			.entrySet()
			.stream()
			.filter(e -> e.getValue().size() > 1)
			.map(Map.Entry::getKey).collect(Collectors.toList());
		
		if(duplicates != null && duplicates.size() > 0)
			throw new MasterDataServiceException(SchemaErrorCode.DUPLICATE_FIELD_EXCEPTION.getErrorCode(),
					String.format(SchemaErrorCode.DUPLICATE_FIELD_EXCEPTION.getErrorMessage(), duplicates));
	}
	
	private void validateDocumentFields(List<SchemaDto> dtoList) {
		validateSubType(dtoList, "documentType");
	}
	
	private void validateSubType(List<SchemaDto> dtoList, String fieldType) {
		List<SchemaDto> fields = dtoList.stream()
				.filter(obj -> fieldType.equalsIgnoreCase(obj.getType()))
				.collect(Collectors.toList());
		
		for(SchemaDto dto : fields) {
			if("none".equalsIgnoreCase(dto.getSubType()))
				throw new MasterDataServiceException(SchemaErrorCode.SUB_TYPE_REQUIRED_EXCEPTION.getErrorCode(),
						String.format(SchemaErrorCode.SUB_TYPE_REQUIRED_EXCEPTION.getErrorMessage(), dto.getId()));
		}
	}
	
	private void validateBiometricFields(List<SchemaDto> dtoList) {
		validateSubType(dtoList, "biometricsType");
		
		List<SchemaDto> fields = dtoList.stream()
				.filter(obj -> "biometricsType".equalsIgnoreCase(obj.getType()))
				.collect(Collectors.toList());
			
		if(fields != null) {
			Map<String, List<SchemaDto>> fieldsGroupedBySubType  = fields.stream()
			.collect(Collectors.groupingBy(SchemaDto::getSubType))
			.entrySet()
			.stream()
			.filter(e -> e.getValue().size() > 1)
			.collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
			
			if(fieldsGroupedBySubType != null) {
				for(Entry<String, List<SchemaDto>> entry : fieldsGroupedBySubType.entrySet()) {
					List<SchemaDto> list = entry.getValue();
					
					if(entry.getKey() == null || "none".equalsIgnoreCase(entry.getKey()))
						throw new MasterDataServiceException(SchemaErrorCode.SUB_TYPE_REQUIRED_EXCEPTION.getErrorCode(),
								String.format(SchemaErrorCode.SUB_TYPE_REQUIRED_EXCEPTION.getErrorMessage(), list.get(0).getId()));
					
					
					List<String> temp = new ArrayList<String>();
					for(SchemaDto dto : list) {
						if(dto.getBioAttributes() == null)
							throw new MasterDataServiceException(SchemaErrorCode.BIO_ATTRIBUTES_REQUIRED_EXCEPTION.getErrorCode(),
									String.format(SchemaErrorCode.BIO_ATTRIBUTES_REQUIRED_EXCEPTION.getErrorMessage(), dto.getId()));
						
						temp.addAll(dto.getBioAttributes());
					}
					
					List<String> distinctValues = temp.stream().distinct().collect(Collectors.toList());
					
					if(temp.size() > distinctValues.size())
						throw new MasterDataServiceException(SchemaErrorCode.BIO_ATTRIBUTES_DUPLICATED_EXCEPTION.getErrorCode(),
								String.format(SchemaErrorCode.BIO_ATTRIBUTES_DUPLICATED_EXCEPTION.getErrorMessage(), list.get(0).getId()));
				}
			}
		}
	}
}
