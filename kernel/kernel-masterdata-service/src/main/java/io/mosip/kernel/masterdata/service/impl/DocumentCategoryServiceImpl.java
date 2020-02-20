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
import io.mosip.kernel.masterdata.constant.DocumentCategoryErrorCode;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.DocumentCategoryDto;
import io.mosip.kernel.masterdata.dto.getresponse.DocumentCategoryResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.DocumentCategoryExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.ColumnValue;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.DeviceType;
import io.mosip.kernel.masterdata.entity.DocumentCategory;
import io.mosip.kernel.masterdata.entity.ValidDocument;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.DocumentCategoryRepository;
import io.mosip.kernel.masterdata.repository.ValidDocumentRepository;
import io.mosip.kernel.masterdata.service.DocumentCategoryService;
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
 * This class have methods to fetch list of valid document category, create
 * document category based on provided data,update document category based on
 * data provided and delete document category based on id provided.
 * 
 * @author Neha
 * @author Ritesh Sinha
 * @author Uday Kumar
 * @since 1.0.0
 *
 */
@Service
@Transactional
public class DocumentCategoryServiceImpl implements DocumentCategoryService {

	@Autowired
	AuditUtil auditUtil;

	@Autowired
	private DocumentCategoryRepository documentCategoryRepository;

	@Autowired
	private ValidDocumentRepository validDocumentRepository;

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

	private List<DocumentCategory> documentCategoryList = new ArrayList<>();

	private DocumentCategoryResponseDto documentCategoryResponseDto = new DocumentCategoryResponseDto();

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DocumentCategoryService#
	 * getAllDocumentCategory()
	 */
	@Override
	public DocumentCategoryResponseDto getAllDocumentCategory() {
		List<DocumentCategoryDto> documentCategoryDtoList = new ArrayList<>();
		try {
			documentCategoryList = documentCategoryRepository
					.findAllByIsDeletedFalseOrIsDeletedIsNull(DocumentCategory.class);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage() + ExceptionUtils.parseException(e));
		}

