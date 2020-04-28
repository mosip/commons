package io.mosip.kernel.masterdata.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.SchemaErrorCode;
import io.mosip.kernel.masterdata.dto.DynamicFieldDto;
import io.mosip.kernel.masterdata.dto.DynamicFieldValueDto;
import io.mosip.kernel.masterdata.dto.getresponse.DynamicFieldResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.entity.DynamicField;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.DynamicFieldRepository;
import io.mosip.kernel.masterdata.service.DynamicFieldService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

@Service
public class DynamicFieldServiceImpl implements DynamicFieldService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicFieldServiceImpl.class);
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	private DynamicFieldRepository dynamicFieldRepository;
	
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DynamicFieldService#getAllDynamicField()
	 */
	@Override
	public PageDto<DynamicFieldResponseDto> getAllDynamicField(int pageNumber, int pageSize, String sortBy, String orderBy, String langCode) {
		List<DynamicFieldResponseDto> list = new ArrayList<>();		
		PageDto<DynamicFieldResponseDto> pagedFields = new PageDto<>(pageNumber, 0, 0, list);
		Page<DynamicField> pagedResult = null;
		
		try {
			
			PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(Direction.fromString(orderBy), sortBy));			
			pagedResult = langCode == null ? dynamicFieldRepository.findAllDynamicFields(pageRequest) : 
				dynamicFieldRepository.findAllDynamicFieldsByLangCode(langCode, pageRequest);
			
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(SchemaErrorCode.DYNAMIC_FIELD_FETCH_EXCEPTION.getErrorCode(),
					SchemaErrorCode.DYNAMIC_FIELD_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		
		if(pagedResult != null && pagedResult.getContent() != null) {
			pagedResult.getContent().forEach(entity -> {
				list.add(getDynamicFieldDto(entity));
			});
			
			pagedFields.setPageNo(pagedResult.getNumber());
			pagedFields.setTotalPages(pagedResult.getTotalPages());
			pagedFields.setTotalItems(pagedResult.getTotalElements());
		}		
		return pagedFields;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DynamicFieldService#createDynamicField()
	 */
	@Override
	@Transactional
	public DynamicFieldResponseDto createDynamicField(DynamicFieldDto dto) {		
		checkIfDynamicFieldExists(dto.getName(), dto.getLangCode());		
		DynamicField entity = MetaDataUtils.setCreateMetaData(dto, DynamicField.class);
		entity.setIsActive(true);
		entity.setIsDeleted(false);
		entity.setId(UUID.randomUUID().toString());
		entity.setValueJson(getValueJson(dto.getFieldVal()));
		try {
			entity = dynamicFieldRepository.create(entity);
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(SchemaErrorCode.DYNAMIC_FIELD_INSERT_EXCEPTION.getErrorCode(),
					ExceptionUtils.parseException(e));
		}
		return getDynamicFieldDto(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DynamicFieldService#updateDynamicField()
	 */
	@Override
	@Transactional
	public DynamicFieldResponseDto updateDynamicField(String id, DynamicFieldDto dto) {
		DynamicField entity = null;
		try {
			int updatedRows = dynamicFieldRepository.updateDynamicField(id, dto.getDescription(), dto.getLangCode(), 
					dto.getDataType(), dto.isActive(), MetaDataUtils.getCurrentDateTime(), MetaDataUtils.getContextUser());
			
			if (updatedRows < 1) {
				throw new RequestException(SchemaErrorCode.DYNAMIC_FIELD_NOT_FOUND_EXCEPTION.getErrorCode(),
						SchemaErrorCode.DYNAMIC_FIELD_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
			
			entity = dynamicFieldRepository.findDynamicFieldById(id);
			
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(SchemaErrorCode.DYNAMIC_FIELD_UPDATE_EXCEPTION.getErrorCode(),
					ExceptionUtils.parseException(e));
		}
		return getDynamicFieldDto(entity);
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DynamicFieldService#updateFieldValue()
	 */
	@Override
	@Transactional
	public String updateFieldValue(String fieldId, DynamicFieldValueDto dto) {		
		DynamicField entity = null;
		try {
			entity = dynamicFieldRepository.findDynamicFieldById(fieldId);
			if(entity == null)
				throw new RequestException(SchemaErrorCode.DYNAMIC_FIELD_NOT_FOUND_EXCEPTION.getErrorCode(),
						SchemaErrorCode.DYNAMIC_FIELD_NOT_FOUND_EXCEPTION.getErrorMessage());
			
			String fieldVal = getFieldValue(entity.getValueJson(), dto);
			
			int updatedRows = dynamicFieldRepository.updateDynamicFieldValue(fieldId, fieldVal, dto.getLangCode(), 
					MetaDataUtils.getCurrentDateTime(), MetaDataUtils.getContextUser());
			
			if (updatedRows < 1) {
				throw new RequestException(SchemaErrorCode.DYNAMIC_FIELD_NOT_FOUND_EXCEPTION.getErrorCode(),
						SchemaErrorCode.DYNAMIC_FIELD_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
			
		} catch (DataAccessLayerException | DataAccessException | JsonProcessingException e) {
			throw new MasterDataServiceException(SchemaErrorCode.DYNAMIC_FIELD_UPDATE_EXCEPTION.getErrorCode(),
					ExceptionUtils.parseException(e));
		}
		return fieldId;		
	}
	
	private String getValueJson(List<DynamicFieldValueDto> fieldValues) {
		String valueJson = "[]";
		try {
			if(fieldValues == null)
				return valueJson;
			
			for(DynamicFieldValueDto valueDto : fieldValues) {
				valueJson = getFieldValue(valueJson, valueDto);
			}
		} catch(JsonProcessingException e) {
			LOGGER.error("Failed to parse field value passed : ", e);
		}
		return valueJson;
	}

		
	private void checkIfDynamicFieldExists(String fieldName, String langCode) {
		DynamicField dynamicField = dynamicFieldRepository.findDynamicFieldByNameAndLangCode(fieldName, langCode);
		
		if(dynamicField != null) {
			throw new RequestException(SchemaErrorCode.DYNAMIC_FIELD_ALREADY_EXISTS.getErrorCode(),
					SchemaErrorCode.DYNAMIC_FIELD_ALREADY_EXISTS.getErrorMessage());
		}		
	}
	
	private DynamicFieldResponseDto getDynamicFieldDto(DynamicField entity) {
		DynamicFieldResponseDto dto = new DynamicFieldResponseDto();
		dto.setActive(entity.getIsActive());
		dto.setDataType(entity.getDataType());
		dto.setDescription(entity.getDescription());
		dto.setId(entity.getId());
		dto.setLangCode(entity.getLangCode());
		dto.setName(entity.getName());
		dto.setCreatedBy(entity.getCreatedBy());
		dto.setCreatedOn(entity.getCreatedDateTime());
		dto.setUpdatedBy(entity.getUpdatedBy());
		dto.setUpdatedOn(entity.getUpdatedDateTime());
		dto.setFieldVal(convertJsonStringToFieldValueDto(entity.getValueJson()));
		return dto;
	}
	
	
	private List<DynamicFieldValueDto> convertJsonStringToFieldValueDto(String jsonString) {
		List<DynamicFieldValueDto> valueDtoList = new ArrayList<>();
		try {
			valueDtoList = objectMapper.readValue(jsonString == null ? "[]" : jsonString, new TypeReference<List<DynamicFieldValueDto>>() {});
		} catch (IOException e) {
			throw new MasterDataServiceException(SchemaErrorCode.VALUE_PARSE_ERROR.getErrorCode(),
					ExceptionUtils.parseException(e));
		}
		return valueDtoList;
	}
	
	//TODO should also validate datatype but how will you do it for non-english languages ?
	private String getFieldValue(String jsonString, DynamicFieldValueDto dto) throws JsonProcessingException {
		List<DynamicFieldValueDto> valueDtoList = convertJsonStringToFieldValueDto(jsonString);
				
		boolean updatedExisting = false;
		if(valueDtoList != null) {
			for (DynamicFieldValueDto fieldValDto : valueDtoList) {
				if(fieldValDto.equals(dto)) {
					fieldValDto.setActive(dto.isActive());
					fieldValDto.setValue(dto.getValue());
					updatedExisting = true;
					break;
				}
			}
			
			if(!updatedExisting)
				valueDtoList.add(dto);
		}	
		
		return objectMapper.writeValueAsString(valueDtoList);
	}

}
