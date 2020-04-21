package io.mosip.kernel.masterdata.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.ModuleErrorCode;
import io.mosip.kernel.masterdata.dto.ModuleDto;
import io.mosip.kernel.masterdata.dto.ModuleResponseDto;
import io.mosip.kernel.masterdata.entity.ModuleDetail;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.repository.ModuleRepository;
import io.mosip.kernel.masterdata.service.ModuleService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;

/**
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

@Service
public class ModuleServiceImpl implements ModuleService {

	@Autowired
	private ModuleRepository moduleRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.ModuleService#getModuleIdandLangCode(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	public ModuleResponseDto getModuleIdandLangCode(String id, String langCode) {
		List<ModuleDetail> moduleList = null;
		List<ModuleDto> moduleDtoList = null;
		ModuleResponseDto moduleResponseDto = new ModuleResponseDto();
		try {
			moduleList = moduleRepository.findAllByIdAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(id, langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(ModuleErrorCode.MODULE_FETCH_EXCEPTION.getErrorCode(),
					ModuleErrorCode.MODULE_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		if (moduleList != null && !moduleList.isEmpty()) {
			moduleDtoList = MapperUtils.mapAll(moduleList, ModuleDto.class);
		} else {

			throw new DataNotFoundException(ModuleErrorCode.MODULE_NOT_FOUND_EXCEPTION.getErrorCode(),
					ModuleErrorCode.MODULE_NOT_FOUND_EXCEPTION.getErrorMessage());

		}
		moduleResponseDto.setModules(moduleDtoList);
		return moduleResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.ModuleService#getModuleLangCode(java.lang.
	 * String)
	 */
	@Override
	public ModuleResponseDto getModuleLangCode(String langCode) {
		ModuleResponseDto moduleResponseDto = new ModuleResponseDto();
		List<ModuleDetail> moduleList = null;
		List<ModuleDto> moduleDtoList = null;
		try {
			moduleList = moduleRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(ModuleErrorCode.MODULE_FETCH_EXCEPTION.getErrorCode(),
					ModuleErrorCode.MODULE_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		if (moduleList != null && !moduleList.isEmpty()) {
			moduleDtoList = MapperUtils.mapAll(moduleList, ModuleDto.class);

		} else {
			throw new DataNotFoundException(ModuleErrorCode.MODULE_NOT_FOUND_EXCEPTION.getErrorCode(),
					ModuleErrorCode.MODULE_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		moduleResponseDto.setModules(moduleDtoList);
		return moduleResponseDto;
	}
}
