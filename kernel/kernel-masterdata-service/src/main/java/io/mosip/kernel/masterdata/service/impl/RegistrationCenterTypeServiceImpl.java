package io.mosip.kernel.masterdata.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.ApplicationErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.RegistrationCenterTypeErrorCode;
import io.mosip.kernel.masterdata.dto.FilterData;
import io.mosip.kernel.masterdata.dto.MachineDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterTypeDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.RegistrationCenterTypeExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.ColumnCodeValue;
import io.mosip.kernel.masterdata.dto.response.FilterResponseCodeDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.entity.RegistrationCenterType;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterTypeRepository;
import io.mosip.kernel.masterdata.service.RegistrationCenterTypeService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.MasterdataSearchHelper;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;
import io.mosip.kernel.masterdata.utils.PageUtils;
import io.mosip.kernel.masterdata.validator.FilterColumnValidator;
import io.mosip.kernel.masterdata.validator.FilterTypeValidator;

/**
 * Implementation class for {@link RegistrationCenterTypeService}.
 * 
 * @author Sagar Mahapatra
 * @author Megha Tanga
 * @author Srinivasan
 * @since 1.0.0
 *
 */
@Service
@Transactional
public class RegistrationCenterTypeServiceImpl implements RegistrationCenterTypeService {

	/**
	 * Autowired reference for {@link RegistrationCenterTypeRepository}.
	 */
	@Autowired
	private RegistrationCenterTypeRepository registrationCenterTypeRepository;

	/**
	 * Autowired reference for {@link RegistrationCenteRepository}.
	 */
	@Autowired
	private RegistrationCenterRepository registrationCenterRepository;

	@Autowired
	FilterTypeValidator filterTypeValidator;

	@Autowired
	MasterdataSearchHelper masterdataSearchHelper;

	@Autowired
	FilterColumnValidator filterColumnValidator;

	@Autowired
	MasterDataFilterHelper masterDataFilterHelper;

	@Autowired
	private PageUtils pageUtils;

