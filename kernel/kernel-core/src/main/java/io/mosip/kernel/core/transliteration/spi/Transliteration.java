package io.mosip.kernel.core.transliteration.spi;

/**
 * Transliterates text from one MOSIP language code to another.
 * <p>
 * Contract: implementations typically call ICU4J locally (no HTTP). Language
 * codes must be non-null MOSIP lang codes; {@code text} must be non-null.
 * Invalid language ids throw
 * {@link io.mosip.kernel.core.transliteration.exception.InvalidTransliterationException}.
 * </p>
 *
 * @param <T> language-code type, typically {@link String}
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public interface Transliteration<T> {

	/**
	 * Transliterates {@code text} from {@code fromLanguage} to {@code toLanguage}.
	 *
	 * @param fromLanguage never-null source language code
	 * @param toLanguage   never-null target language code
	 * @param text         never-null source text; empty string yields empty result
	 * @return never-null transliterated text
	 * @throws io.mosip.kernel.core.transliteration.exception.InvalidTransliterationException
	 *                                                                                        when
	 *                                                                                        a
	 *                                                                                        language
	 *                                                                                        id
	 *                                                                                        is
	 *                                                                                        unsupported
	 */
	public String transliterate(T fromLanguage, T toLanguage, String text);

}
