package io.mosip.kernel.masterdata.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.datamapper.spi.DataMapper;
import io.mosip.kernel.masterdata.constant.ApplicationErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.ApplicationDto;
import io.mosip.kernel.masterdata.dto.GenderTypeDto;
import io.mosip.kernel.masterdata.dto.getresponse.ApplicationResponseDto;
import io.mosip.kernel.masterdata.entity.Application;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.repository.ApplicationRepository;
import io.mosip.kernel.masterdata.service.ApplicationService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * Service API implementaion class for Application
 * 
 * @author Neha
 * @since 1.0.0
 *
 */
@Service
public class ApplicationServiceImpl implements ApplicationService {

	@Autowired
	private ApplicationRepository applicationRepository;

	@Qualifier("applicationtoToApplicationDtoDefaultMapper")
	@Autowired
	private DataMapper<Application, ApplicationDto> applicationtoToApplicationDtoDefaultMapper;

	@Qualifier("applicationToCodeandlanguagecodeDefaultMapper")
	@Autowired
	private DataMapper<Application, CodeAndLanguageCodeID> applicationToCodeandlanguagecodeDefaultMapper;
	
	@Autowired
	private MasterdataCreationUtil masterdataCreationUtil;

	@Autowired
	private AuditUtil auditUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.ApplicationService#getAllApplication()
	 */
	@Override
	public ApplicationResponseDto getAllApplication() {
		List<ApplicationDto> applicationDtoList = new ArrayList<>();
		List<Application> applicationList;
		try {
			applicationList = applicationRepository.findAllByIsDeletedFalseOrIsDeletedNull(Application.class);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(ApplicationErrorCode.APPLICATION_FETCH_EXCEPTION.getErrorCode(),
					ApplicationErrorCode.APPLICATION_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}

		if (!(applicationList.isEmpty())) {
			applicationList.forEach(
					application -> applicationDtoList.add(applicationtoToApplicationDtoDefaultMapper.map(application)));
		} else {
			throw new DataNotFoundException(ApplicationErrorCode.APPLICATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					ApplicationErrorCode.APPLICATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		ApplicationResponseDto applicationResponseDto = new ApplicationResponseDto();
		applicationResponseDto.setApplicationtypes(applicationDtoList);
		return applicationResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.ApplicationService#
	 * getAllApplicationByLanguageCode(java.lang.String)
	 */
	@Override
	public ApplicationResponseDto getAllApplicationByLanguageCode(String languageCode) {
		List<ApplicationDto> applicationDtoList = new ArrayList<>();
		List<Application> applicationList;
		try {
			applicationList = applicationRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(languageCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(ApplicationErrorCode.APPLICATION_FETCH_EXCEPTION.getErrorCode(),
					ApplicationErrorCode.APPLICATION_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		if (!(applicationList.isEmpty())) {
			applicationList.forEach(
					application -> applicationDtoList.add(applicationtoToApplicationDtoDefaultMapper.map(application)));
		} else {
			throw new DataNotFoundException(ApplicationErrorCode.APPLICATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					ApplicationErrorCode.APPLICATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		ApplicationResponseDto applicationResponseDto = new ApplicationResponseDto();
		applicationResponseDto.setApplicationtypes(applicationDtoList);
		return applicationResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.ApplicationService#
	 * getApplicationByCodeAndLanguageCode(java.lang.String, java.lang.String)
	 */
	@Override
	public ApplicationResponseDto getApplicationByCodeAndLanguageCode(String code, String languageCode) {
		Application application;
		List<ApplicationDto> applicationDtoList = new ArrayList<>();
		try {
			application = applicationRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(code,
					languageCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(ApplicationErrorCode.APPLICATION_FETCH_EXCEPTION.getErrorCode(),
					ApplicationErrorCode.APPLICATION_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		if (application != null) {
			applicationDtoList.add(applicationtoToApplicationDtoDefaultMapper.map(application));
		} else {
			throw new DataNotFoundException(ApplicationErrorCode.APPLICATION_NOT_FOUND_EXCEPTION.getErrorCode(),
					ApplicationErrorCode.APPLICATION_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		ApplicationResponseDto applicationResponseDto = new ApplicationResponseDto();
		applicationResponseDto.setApplicationtypes(applicationDtoList);
		return applicationResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.ApplicationService#createApplication(io.
	 * mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID createApplication(ApplicationDto applicationRequestDto) {
		
		Application application=null;
		try {
			applicationRequestDto=masterdataCreationUtil.createMasterData(Application.class, applicationRequestDto);
			Application entity = MetaDataUtils.setCreateMetaData(applicationRequestDto, Application.class);
			application = applicationRepository.create(entity);
		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_CREATE, GenderTypeDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							ApplicationErrorCode.APPLICATION_INSERT_EXCEPTION.getErrorCode(),
							ApplicationErrorCode.APPLICATION_INSERT_EXCEPTION.getErrorMessage()
									+ ExceptionUtils.parseException(e)));
			throw new MasterDataServiceException(ApplicationErrorCode.APPLICATION_INSERT_EXCEPTION.getErrorCode(),
					ApplicationErrorCode.APPLICATION_INSERT_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));

		}
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_CREATE, GenderTypeDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						ApplicationDto.class.getSimpleName(), application.getCode()));
		return applicationToCodeandlanguagecodeDefaultMapper.map(application);
	}
}
