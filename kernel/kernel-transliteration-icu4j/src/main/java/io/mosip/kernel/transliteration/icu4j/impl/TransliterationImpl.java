package io.mosip.kernel.transliteration.icu4j.impl;

import org.springframework.stereotype.Component;

import com.ibm.icu.text.Transliterator;

import io.mosip.kernel.core.transliteration.exception.InvalidTransliterationException;
import io.mosip.kernel.core.transliteration.spi.Transliteration;
import io.mosip.kernel.transliteration.icu4j.constant.TransliterationErrorConstant;
import io.mosip.kernel.transliteration.icu4j.constant.TransliterationPropertyConstant;

/**
 * This class perform transliteration of text based on language code mention.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@Component
public class TransliterationImpl implements Transliteration<String> {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.transliteration.spi.Transliteration#transliterate(java.
	 * lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public String transliterate(String fromLanguageCode, String toLanguageCode, String text) {
		Transliterator translitratedLanguage;
		String languageId = fromLanguageCode
				+ TransliterationPropertyConstant.TRANSLITERATION_ID_SEPARATOR.getProperty() + toLanguageCode;
		try {
			translitratedLanguage = Transliterator.getInstance(languageId);

		} catch (IllegalArgumentException e) {
			throw new InvalidTransliterationException(
					TransliterationErrorConstant.TRANSLITERATION_INVALID_ID.getErrorCode(),
					TransliterationErrorConstant.TRANSLITERATION_INVALID_ID.getErrorMessage(), e);
		}

		return translitratedLanguage.transliterate(text);
	}
}
