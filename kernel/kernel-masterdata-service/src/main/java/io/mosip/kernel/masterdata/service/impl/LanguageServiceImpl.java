package io.mosip.kernel.masterdata.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.masterdata.constant.LanguageErrorCode;
import io.mosip.kernel.masterdata.dto.LanguageDto;
import io.mosip.kernel.masterdata.dto.getresponse.LanguageResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.entity.Language;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.LanguageRepository;
import io.mosip.kernel.masterdata.service.LanguageService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 *
 * @author Bal Vikash Sharma
 */
@Service
public class LanguageServiceImpl implements LanguageService {

	/**
	 * Repository used for CRUD operation.
	 */
	@Autowired
	private LanguageRepository languageRepository;

	/**
	 * (non-Javadoc)
	 * 
	 * @see LanguageService#getAllLaguages()
	 */
	@Override
	public LanguageResponseDto getAllLaguages() {
		LanguageResponseDto languageResponseDto = new LanguageResponseDto();
		List<LanguageDto> languageDtos = null;
		List<Language> languages = null;

		try {
			languages = languageRepository.findAllByIsDeletedFalseOrIsDeletedIsNull();
		} catch (DataAccessException | DataAccessLayerException dataAccessException) {
			throw new MasterDataServiceException(LanguageErrorCode.LANGUAGE_FETCH_EXCEPTION.getErrorCode(),
					LanguageErrorCode.LANGUAGE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(dataAccessException),
					dataAccessException);
		}

		if (languages != null && !languages.isEmpty()) {
			languageDtos = MapperUtils.mapAll(languages, LanguageDto.class);
		} else {
			throw new DataNotFoundException(LanguageErrorCode.NO_LANGUAGE_FOUND_EXCEPTION.getErrorCode(),
					LanguageErrorCode.NO_LANGUAGE_FOUND_EXCEPTION.getErrorMessage());
		}

		languageResponseDto.setLanguages(languageDtos);
		return languageResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LanguageService#saveLanguage(io.mosip.
	 * kernel.masterdata.dto.LanguageDto)
	 */
	public CodeResponseDto saveLanguage(LanguageDto requestDto) {

		try {
			Language language = MetaDataUtils.setCreateMetaData(requestDto, Language.class);
			Language savedLanguage = languageRepository.create(language);
			return MapperUtils.map(savedLanguage, CodeResponseDto.class);
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(LanguageErrorCode.LANGUAGE_CREATE_EXCEPTION.getErrorCode(),
					LanguageErrorCode.LANGUAGE_CREATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LanguageService#updateLanguage(io.mosip.
	 * kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeResponseDto updateLanguage(LanguageDto languageDto) {
		CodeResponseDto code = new CodeResponseDto();
		try {
			Language language = languageRepository.findLanguageByCode(languageDto.getCode());
			if (!EmptyCheckUtils.isNullEmpty(language)) {
				MetaDataUtils.setUpdateMetaData(languageDto, language, false);
				languageRepository.update(language);
				code.setCode(language.getCode());
			} else {
				throw new RequestException(LanguageErrorCode.NO_LANGUAGE_FOUND_EXCEPTION.getErrorCode(),
						LanguageErrorCode.NO_LANGUAGE_FOUND_EXCEPTION.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(LanguageErrorCode.LANGUAGE_UPDATE_EXCEPTION.getErrorCode(),
					LanguageErrorCode.LANGUAGE_UPDATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		return code;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.LanguageService#deleteLanguage(java.lang.
	 * String)
	 */
	@Override
	public CodeResponseDto deleteLanguage(String code) {
		CodeResponseDto response = new CodeResponseDto();
		try {
			Language language = languageRepository.findLanguageByCode(code);
			if (!EmptyCheckUtils.isNullEmpty(language)) {
				MetaDataUtils.setDeleteMetaData(language);
				languageRepository.update(language);
				response.setCode(language.getCode());
			} else {
				throw new RequestException(LanguageErrorCode.NO_LANGUAGE_FOUND_EXCEPTION.getErrorCode(),
						LanguageErrorCode.NO_LANGUAGE_FOUND_EXCEPTION.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(LanguageErrorCode.LANGUAGE_DELETE_EXCEPTION.getErrorCode(),
					LanguageErrorCode.LANGUAGE_DELETE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}

		return response;
	}

}
