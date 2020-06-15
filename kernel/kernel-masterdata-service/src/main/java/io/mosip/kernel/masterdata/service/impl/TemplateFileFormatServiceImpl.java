package io.mosip.kernel.masterdata.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.MachineErrorCode;
import io.mosip.kernel.masterdata.constant.TemplateFileFormatErrorCode;
import io.mosip.kernel.masterdata.dto.MachineDto;
import io.mosip.kernel.masterdata.dto.TemplateFileFormatDto;
import io.mosip.kernel.masterdata.dto.TemplateFileFormatResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.entity.Machine;
import io.mosip.kernel.masterdata.entity.Template;
import io.mosip.kernel.masterdata.entity.TemplateFileFormat;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.TemplateFileFormatRepository;
import io.mosip.kernel.masterdata.repository.TemplateRepository;
import io.mosip.kernel.masterdata.service.TemplateFileFormatService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MasterdataCreationUtil;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

@Service
public class TemplateFileFormatServiceImpl implements TemplateFileFormatService {

	@Autowired
	private TemplateFileFormatRepository templateFileFormatRepository;

	@Autowired
	private TemplateRepository templateRepository;
	
	
	@Autowired
	private MasterdataCreationUtil masterdataCreationUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateFileFormatService#
	 * createTemplateFileFormat(io.mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID createTemplateFileFormat(TemplateFileFormatDto templateFileFormatRequestDto) {
		TemplateFileFormat entity = MetaDataUtils.setCreateMetaData(templateFileFormatRequestDto,
				TemplateFileFormat.class);
		TemplateFileFormat templateFileFormat;
		try {
			templateFileFormat = templateFileFormatRepository.create(entity);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_INSERT_EXCEPTION.getErrorCode(),
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_INSERT_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		CodeAndLanguageCodeID codeLangCodeId = new CodeAndLanguageCodeID();
		MapperUtils.map(templateFileFormat, codeLangCodeId);
		return codeLangCodeId;
	}

	@Override
	public CodeAndLanguageCodeID updateTemplateFileFormat(TemplateFileFormatDto templateFileFormatRequestDto) {

		TemplateFileFormatDto templateFileFormatDto = templateFileFormatRequestDto;

		CodeAndLanguageCodeID templateFileFormatId = new CodeAndLanguageCodeID();

		MapperUtils.mapFieldValues(templateFileFormatDto, templateFileFormatId);

		try {
			TemplateFileFormat templateFileFormat = templateFileFormatRepository
					.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(templateFileFormatRequestDto.getCode(),
							templateFileFormatRequestDto.getLangCode());

			if (templateFileFormat != null) {
				MetaDataUtils.setUpdateMetaData(templateFileFormatDto, templateFileFormat, false);
				templateFileFormatRepository.update(templateFileFormat);
				if(!templateFileFormatRequestDto.getIsActive()) {
					masterdataCreationUtil.updateMasterDataDeactivate(TemplateFileFormat.class, templateFileFormatRequestDto.getCode());
				}
			} else {

				throw new RequestException(TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_NOT_FOUND.getErrorCode(),

						TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_NOT_FOUND.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_UPDATE_EXCEPTION.getErrorCode(),
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_UPDATE_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		return templateFileFormatId;
	}

	@Override
	public CodeResponseDto deleteTemplateFileFormat(String code) {
		List<Template> templates;
		try {
			templates = templateRepository.findAllByFileFormatCodeAndIsDeletedFalseOrIsDeletedIsNull(code);
			if (!templates.isEmpty()) {
				throw new MasterDataServiceException(
						TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_DELETE_DEPENDENCY_EXCEPTION.getErrorCode(),
						TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_DELETE_DEPENDENCY_EXCEPTION.getErrorMessage());
			}
			int updatedRows = templateFileFormatRepository.deleteTemplateFileFormat(MetaDataUtils.getContextUser(),
					LocalDateTime.now(ZoneId.of("UTC")), code);
			if (updatedRows < 1) {

				throw new RequestException(TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_NOT_FOUND.getErrorCode(),

						TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_NOT_FOUND.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException e) {
			System.out.println(e.getMessage());
			throw new MasterDataServiceException(
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_DELETE_EXCEPTION.getErrorCode(),
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_DELETE_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		CodeResponseDto responseDto = new CodeResponseDto();
		responseDto.setCode(code);
		return responseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.MachineService#getMachine(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public TemplateFileFormatResponseDto getTemplateFileFormatCodeandLangCode(String templateFileFormatCode,
			String langCode) {
		List<TemplateFileFormat> templateFileFormatList = null;
		List<TemplateFileFormatDto> templateFileFormatDtoList = null;
		TemplateFileFormatResponseDto templateFileFormatResponseDto = new TemplateFileFormatResponseDto();
		try {
			templateFileFormatList = templateFileFormatRepository
					.findAllByCodeAndLangCodeAndIsDeletedFalseorIsDeletedIsNull(templateFileFormatCode, langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_FETCH_EXCEPTION.getErrorCode(),
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (templateFileFormatList != null && !templateFileFormatList.isEmpty()) {
			templateFileFormatDtoList = MapperUtils.mapAll(templateFileFormatList, TemplateFileFormatDto.class);
		} else {

			throw new DataNotFoundException(TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_NOT_FOUND.getErrorCode(),
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_NOT_FOUND.getErrorMessage());

		}
		templateFileFormatResponseDto.setTemplateFileFormats(templateFileFormatDtoList);
		return templateFileFormatResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.MachineService#getMachine(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public TemplateFileFormatResponseDto getTemplateFileFormatLangCode(String langCode) {
		List<TemplateFileFormat> templateFileFormatList = null;
		List<TemplateFileFormatDto> templateFileFormatDtoList = null;
		TemplateFileFormatResponseDto templateFileFormatResponseDto = new TemplateFileFormatResponseDto();
		try {
			templateFileFormatList = templateFileFormatRepository
					.findAllByLangCodeAndIsDeletedFalseorIsDeletedIsNull(langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_FETCH_EXCEPTION.getErrorCode(),
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (templateFileFormatList != null && !templateFileFormatList.isEmpty()) {
			templateFileFormatDtoList = MapperUtils.mapAll(templateFileFormatList, TemplateFileFormatDto.class);
		} else {

			throw new DataNotFoundException(TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_NOT_FOUND.getErrorCode(),
					TemplateFileFormatErrorCode.TEMPLATE_FILE_FORMAT_NOT_FOUND.getErrorMessage());

		}
		templateFileFormatResponseDto.setTemplateFileFormats(templateFileFormatDtoList);
		return templateFileFormatResponseDto;
	}
}
