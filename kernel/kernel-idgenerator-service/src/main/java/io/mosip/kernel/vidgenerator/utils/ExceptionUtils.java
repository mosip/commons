package io.mosip.kernel.vidgenerator.utils;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * This class is used to get the Exception related functionalities.
 * 
 * @author Urvil Joshi
 * @author Sagar Mahapatra
 * @author Ritesh Sinha
 * 
 * @since 1.0.0
 */
public final class ExceptionUtils {
	/**
	 * Constructor for ExceptionUtils class.
	 */
	private ExceptionUtils() {
		super();
	}

	/**
	 * Method to find the root cause of the exception.
	 * 
	 * @param exception the exception.
	 * @return the root cause.
	 */
	public static String parseException(Throwable exception) {
		Optional<Throwable> rootCause = Stream.iterate(exception, Throwable::getCause)
				.filter(element -> element.getCause() == null).findFirst();
		String cause = rootCause.isPresent() ? rootCause.get().getMessage() : exception.getMessage();
		return " " + cause;
	}
}
