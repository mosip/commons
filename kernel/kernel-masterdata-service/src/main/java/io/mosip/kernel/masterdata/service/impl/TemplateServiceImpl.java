package io.mosip.kernel.masterdata.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.RegistrationCenterErrorCode;
import io.mosip.kernel.masterdata.constant.TemplateErrorCode;
import io.mosip.kernel.masterdata.dto.TemplateDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.TemplateResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.TemplateExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.ColumnValue;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.entity.Template;
import io.mosip.kernel.masterdata.entity.id.IdAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.TemplateRepository;
import io.mosip.kernel.masterdata.service.TemplateService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.MasterdataSearchHelper;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;
import io.mosip.kernel.masterdata.utils.PageUtils;
import io.mosip.kernel.masterdata.validator.FilterColumnValidator;
import io.mosip.kernel.masterdata.validator.FilterTypeValidator;

/**
 * 
 * @author Neha
 * @author Uday Kumar
 * @since 1.0.0
 *
 */
@Service
public class TemplateServiceImpl implements TemplateService {

	@Autowired
	private TemplateRepository templateRepository;

	private List<Template> templateList;

	private List<TemplateDto> templateDtoList;

	private TemplateResponseDto templateResponseDto = new TemplateResponseDto();

	@Autowired
	private FilterTypeValidator filterTypeValidator;

	@Autowired
	private FilterColumnValidator filterColumnValidator;

	@Autowired
	private MasterdataSearchHelper masterDataSearchHelper;

	@Autowired
	private MasterDataFilterHelper masterDataFilterHelper;

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private MasterdataCreationUtil masterdataCreationUtil;

	@Value("${mosip.primary-language:eng}")
	private String primaryLang;

	@Value("${mosip.secondary-language:ara}")
	private String secondaryLang;


	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateService#getAllTemplate()
	 */
	@Override
	public TemplateResponseDto getAllTemplate() {
		try {
			templateList = templateRepository.findAllByIsDeletedFalseOrIsDeletedIsNull(Template.class);
		} catch (DataAccessException | DataAccessLayerException exception) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		if (templateList != null && !templateList.isEmpty()) {
			templateDtoList = MapperUtils.mapAll(templateList, TemplateDto.class);
		} else {
			throw new DataNotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
					TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
		}
		templateResponseDto.setTemplates(templateDtoList);
		return templateResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateService#
	 * getAllTemplateByLanguageCode(java.lang.String)
	 */
	@Override
	public TemplateResponseDto getAllTemplateByLanguageCode(String languageCode) {
		try {
			templateList = templateRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(languageCode);
		} catch (DataAccessException | DataAccessLayerException exception) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		if (templateList != null && !templateList.isEmpty()) {
			templateDtoList = MapperUtils.mapAll(templateList, TemplateDto.class);
		} else {
			throw new DataNotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
					TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
		}
		templateResponseDto.setTemplates(templateDtoList);
		return templateResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateService#
	 * getAllTemplateByLanguageCodeAndTemplateTypeCode(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public TemplateResponseDto getAllTemplateByLanguageCodeAndTemplateTypeCode(String languageCode,
			String templateTypeCode) {
		try {
			templateList = templateRepository.findAllByLangCodeAndTemplateTypeCodeAndIsDeletedFalseOrIsDeletedIsNull(
					languageCode, templateTypeCode);
		} catch (DataAccessException | DataAccessLayerException exception) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		if (templateList != null && !templateList.isEmpty()) {
			templateDtoList = MapperUtils.mapAll(templateList, TemplateDto.class);
		} else {
			throw new DataNotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
					TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
		}
		templateResponseDto.setTemplates(templateDtoList);
		return templateResponseDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TemplateService#createTemplate(io.mosip.
	 * kernel.masterdata.dto.TemplateDto)
	 */
	@Override
	public IdAndLanguageCodeID createTemplate(TemplateDto template) {

		Template templateEntity;
		
		try {
			if (StringUtils.isNotEmpty(primaryLang) && primaryLang.equals(template.getLangCode())) {
				String uniqueId = generateId();
				template.setId(uniqueId);
			}
			template = masterdataCreationUtil.createMasterData(Template.class, template);
			Template entity = MetaDataUtils.setCreateMetaData(template, Template.class);
			templateEntity = templateRepository.create(entity);

		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.CREATE_ERROR_AUDIT, Template.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							TemplateErrorCode.TEMPLATE_INSERT_EXCEPTION.getErrorCode(),
							TemplateErrorCode.TEMPLATE_INSERT_EXCEPTION.getErrorMessage()),
					"ADM-812");
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_INSERT_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_INSERT_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));

		}

