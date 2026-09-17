package io.mosip.kernel.transliteration.icu4j.impl;

import org.springframework.stereotype.Component;

import com.ibm.icu.text.Transliterator;

import io.mosip.kernel.core.transliteration.exception.InvalidTransliterationException;
import io.mosip.kernel.core.transliteration.spi.Transliteration;
import io.mosip.kernel.transliteration.icu4j.constant.TransliterationErrorConstant;
import io.mosip.kernel.transliteration.icu4j.constant.TransliterationPropertyConstant;

/**
 * ICU4J {@link Transliteration} that converts text from one language id to another.
 * <p>
 * Builds an ICU id as {@code fromLanguageCode-toLanguageCode}. Unknown ids raise
 * {@link InvalidTransliterationException}.
 * </p>
 *
 * @author Ritesh Sinha
 * @since 1.0.0
 * @see Transliteration
 */
@Component
public class TransliterationImpl implements Transliteration<String> {
	/**
	 * Transliterates {@code text} from {@code fromLanguageCode} to {@code toLanguageCode}.
	 *
	 * @param fromLanguageCode ICU source language / script id
	 * @param toLanguageCode   ICU target language / script id
	 * @param text             text to convert
	 * @return transliterated text
	 * @throws InvalidTransliterationException if ICU has no transliterator for the composed id
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