		if (!(documentCategoryList.isEmpty())) {
			documentCategoryList.forEach(documentCategory -> {
				DocumentCategoryDto documentCategoryDto = new DocumentCategoryDto();
				MapperUtils.map(documentCategory, documentCategoryDto);
				documentCategoryDtoList.add(documentCategoryDto);
			});
		} else {
			throw new DataNotFoundException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorCode(),
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		documentCategoryResponseDto.setDocumentcategories(documentCategoryDtoList);
		return documentCategoryResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DocumentCategoryService#
	 * getAllDocumentCategoryByLaguageCode(java.lang.String)
	 */
	@Override
	public DocumentCategoryResponseDto getAllDocumentCategoryByLaguageCode(String langCode) {
		List<DocumentCategoryDto> documentCategoryDtoList = new ArrayList<>();
		try {
			documentCategoryList = documentCategoryRepository
					.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage() + ExceptionUtils.parseException(e));
		}

		if (!(documentCategoryList.isEmpty())) {
			documentCategoryList.forEach(documentCategory -> {
				DocumentCategoryDto documentCategoryDto = new DocumentCategoryDto();
				MapperUtils.map(documentCategory, documentCategoryDto);
				documentCategoryDtoList.add(documentCategoryDto);
			});
		} else {
			throw new DataNotFoundException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorCode(),
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		documentCategoryResponseDto.setDocumentcategories(documentCategoryDtoList);
		return documentCategoryResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DocumentCategoryService#
	 * getDocumentCategoryByCodeAndLangCode(java.lang.String, java.lang.String)
	 */
	@Override
	public DocumentCategoryResponseDto getDocumentCategoryByCodeAndLangCode(String code, String langCode) {
		List<DocumentCategoryDto> documentCategoryDtoList = new ArrayList<>();
		DocumentCategory documentCategory;
		DocumentCategoryDto documentCategoryDto;
		try {
			documentCategory = documentCategoryRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(code,
					langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage() + ExceptionUtils.parseException(e));
		}

		if (documentCategory != null) {
			documentCategoryDto = MapperUtils.map(documentCategory, DocumentCategoryDto.class);
		} else {
			throw new DataNotFoundException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorCode(),
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		documentCategoryDtoList.add(documentCategoryDto);
		documentCategoryResponseDto.setDocumentcategories(documentCategoryDtoList);
		return documentCategoryResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DocumentCategoryService#
	 * createDocumentCategory(io.mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID createDocumentCategory(DocumentCategoryDto category) {
		DocumentCategory entity = MetaDataUtils.setCreateMetaData(category, DocumentCategory.class);
		DocumentCategory documentCategory;
		try {
			documentCategory = documentCategoryRepository.create(entity);

		} catch (DataAccessLayerException | DataAccessException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_CREATE, DeviceType.class.getCanonicalName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DocumentCategoryErrorCode.DOCUMENT_CATEGORY_INSERT_EXCEPTION.getErrorCode(),
							DocumentCategoryErrorCode.DOCUMENT_CATEGORY_INSERT_EXCEPTION.getErrorMessage()),
					"ADM-802");
			throw new MasterDataServiceException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_INSERT_EXCEPTION.getErrorCode(),
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_INSERT_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		CodeAndLanguageCodeID codeLangCodeId = new CodeAndLanguageCodeID();
		MapperUtils.map(documentCategory, codeLangCodeId);

		return codeLangCodeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DocumentCategoryService#
	 * updateDocumentCategory(io.mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID updateDocumentCategory(DocumentCategoryDto categoryDto) {

		CodeAndLanguageCodeID documentCategoryId = new CodeAndLanguageCodeID();

		MapperUtils.mapFieldValues(categoryDto, documentCategoryId);
		try {

			DocumentCategory documentCategory = documentCategoryRepository
					.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(categoryDto.getCode(),
							categoryDto.getLangCode());

			if (documentCategory != null) {
				MetaDataUtils.setUpdateMetaData(categoryDto, documentCategory, false);
				documentCategoryRepository.update(documentCategory);
			} else {
				auditUtil.auditRequest(
						String.format(MasterDataConstant.FAILURE_UPDATE, DeviceType.class.getCanonicalName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorCode(),
								DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorMessage()),
						"ADM-803");
				throw new RequestException(
						DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorCode(),
						DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_UPDATE, DeviceType.class.getCanonicalName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							DocumentCategoryErrorCode.DOCUMENT_CATEGORY_UPDATE_EXCEPTION.getErrorCode(),
							DocumentCategoryErrorCode.DOCUMENT_CATEGORY_UPDATE_EXCEPTION.getErrorMessage()),
					"ADM-804");
			throw new MasterDataServiceException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_UPDATE_EXCEPTION.getErrorCode(),
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_UPDATE_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		return documentCategoryId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DocumentCategoryService#
	 * deleteDocumentCategory(java.lang.String)
	 */
	@Override
	public CodeResponseDto deleteDocumentCategory(String code) {

		try {
			List<ValidDocument> validDocument = validDocumentRepository.findByDocCategoryCode(code);

			if (!validDocument.isEmpty()) {
				throw new MasterDataServiceException(
						DocumentCategoryErrorCode.DOCUMENT_CATEGORY_DELETE_DEPENDENCY_EXCEPTION.getErrorCode(),
						DocumentCategoryErrorCode.DOCUMENT_CATEGORY_DELETE_DEPENDENCY_EXCEPTION.getErrorMessage());
			}

			int updatedRows = documentCategoryRepository.deleteDocumentCategory(LocalDateTime.now(ZoneId.of("UTC")),
					code, MetaDataUtils.getContextUser());
			if (updatedRows < 1) {

				throw new RequestException(
						DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorCode(),
						DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_DELETE_EXCEPTION.getErrorCode(),
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_DELETE_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		CodeResponseDto responseDto = new CodeResponseDto();
		responseDto.setCode(code);
		return responseDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DocumentCategoryService#
	 * getAllDocCategories(int, int, java.lang.String, java.lang.String)
	 */
	@Override
	public PageDto<DocumentCategoryExtnDto> getAllDocCategories(int pageNumber, int pageSize, String sortBy,
			String orderBy) {
		List<DocumentCategoryExtnDto> docCategories = null;
		PageDto<DocumentCategoryExtnDto> docCategoriesPages = null;
		try {
			Page<DocumentCategory> pageData = documentCategoryRepository
					.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(Direction.fromString(orderBy), sortBy)));
			if (pageData != null && pageData.getContent() != null && !pageData.getContent().isEmpty()) {
				docCategories = MapperUtils.mapAll(pageData.getContent(), DocumentCategoryExtnDto.class);
				docCategoriesPages = new PageDto<>(pageData.getNumber(), pageData.getTotalPages(),
						pageData.getTotalElements(), docCategories);
			} else {
				throw new DataNotFoundException(
						DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorCode(),
						DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_FETCH_EXCEPTION.getErrorCode(),
					e.getMessage() + ExceptionUtils.parseException(e));
		}
		return docCategoriesPages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DocumentCategoryService#
	 * searchDocCategories(io.mosip.kernel.masterdata.dto.request.SearchDto)
	 */
	@Override
	public PageResponseDto<DocumentCategoryExtnDto> searchDocCategories(SearchDto dto) {
		PageResponseDto<DocumentCategoryExtnDto> pageDto = new PageResponseDto<>();
		List<DocumentCategoryExtnDto> documentCategories = null;
		pageUtils.validateSortField(DocumentCategory.class, dto.getSort());
		if (filterTypeValidator.validate(DocumentCategoryExtnDto.class, dto.getFilters())) {
			pageUtils.validateSortField(DocumentCategory.class, dto.getSort());
			Page<DocumentCategory> page = masterDataSearchHelper.searchMasterdata(DocumentCategory.class, dto, null);
			if (page.getContent() != null && !page.getContent().isEmpty()) {
				pageDto = PageUtils.pageResponse(page);
				documentCategories = MapperUtils.mapAll(page.getContent(), DocumentCategoryExtnDto.class);
				pageDto.setData(documentCategories);
			}
		}
		return pageDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DocumentCategoryService#
	 * docCategoriesFilterValues(io.mosip.kernel.masterdata.dto.request.
	 * FilterValueDto)
	 */
	@Override
	public FilterResponseDto docCategoriesFilterValues(FilterValueDto filterValueDto) {
		FilterResponseDto filterResponseDto = new FilterResponseDto();
		List<ColumnValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), DocumentCategory.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				masterDataFilterHelper.filterValues(DocumentCategory.class, filterDto, filterValueDto)
						.forEach(filterValue -> {
							if (filterValue != null) {
								ColumnValue columnValue = new ColumnValue();
								columnValue.setFieldID(filterDto.getColumnName());
								columnValue.setFieldValue(filterValue.toString());
								columnValueList.add(columnValue);
							}
						});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		return filterResponseDto;
	}

}
