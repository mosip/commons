package io.mosip.kernel.masterdata.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.TemplateTypeErrorCode;
import io.mosip.kernel.masterdata.dto.TemplateTypeDto;
import io.mosip.kernel.masterdata.dto.TemplateTypeResponseDto;
import io.mosip.kernel.masterdata.entity.TemplateType;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.repository.TemplateTypeRepository;
import io.mosip.kernel.masterdata.service.TemplateTypeService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * 
 * @author Uday Kumar
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

@Service
public class TemplateTypeServiceImpl implements TemplateTypeService {

	@Autowired
	private TemplateTypeRepository templateTypeRepository;

	@Autowired
	private MasterdataCreationUtil masterdataCreationUtil;

	@Autowired
	private AuditUtil auditUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TemplateTypeService#createTemplateType(io.
	 * mosip.kernel.masterdata.dto.TemplateTypeDto)
	 */
	@Override
	public CodeAndLanguageCodeID createTemplateType(TemplateTypeDto templateTypeDto) {

		TemplateType templateType;
		try {
			templateTypeDto = masterdataCreationUtil.createMasterData(TemplateType.class, templateTypeDto);
			TemplateType entity = MetaDataUtils.setCreateMetaData(templateTypeDto, TemplateType.class);
			templateType = templateTypeRepository.create(entity);

		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.CREATE_ERROR_AUDIT, TemplateType.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							TemplateTypeErrorCode.TEMPLATE_TYPE_INSERT_EXCEPTION.getErrorCode(),
							TemplateTypeErrorCode.TEMPLATE_TYPE_INSERT_EXCEPTION.getErrorMessage()));
			throw new MasterDataServiceException(TemplateTypeErrorCode.TEMPLATE_TYPE_INSERT_EXCEPTION.getErrorCode(),
					TemplateTypeErrorCode.TEMPLATE_TYPE_INSERT_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}

		CodeAndLanguageCodeID codeLangCodeId = new CodeAndLanguageCodeID();
		MapperUtils.map(templateType, codeLangCodeId);
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_CREATE, TemplateType.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						TemplateType.class.getSimpleName(), codeLangCodeId.getCode()));
		return codeLangCodeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateTypeService#
	 * getTemplateTypeCodeandLangCode(java.lang.String, java.lang.String)
	 */
	@Override
	public TemplateTypeResponseDto getTemplateTypeCodeandLangCode(String templateTypeCode, String langCode) {
		List<TemplateType> templateTypeList = null;
		List<TemplateTypeDto> templateTypeDtoList = null;
		TemplateTypeResponseDto templateTypeResponseDto = new TemplateTypeResponseDto();
		try {
			templateTypeList = templateTypeRepository
					.findAllByCodeAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(templateTypeCode, langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(TemplateTypeErrorCode.TEMPLATE_TYPE_FETCH_EXCEPTION.getErrorCode(),
					TemplateTypeErrorCode.TEMPLATE_TYPE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (templateTypeList != null && !templateTypeList.isEmpty()) {
			templateTypeDtoList = MapperUtils.mapAll(templateTypeList, TemplateTypeDto.class);
		} else {

			throw new DataNotFoundException(TemplateTypeErrorCode.TEMPLATE_TYPE_NOT_FOUND_EXCEPTION.getErrorCode(),
					TemplateTypeErrorCode.TEMPLATE_TYPE_NOT_FOUND_EXCEPTION.getErrorMessage());

		}
		templateTypeResponseDto.setTemplateTypes(templateTypeDtoList);
		return templateTypeResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateTypeService#
	 * getTemplateTypeLangCode(java.lang.String)
	 */
	@Override
	public TemplateTypeResponseDto getTemplateTypeLangCode(String langCode) {
		TemplateTypeResponseDto templateTypeResponseDto = new TemplateTypeResponseDto();
		List<TemplateType> templateTypeList = null;
		List<TemplateTypeDto> templateTypeDtoList = null;
		try {
			templateTypeList = templateTypeRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(TemplateTypeErrorCode.TEMPLATE_TYPE_FETCH_EXCEPTION.getErrorCode(),
					TemplateTypeErrorCode.TEMPLATE_TYPE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (templateTypeList != null && !templateTypeList.isEmpty()) {
			templateTypeDtoList = MapperUtils.mapAll(templateTypeList, TemplateTypeDto.class);

		} else {
			throw new DataNotFoundException(TemplateTypeErrorCode.TEMPLATE_TYPE_NOT_FOUND_EXCEPTION.getErrorCode(),
					TemplateTypeErrorCode.TEMPLATE_TYPE_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		templateTypeResponseDto.setTemplateTypes(templateTypeDtoList);
		return templateTypeResponseDto;
	}

}