		IdAndLanguageCodeID idAndLanguageCodeID = new IdAndLanguageCodeID();
		MapperUtils.map(templateEntity, idAndLanguageCodeID);
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_CREATE, Template.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						Template.class.getSimpleName(), idAndLanguageCodeID.getId()),
				"ADM-813");
		return idAndLanguageCodeID;
	}
	
	private String generateId() throws DataAccessLayerException , DataAccessException{
		UUID uuid = UUID.randomUUID();
		String uniqueId = uuid.toString();
		
		Template template = templateRepository
				.findTemplateByIDAndLangCode(uniqueId,primaryLang);
			
		return template ==null?uniqueId:generateId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TemplateService#updateTemplates(io.mosip.
	 * kernel.masterdata.dto.TemplateDto)
	 */
	@Override
	public IdAndLanguageCodeID updateTemplates(TemplateDto template) {
		IdAndLanguageCodeID idAndLanguageCodeID = new IdAndLanguageCodeID();
		try {
			Template entity = templateRepository.findTemplateByIDAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(
					template.getId(), template.getLangCode());
			if (!EmptyCheckUtils.isNullEmpty(entity)) {
				template = masterdataCreationUtil.updateMasterData(Template.class, template);
				MetaDataUtils.setUpdateMetaData(template, entity, false);
				templateRepository.update(entity);
				idAndLanguageCodeID.setId(entity.getId());
				idAndLanguageCodeID.setLangCode(entity.getLangCode());
			} else {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_UPDATE, Template.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
								TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage()),
						"ADM-814");
				throw new RequestException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
						TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			auditUtil.auditRequest(String.format(MasterDataConstant.FAILURE_UPDATE, Template.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							TemplateErrorCode.TEMPLATE_UPDATE_EXCEPTION.getErrorCode(),
							TemplateErrorCode.TEMPLATE_UPDATE_EXCEPTION.getErrorMessage()
									+ ExceptionUtils.parseException(e)),
					"ADM-815");
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_UPDATE_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_UPDATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_UPDATE, Template.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_UPDATE_DESC,
						Template.class.getSimpleName(), idAndLanguageCodeID.getId()),
				"ADM-816");
		return idAndLanguageCodeID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TemplateService#deleteTemplates(java.lang.
	 * String)
	 */
	@Transactional
	@Override
	public IdResponseDto deleteTemplates(String id) {
		try {
			int updatedRows = templateRepository.deleteTemplate(id, MetaDataUtils.getCurrentDateTime(),
					MetaDataUtils.getContextUser());
			if (updatedRows < 1) {
				throw new RequestException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
						TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_DELETE_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_DELETE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		IdResponseDto idResponseDto = new IdResponseDto();
		idResponseDto.setId(id);
		return idResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateService#
	 * getAllTemplateByTemplateTypeCode(java.lang.String)
	 */
	@Override
	public TemplateResponseDto getAllTemplateByTemplateTypeCode(String templateTypeCode) {
		List<Template> templates;
		List<TemplateDto> templateDtos;
		try {
			templates = templateRepository
					.findAllByTemplateTypeCodeAndIsDeletedFalseOrIsDeletedIsNull(templateTypeCode);
		} catch (DataAccessException | DataAccessLayerException exception) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		if (templates != null && !templates.isEmpty()) {
			templateDtos = MapperUtils.mapAll(templates, TemplateDto.class);
		} else {
			throw new DataNotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
					TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
		}
		TemplateResponseDto responseDto = new TemplateResponseDto();
		responseDto.setTemplates(templateDtos);
		return responseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateService#getTemplates(int,
	 * int, java.lang.String, java.lang.String)
	 */
	@Override
	public PageDto<TemplateExtnDto> getTemplates(int pageNumber, int pageSize, String sortBy, String orderBy) {
		List<TemplateExtnDto> templates = null;
		PageDto<TemplateExtnDto> pageDto = null;
		try {
			Page<Template> pageData = templateRepository
					.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(Direction.fromString(orderBy), sortBy)));
			if (pageData != null && pageData.getContent() != null && !pageData.getContent().isEmpty()) {
				templates = MapperUtils.mapAll(pageData.getContent(), TemplateExtnDto.class);
				pageDto = new PageDto<>(pageData.getNumber(), pageData.getTotalPages(), pageData.getTotalElements(),
						templates);
			} else {
				throw new DataNotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
						TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
			}
		} catch (DataAccessException | DataAccessLayerException exception) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		return pageDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TemplateService#searchTemplates(io.mosip.
	 * kernel.masterdata.dto.request.SearchDto)
	 */
	@Override
	public PageResponseDto<TemplateExtnDto> searchTemplates(SearchDto searchDto) {
		PageResponseDto<TemplateExtnDto> pageDto = new PageResponseDto<>();
		List<TemplateExtnDto> templates = null;
		if (filterTypeValidator.validate(TemplateExtnDto.class, searchDto.getFilters())) {
			Page<Template> page = masterDataSearchHelper.searchMasterdata(Template.class, searchDto, null);
			if (page.getContent() != null && !page.getContent().isEmpty()) {
				pageDto = PageUtils.pageResponse(page);
				templates = MapperUtils.mapAll(page.getContent(), TemplateExtnDto.class);
				pageDto.setData(templates);
			}
		}
		return pageDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TemplateService#filterTemplates(io.mosip.
	 * kernel.masterdata.dto.request.FilterValueDto)
	 */
	@Override
	public FilterResponseDto filterTemplates(FilterValueDto filterValueDto) {
		FilterResponseDto filterResponseDto = new FilterResponseDto();
		List<ColumnValue> columnValueList = new ArrayList<>();

		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), Template.class)) {
			filterValueDto.getFilters().stream().forEach(filter -> {
				masterDataFilterHelper.filterValues(Template.class, filter, filterValueDto).forEach(filteredValue -> {
					if (filteredValue != null) {
						ColumnValue columnValue = new ColumnValue();
						columnValue.setFieldID(filter.getColumnName());
						columnValue.setFieldValue(filteredValue.toString());
						columnValueList.add(columnValue);
					}
				});
			});
			filterResponseDto.setFilters(columnValueList);
		}
		return filterResponseDto;
	}

}