	@Autowired
	private AuditUtil auditUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterTypeService#
	 * createRegistrationCenterType(io.mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID createRegistrationCenterType(
			RegistrationCenterTypeDto registrationCenterTypeRequestDto) {
		RegistrationCenterType entity = MetaDataUtils.setCreateMetaData(registrationCenterTypeRequestDto,
				RegistrationCenterType.class);
		RegistrationCenterType registrationCenterType;
		try {
			registrationCenterType = registrationCenterTypeRepository.create(entity);
		} catch (DataAccessLayerException | DataAccessException exception) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_CREATE, RegistrationCenterTypeDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							ApplicationErrorCode.APPLICATION_INSERT_EXCEPTION.getErrorCode(),
							ApplicationErrorCode.APPLICATION_INSERT_EXCEPTION.getErrorMessage()
									+ ExceptionUtils.parseException(exception)),
					"ADM-556");
			throw new MasterDataServiceException(ApplicationErrorCode.APPLICATION_INSERT_EXCEPTION.getErrorCode(),
					ApplicationErrorCode.APPLICATION_INSERT_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		CodeAndLanguageCodeID codeAndLanguageCodeID = new CodeAndLanguageCodeID();
		MapperUtils.map(registrationCenterType, codeAndLanguageCodeID);
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_CREATE, RegistrationCenterTypeDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						RegistrationCenterTypeDto.class.getSimpleName(), codeAndLanguageCodeID.getCode()),
				"ADM-555");
		return codeAndLanguageCodeID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterTypeService#
	 * updateRegistrationCenterType(io.mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID updateRegistrationCenterType(RegistrationCenterTypeDto registrationCenterTypeDto) {
		CodeAndLanguageCodeID registrationCenterTypeId = new CodeAndLanguageCodeID();
		MapperUtils.mapFieldValues(registrationCenterTypeDto, registrationCenterTypeId);
		try {
			RegistrationCenterType registrationCenterTypeEntity = registrationCenterTypeRepository
					.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(registrationCenterTypeDto.getCode(),
							registrationCenterTypeDto.getLangCode());
			if (registrationCenterTypeEntity != null) {
				MetaDataUtils.setUpdateMetaData(registrationCenterTypeDto, registrationCenterTypeEntity, false);
				registrationCenterTypeRepository.update(registrationCenterTypeEntity);
			} else {
				auditUtil.auditRequest(
						String.format(
								MasterDataConstant.FAILURE_UPDATE, RegistrationCenterTypeDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(
								RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_NOT_FOUND_EXCEPTION
										.getErrorCode(),
								RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_NOT_FOUND_EXCEPTION
										.getErrorMessage()),
						"ADM-552");
				throw new RequestException(
						RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_NOT_FOUND_EXCEPTION.getErrorCode(),
						RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException exception) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_UPDATE, RegistrationCenterTypeDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_UPDATE_EXCEPTION.getErrorCode(),
							RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_UPDATE_EXCEPTION.getErrorMessage()
									+ ExceptionUtils.parseException(exception)),
					"ADM-553");
			throw new MasterDataServiceException(
					RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_UPDATE_EXCEPTION.getErrorCode(),
					RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_UPDATE_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_UPDATE, MachineDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_UPDATE_DESC,
						RegistrationCenterTypeDto.class.getSimpleName(), registrationCenterTypeId.getCode()),
				"ADM-554");
		return registrationCenterTypeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterTypeService#
	 * deleteRegistrationCenterType(java.lang.String)
	 */
	@Override
	public CodeResponseDto deleteRegistrationCenterType(String registrationCenterTypeCode) {
		try {
			List<RegistrationCenter> mappedRegistrationCenters = registrationCenterRepository
					.findByCenterTypeCode(registrationCenterTypeCode);
			if (!mappedRegistrationCenters.isEmpty()) {
				throw new MasterDataServiceException(
						RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_DELETE_DEPENDENCY_EXCEPTION
								.getErrorCode(),
						RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_DELETE_DEPENDENCY_EXCEPTION
								.getErrorMessage());
			}
			int deletedRegistrationCenterTypes = registrationCenterTypeRepository.deleteRegistrationCenterType(
					LocalDateTime.now(ZoneId.of("UTC")), registrationCenterTypeCode, MetaDataUtils.getContextUser());
			if (deletedRegistrationCenterTypes < 1) {
				throw new RequestException(
						RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_NOT_FOUND_EXCEPTION.getErrorCode(),
						RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException exception) {
			throw new MasterDataServiceException(
					RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_DELETE_EXCEPTION.getErrorCode(),
					RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_DELETE_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		CodeResponseDto responseDto = new CodeResponseDto();
		responseDto.setCode(registrationCenterTypeCode);
		return responseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterTypeService#
	 * getAllRegistrationCenterTypes(int, int, java.lang.String, java.lang.String)
	 */
	@Override
	public PageDto<RegistrationCenterTypeExtnDto> getAllRegistrationCenterTypes(int pageNumber, int pageSize,
			String sortBy, String orderBy) {
		List<RegistrationCenterTypeExtnDto> regCenterTypes = null;
		PageDto<RegistrationCenterTypeExtnDto> pageDto = null;
		try {

			Page<RegistrationCenterType> page = registrationCenterTypeRepository
					.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(Direction.fromString(orderBy), sortBy)));
			if (page != null && page.getContent() != null && !page.getContent().isEmpty()) {
				regCenterTypes = MapperUtils.mapAll(page.getContent(), RegistrationCenterTypeExtnDto.class);
				pageDto = new PageDto<>(page.getNumber(), page.getTotalPages(), page.getTotalElements(),
						regCenterTypes);
			} else {
				throw new DataNotFoundException(
						RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_NOT_FOUND_EXCEPTION.getErrorCode(),
						RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_NOT_FOUND_EXCEPTION.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException exception) {
			throw new MasterDataServiceException(
					RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterTypeErrorCode.REGISTRATION_CENTER_TYPE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		return pageDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterTypeService#
	 * registrationCenterTypeFilterValues(io.mosip.kernel.masterdata.dto.request.
	 * FilterValueDto)
	 */
	@Override
	public FilterResponseCodeDto registrationCenterTypeFilterValues(FilterValueDto filterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(),
				RegistrationCenterType.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<FilterData> filterValues = masterDataFilterHelper
						.filterValuesWithCode(RegistrationCenterType.class, filterDto, filterValueDto, "code");
				filterValues.forEach(filterValue -> {
					ColumnCodeValue columnValue = new ColumnCodeValue();
					columnValue.setFieldCode(filterValue.getFieldCode());
					columnValue.setFieldID(filterDto.getColumnName());
					columnValue.setFieldValue(filterValue.getFieldValue());
					columnValueList.add(columnValue);
				});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		return filterResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterTypeService#
	 * searchRegistrationCenterTypes(io.mosip.kernel.masterdata.dto.request.
	 * SearchDto)
	 */
	@Override
	public PageResponseDto<RegistrationCenterTypeExtnDto> searchRegistrationCenterTypes(SearchDto dto) {
		PageResponseDto<RegistrationCenterTypeExtnDto> pageDto = new PageResponseDto<>();
		List<RegistrationCenterTypeExtnDto> registrationCenterTypes = null;
		if (filterTypeValidator.validate(RegistrationCenterTypeExtnDto.class, dto.getFilters())) {
			pageUtils.validateSortField(RegistrationCenterType.class, dto.getSort());
			Page<RegistrationCenterType> page = masterdataSearchHelper.searchMasterdata(RegistrationCenterType.class,
					dto, null);
			if (page.getContent() != null && !page.getContent().isEmpty()) {
				pageDto = PageUtils.pageResponse(page);
				registrationCenterTypes = MapperUtils.mapAll(page.getContent(), RegistrationCenterTypeExtnDto.class);
				pageDto.setData(registrationCenterTypes);
			}
		}
		return pageDto;
	}
}
