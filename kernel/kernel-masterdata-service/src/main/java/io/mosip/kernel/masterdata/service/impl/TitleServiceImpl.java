package io.mosip.kernel.masterdata.service.impl;

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
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.TemplateErrorCode;
import io.mosip.kernel.masterdata.constant.TitleErrorCode;
import io.mosip.kernel.masterdata.dto.TemplateDto;
import io.mosip.kernel.masterdata.dto.TitleDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.TitleResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.TitleExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.ColumnValue;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.dto.response.RegistrationCenterSearchDto;
import io.mosip.kernel.masterdata.entity.Title;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.TitleRepository;
import io.mosip.kernel.masterdata.service.TitleService;
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
 * Implementing service class for fetching titles from master db
 * 
 * @author Sidhant Agarwal
 * @author Srinivasan
 * @since 1.0.0
 *
 */
@Service
public class TitleServiceImpl implements TitleService {

	@Autowired
	private TitleRepository titleRepository;

	@Autowired
	private FilterTypeValidator filterTypeValidator;

	@Autowired
	private MasterdataSearchHelper masterDataSearchHelper;

	@Autowired
	private FilterColumnValidator filterColumnValidator;

	@Autowired
	private MasterDataFilterHelper masterDataFilterHelper;

	@Autowired
	private PageUtils pageUtils;

