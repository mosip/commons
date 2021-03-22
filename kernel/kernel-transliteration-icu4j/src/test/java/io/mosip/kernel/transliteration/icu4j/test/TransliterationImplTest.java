package io.mosip.kernel.transliteration.icu4j.test;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.core.transliteration.exception.InvalidTransliterationException;
import io.mosip.kernel.core.transliteration.spi.Transliteration;

/**
 * This Unit test class contains test methods for transliteration
 * implementation.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TransliterationImplTest {

	/**
	 * 
	 * Key for arabic language.
	 */	
	private String arabicLanguageCode="ara";

	/**
	 * Key for french language.
	 */	
	private String frenchLanguageCode ="fra";

	/**
	 * Reference to {@link Transliteration}.
	 */
	@Autowired
	private Transliteration<String> transliterateImpl;

	/**
	 * This method test successfull transliteration of provided string as mention by
	 * language code.
	 */
	@Test
	public void transliterateTest() {

		String frenchToArabic = transliterateImpl.transliterate(frenchLanguageCode, arabicLanguageCode, "Bienvenue");

		assertThat(frenchToArabic, isA(String.class));

	}

	/**
	 * This method test for invalid input language code provided by user.
	 */
	@Test(expected = InvalidTransliterationException.class)
	public void transliterateInvalidInputLanguageCodeExceptionTest() {
		transliterateImpl.transliterate("dnjksd", "ara", "Bienvenue");
	}

	/**
	 * This method test for invalid to language code provided by user.
	 */
	@Test(expected = InvalidTransliterationException.class)
	public void transliterateInvalidOutputLanguageCodeExceptionTest() {
		transliterateImpl.transliterate("fra", "aradkn", "Bienvenue");
	}
}
