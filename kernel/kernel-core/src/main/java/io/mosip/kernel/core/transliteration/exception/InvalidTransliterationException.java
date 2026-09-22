package io.mosip.kernel.core.transliteration.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when transliteration language ids are invalid.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.transliteration.spi.Transliteration}
 * when {@code fromLanguage} or {@code toLanguage} is null or unsupported.
 * </p>
 *
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public class InvalidTransliterationException extends BaseUncheckedException {

	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = -1429752953542214921L;

	/**
	 * Constructs a transliteration exception with a root cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public InvalidTransliterationException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Constructs a transliteration exception without a root cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public InvalidTransliterationException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