	@Autowired
	private AuditUtil auditUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TitleService#getAllTitles()
	 */
	@Override
	public TitleResponseDto getAllTitles() {
		TitleResponseDto titleResponseDto = null;
		List<TitleDto> titleDto = null;
		List<Title> titles = null;
		try {
			titles = titleRepository.findAll(Title.class);
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(TitleErrorCode.TITLE_FETCH_EXCEPTION.getErrorCode(),
					TitleErrorCode.TITLE_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		if (titles != null && !titles.isEmpty()) {
			titleDto = MapperUtils.mapAll(titles, TitleDto.class);
		} else {
			throw new DataNotFoundException(TitleErrorCode.TITLE_NOT_FOUND.getErrorCode(),
					TitleErrorCode.TITLE_NOT_FOUND.getErrorMessage());
		}
		titleResponseDto = new TitleResponseDto();
		titleResponseDto.setTitleList(titleDto);
		return titleResponseDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TitleService#getByLanguageCode(java.lang.
	 * String)
	 */
	@Override
	public TitleResponseDto getByLanguageCode(String languageCode) {
		TitleResponseDto titleResponseDto = null;
		List<TitleDto> titleDto = null;
		List<Title> title = null;

		try {
			title = titleRepository.getThroughLanguageCode(languageCode);
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(TitleErrorCode.TITLE_FETCH_EXCEPTION.getErrorCode(),
					TitleErrorCode.TITLE_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		if (title.isEmpty()) {
			throw new DataNotFoundException(TitleErrorCode.TITLE_NOT_FOUND.getErrorCode(),
					TitleErrorCode.TITLE_NOT_FOUND.getErrorMessage());
		}
		titleDto = MapperUtils.mapAll(title, TitleDto.class);

		titleResponseDto = new TitleResponseDto();
		titleResponseDto.setTitleList(titleDto);

		return titleResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TitleService#saveTitle(io.mosip.kernel.
	 * masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID saveTitle(TitleDto titleRequestDto) {
		Title entity = MetaDataUtils.setCreateMetaData(titleRequestDto, Title.class);
		Title title;
		try {
			title = titleRepository.create(entity);
		} catch (DataAccessLayerException | DataAccessException e) {
			auditUtil.auditRequest(String.format(MasterDataConstant.CREATE_ERROR_AUDIT, TitleDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.FAILURE_DESC,
							TitleErrorCode.TITLE_INSERT_EXCEPTION.getErrorCode(), ExceptionUtils.parseException(e)),
					"ADM-819");
			throw new MasterDataServiceException(TitleErrorCode.TITLE_INSERT_EXCEPTION.getErrorCode(),
					ExceptionUtils.parseException(e));
		}
		CodeAndLanguageCodeID codeLangCodeId = new CodeAndLanguageCodeID();
		MapperUtils.map(title, codeLangCodeId);
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_CREATE, TitleDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						TitleDto.class.getSimpleName(), codeLangCodeId.getCode()),
				"ADM-820");
		return codeLangCodeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TitleService#updateTitle(io.mosip.kernel.
	 * masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID updateTitle(TitleDto titles) {

		TitleDto titleDto = titles;

		CodeAndLanguageCodeID titleId = new CodeAndLanguageCodeID();

		MapperUtils.mapFieldValues(titleDto, titleId);
		try {

			Title title = titleRepository.findById(Title.class, titleId);

			if (title != null) {
				MetaDataUtils.setUpdateMetaData(titleDto, title, false);
				titleRepository.update(title);
			} else {
				auditUtil.auditRequest(String.format(MasterDataConstant.FAILURE_UPDATE, TitleDto.class.getSimpleName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC, TitleErrorCode.TITLE_NOT_FOUND.getErrorCode(),
								TitleErrorCode.TITLE_NOT_FOUND.getErrorMessage()),
						"ADM-821");
				throw new RequestException(TitleErrorCode.TITLE_NOT_FOUND.getErrorCode(),
						TitleErrorCode.TITLE_NOT_FOUND.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			auditUtil.auditRequest(String.format(MasterDataConstant.FAILURE_UPDATE, TitleDto.class.getSimpleName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC, TitleErrorCode.TITLE_UPDATE_EXCEPTION.getErrorCode(),
							TitleErrorCode.TITLE_UPDATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e)),
					"ADM-822");
			throw new MasterDataServiceException(TitleErrorCode.TITLE_UPDATE_EXCEPTION.getErrorCode(),
					TitleErrorCode.TITLE_UPDATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		auditUtil.auditRequest(String.format(MasterDataConstant.SUCCESSFUL_UPDATE, TitleDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_UPDATE_DESC,
						TitleDto.class.getSimpleName(), titleId.getCode()),
				"ADM-823");
		return titleId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TitleService#deleteTitle(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	@Transactional
	public CodeResponseDto deleteTitle(String code) {
		try {

			final List<Title> titleList = titleRepository.findByCode(code);

			if (!titleList.isEmpty()) {
				titleList.stream().map(MetaDataUtils::setDeleteMetaData).forEach(titleRepository::update);

			} else {
				throw new RequestException(TitleErrorCode.TITLE_NOT_FOUND.getErrorCode(),
						TitleErrorCode.TITLE_NOT_FOUND.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(TitleErrorCode.TITLE_DELETE_EXCEPTION.getErrorCode(),
					TitleErrorCode.TITLE_DELETE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		CodeResponseDto responseDto = new CodeResponseDto();
		responseDto.setCode(code);
		return responseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TitleService#getTitles(int, int,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public PageDto<TitleExtnDto> getTitles(int pageNumber, int pageSize, String sortBy, String orderBy) {
		List<TitleExtnDto> titles = null;
		PageDto<TitleExtnDto> pageDto = null;
		try {
			Page<Title> pageData = titleRepository
					.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(Direction.fromString(orderBy), sortBy)));
			if (pageData != null && pageData.getContent() != null && !pageData.getContent().isEmpty()) {
				titles = MapperUtils.mapAll(pageData.getContent(), TitleExtnDto.class);
				pageDto = new PageDto<>(pageData.getNumber(), pageData.getTotalPages(), pageData.getTotalElements(),
						titles);
			} else {
				throw new DataNotFoundException(TitleErrorCode.TITLE_NOT_FOUND.getErrorCode(),
						TitleErrorCode.TITLE_NOT_FOUND.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(TitleErrorCode.TITLE_FETCH_EXCEPTION.getErrorCode(),
					TitleErrorCode.TITLE_FETCH_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		return pageDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TitleService#searchTitles(io.mosip.kernel.
	 * masterdata.dto.request.SearchDto)
	 */
	@Override
	public PageResponseDto<TitleExtnDto> searchTitles(SearchDto searchDto) {
		PageResponseDto<TitleExtnDto> pageDto = new PageResponseDto<>();
		List<TitleExtnDto> titles = null;
		if (filterTypeValidator.validate(TitleExtnDto.class, searchDto.getFilters())) {
			pageUtils.validateSortField(Title.class, searchDto.getSort());
			Page<Title> page = masterDataSearchHelper.searchMasterdata(Title.class, searchDto, null);
			if (page.getContent() != null && !page.getContent().isEmpty()) {
				pageDto = PageUtils.pageResponse(page);
				titles = MapperUtils.mapAll(page.getContent(), TitleExtnDto.class);
				pageDto.setData(titles);
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
	public FilterResponseDto filterTitles(FilterValueDto filterValueDto) {
		FilterResponseDto filterResponseDto = new FilterResponseDto();
		List<ColumnValue> columnValueList = new ArrayList<>();

		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), Title.class)) {
			filterValueDto.getFilters().stream().forEach(filter -> {
				masterDataFilterHelper.filterValues(Title.class, filter, filterValueDto).forEach(filteredValue -> {
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
