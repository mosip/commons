package io.mosip.kernel.masterdata.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.masterdata.constant.MachineTypeErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.FilterData;
import io.mosip.kernel.masterdata.dto.MachineTypeDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.MachineTypeExtnDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.request.SearchFilter;
import io.mosip.kernel.masterdata.dto.response.ColumnCodeValue;
import io.mosip.kernel.masterdata.dto.response.FilterResponseCodeDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.Machine;
import io.mosip.kernel.masterdata.entity.MachineType;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.MachineTypeRepository;
import io.mosip.kernel.masterdata.service.MachineTypeService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterDataFilterHelper;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.MasterdataSearchHelper;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;
import io.mosip.kernel.masterdata.utils.OptionalFilter;
import io.mosip.kernel.masterdata.utils.PageUtils;
import io.mosip.kernel.masterdata.validator.FilterColumnValidator;
import io.mosip.kernel.masterdata.validator.FilterTypeValidator;

/**
 * This class have methods to save a Machine Type Details
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Service
public class MachineTypeServiceImpl implements MachineTypeService {

	/**
	 * Field to hold Machine Repository object
	 */
	@Autowired
	MachineTypeRepository machineTypeRepository;

	/**
	 * Reference to {@link FilterTypeValidator}.
	 */
	@Autowired
	private FilterTypeValidator filterValidator;

	/**
	 * Referencr to {@link MasterdataSearchHelper}.
	 */
	@Autowired
	private MasterdataSearchHelper masterdataSearchHelper;

	/**
	 * Reference to {@link MasterDataFilterHelper}.
	 */
	@Autowired
	private MasterDataFilterHelper masterDataFilterHelper;

	/**
	 * Refernece to {@link FilterColumnValidator}.
	 */
	@Autowired
	private FilterColumnValidator filterColumnValidator;

	@Autowired
	private PageUtils pageUtils;

	@Autowired
	private AuditUtil auditUtil;
	
	@Autowired
	private MasterdataCreationUtil masterdataCreationUtil;


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.MachineTypeService#createMachineType(io.
	 * mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID createMachineType(MachineTypeDto machineType) {
		MachineType renMachineType = null;



		try {
			machineType = masterdataCreationUtil.createMasterData(MachineType.class, machineType);
			MachineType entity = MetaDataUtils.setCreateMetaData(machineType, MachineType.class);
			renMachineType = machineTypeRepository.create(entity);
		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_CREATE, MachineType.class.getCanonicalName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							MachineTypeErrorCode.MACHINE_TYPE_INSERT_EXCEPTION.getErrorCode(),
							MachineTypeErrorCode.MACHINE_TYPE_INSERT_EXCEPTION.getErrorMessage()),
					"ADM-657");
			throw new MasterDataServiceException(MachineTypeErrorCode.MACHINE_TYPE_INSERT_EXCEPTION.getErrorCode(),
					MachineTypeErrorCode.MACHINE_TYPE_INSERT_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}

		CodeAndLanguageCodeID codeLangCodeId = new CodeAndLanguageCodeID();
		MapperUtils.map(renMachineType, codeLangCodeId);
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_CREATE, MachineType.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						MachineType.class.getSimpleName(), codeLangCodeId.getCode()));
		return codeLangCodeId;
	}
	
	@Override
	public CodeAndLanguageCodeID updateMachineType(MachineTypeDto machineTypeDto) {
		CodeAndLanguageCodeID codeAndLanguageCodeID = new CodeAndLanguageCodeID();
		try {
			MachineType machineType = machineTypeRepository.findtoUpdateMachineTypeByCodeAndByLangCode(machineTypeDto.getCode(),machineTypeDto.getLangCode());
			if (!EmptyCheckUtils.isNullEmpty(machineType)) {
				machineTypeDto = masterdataCreationUtil.updateMasterData(MachineType.class, machineTypeDto);
				MetaDataUtils.setUpdateMetaData(machineTypeDto, machineType, false);
				machineTypeRepository.update(machineType);
				MapperUtils.map(machineType, codeAndLanguageCodeID);
				if(!machineTypeDto.getIsActive()) {
					masterdataCreationUtil.updateMasterDataDeactivate(MachineType.class, machineTypeDto.getCode());
				}
			} else {
				throw new RequestException(MachineTypeErrorCode.MACHINE_TYPE_NOT_FOUND.getErrorCode(),
						MachineTypeErrorCode.MACHINE_TYPE_NOT_FOUND.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_CREATE, MachineType.class.getCanonicalName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							MachineTypeErrorCode.MACHINE_TYPE_UPDATE_EXCEPTION.getErrorCode(),
							MachineTypeErrorCode.MACHINE_TYPE_UPDATE_EXCEPTION.getErrorMessage()),
					"ADM-657");
			throw new MasterDataServiceException(MachineTypeErrorCode.MACHINE_TYPE_UPDATE_EXCEPTION.getErrorCode(),
					MachineTypeErrorCode.MACHINE_TYPE_UPDATE_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_UPDATE, MachineType.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_UPDATE_DESC,
						MachineType.class.getSimpleName(), codeAndLanguageCodeID.getCode()));
		return codeAndLanguageCodeID;
	}
	/*codeLangCodeId
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.MachineTypeService#getAllMachineTypes(int,
	 * int, java.lang.String, java.lang.String)
	 */
	@Override
	public PageDto<MachineTypeExtnDto> getAllMachineTypes(int pageNumber, int pageSize, String sortBy, String orderBy) {
		List<MachineTypeExtnDto> machineTypes = null;
		PageDto<MachineTypeExtnDto> machineTypesPages = null;
		try {
			Page<MachineType> pageData = machineTypeRepository
					.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(Direction.fromString(orderBy), sortBy)));
			if (pageData != null && pageData.getContent() != null && !pageData.getContent().isEmpty()) {
				machineTypes = MapperUtils.mapAll(pageData.getContent(), MachineTypeExtnDto.class);
				machineTypesPages = new PageDto<>(pageData.getNumber(), pageData.getTotalPages(),
						pageData.getTotalElements(), machineTypes);
			} else {
				throw new DataNotFoundException(MachineTypeErrorCode.MACHINE_TYPE_NOT_FOUND.getErrorCode(),
						MachineTypeErrorCode.MACHINE_TYPE_NOT_FOUND.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(MachineTypeErrorCode.MACHINE_TYPE_FETCH_EXCEPTION.getErrorCode(),
					MachineTypeErrorCode.MACHINE_TYPE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		return machineTypesPages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.MachineTypeService#searchMachineType(io.
	 * mosip.kernel.masterdata.dto.request.SearchDto)
	 */
	@Override
	public PageResponseDto<MachineTypeExtnDto> searchMachineType(SearchDto dto) {
		PageResponseDto<MachineTypeExtnDto> pageDto = new PageResponseDto<>();
		List<MachineTypeExtnDto> machineTypes = null;
		List<SearchFilter> addList = new ArrayList<>();
		if (filterValidator.validate(MachineTypeExtnDto.class, dto.getFilters())) {
			pageUtils.validateSortField(MachineType.class, dto.getSort());
			OptionalFilter optionalFilter = new OptionalFilter(addList);
			Page<MachineType> page = masterdataSearchHelper.searchMasterdata(MachineType.class, dto,
					new OptionalFilter[] { optionalFilter });
			if (page.getContent() != null && !page.getContent().isEmpty()) {
				pageDto = PageUtils.pageResponse(page);
				machineTypes = MapperUtils.mapAll(page.getContent(), MachineTypeExtnDto.class);
				pageDto.setData(machineTypes);
			}

		}
		return pageDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.MachineTypeService#
	 * machineTypesFilterValues(io.mosip.kernel.masterdata.dto.request.
	 * FilterValueDto)
	 */
	@Override
	public FilterResponseCodeDto machineTypesFilterValues(FilterValueDto filterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), Machine.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<FilterData> filterValues = masterDataFilterHelper.filterValuesWithCode(MachineType.class, filterDto,
						filterValueDto,"code");
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
}
