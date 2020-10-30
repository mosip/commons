package io.mosip.kernel.masterdata.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.BiometricAttributeErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.BiometricAttributeDto;
import io.mosip.kernel.masterdata.entity.BiometricAttribute;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.repository.BiometricAttributeRepository;
import io.mosip.kernel.masterdata.service.BiometricAttributeService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * This class have methods to fetch a biomettic attribute
 * 
 * @author Uday Kumar
 * @since 1.0.0
 *
 */

@Service
public class BiometricAttributeServiecImpl implements BiometricAttributeService {
	@Autowired
	private BiometricAttributeRepository biometricAttributeRepository;
	
	@Autowired
	private MasterdataCreationUtil masterdataCreationUtil;

	@Autowired
	private AuditUtil auditUtil;


	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.BiometricAttributeService#
	 * getBiometricAttribute(java.lang.String, java.lang.String)
	 */
	@Override
	public List<BiometricAttributeDto> getBiometricAttribute(String biometricTypeCode, String langCode) {
		List<BiometricAttributeDto> attributesDto = null;
		List<BiometricAttribute> attributes = null;
		try {
			attributes = biometricAttributeRepository
					.findByBiometricTypeCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(biometricTypeCode, langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(
					BiometricAttributeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorCode(),
					BiometricAttributeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		if (attributes != null && !attributes.isEmpty()) {
			attributesDto = MapperUtils.mapAll(attributes, BiometricAttributeDto.class);
		} else {
			throw new DataNotFoundException(
					BiometricAttributeErrorCode.BIOMETRICATTRIBUTE_NOT_FOUND_EXCEPTION.getErrorCode(),
					BiometricAttributeErrorCode.BIOMETRICATTRIBUTE_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		return attributesDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.BiometricAttributeService#
	 * createBiometricAttribute(io.mosip.kernel.masterdata.dto.
	 * BiometricAttributeDto)
	 */
	@Override
	public CodeAndLanguageCodeID createBiometricAttribute(BiometricAttributeDto biometricAttribute) {
		BiometricAttribute entity;
		try {
			biometricAttribute=masterdataCreationUtil.createMasterData(BiometricAttribute.class, biometricAttribute);
			 entity = MetaDataUtils.setCreateMetaData(biometricAttribute, BiometricAttribute.class);
			entity = biometricAttributeRepository.create(entity);
		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_CREATE, BiometricAttribute.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							BiometricAttributeErrorCode.BIOMETRICATTRIBUTE_INSERT_EXCEPTION.getErrorCode(),
							BiometricAttributeErrorCode.BIOMETRICATTRIBUTE_INSERT_EXCEPTION.getErrorMessage()
									+ ExceptionUtils.parseException(e)));
			throw new MasterDataServiceException(
					BiometricAttributeErrorCode.BIOMETRICATTRIBUTE_INSERT_EXCEPTION.getErrorCode(),
					BiometricAttributeErrorCode.BIOMETRICATTRIBUTE_INSERT_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		
		}
		CodeAndLanguageCodeID codeAndLanguageCodeId = new CodeAndLanguageCodeID();

		MapperUtils.map(entity, codeAndLanguageCodeId);
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_CREATE, BiometricAttribute.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						BiometricAttribute.class.getSimpleName(), codeAndLanguageCodeId.getCode()));
		return codeAndLanguageCodeId;
	}

}
