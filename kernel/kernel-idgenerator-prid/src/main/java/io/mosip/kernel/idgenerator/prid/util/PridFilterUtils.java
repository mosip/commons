/**
 * 
 */
package io.mosip.kernel.idgenerator.prid.util;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Dharmesh Khandelwal
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Component
public class PridFilterUtils {

	/**
	 * List of restricted numbers
	 */
	@Value("#{'${mosip.kernel.prid.restricted-numbers}'.split(',')}")
	private List<String> restrictedAdminDigits;

	/**
	 * Upper bound of number of digits in sequence allowed in id. For example if
	 * limit is 3, then 12 is allowed but 123 is not allowed in id (in both
	 * ascending and descending order)
	 */
	@Value("${mosip.kernel.prid.sequence-limit}")
	private int sequenceLimit;

	/**
	 * Number of digits in repeating block allowed in id. For example if limit is 2,
	 * then 4xxx4 is allowed but 48xxx48 is not allowed in id (x is any digit)
	 */
	@Value("${mosip.kernel.prid.repeating-block-limit}")
	private int repeatingBlockLimit;

	/**
	 * Lower bound of number of digits allowed in between two repeating digits in
	 * id. For example if limit is 2, then 11 and 1x1 is not allowed in id (x is any
	 * digit)
	 */
	@Value("${mosip.kernel.prid.repeating-limit}")
	private int repeatingLimit;

	@Value("#{'${mosip.kernel.prid.not-start-with}'.split(',')}")
	private List<String> notStartWith;

	@Value("${mosip.kernel.prid.length}")
	private int pridLength;

	/**
	 * Ascending digits which will be checked for sequence in id
	 */
	private static final String SEQ_ASC = "0123456789";

	/**
	 * Descending digits which will be checked for sequence in id
	 */
	private static final String SEQ_DEC = "9876543210";

	/**
	 * Compiled regex pattern of {@link #repeatingRegEx}
	 */
	private Pattern repeatingPattern = null;

	/**
	 * Compiled regex pattern of {@link #repeatingBlockRegex}
	 */
	private Pattern repeatingBlockpattern = null;

	@PostConstruct
	public void initializeRegEx() {

		/**
		 * Regex for matching repeating digits like 11, 1x1, 1xx1, 1xxx1, etc.<br/>
		 * If repeating digit limit is 2, then <b>Regex:</b> (\d)\d{0,2}\1<br/>
		 * <b>Explanation:</b><br/>
		 * <b>1st Capturing Group (\d)</b><br/>
		 * <b>\d</b> matches a digit (equal to [0-9])<br/>
		 * <b>{0,2} Quantifier</b> — Matches between 0 and 2 times, as many times as
		 * possible, giving back as needed (greedy)<br/>
		 * <b>\1</b> matches the same text as most recently matched by the 1st capturing
		 * group<br/>
		 */
		if (repeatingLimit > 0) {
			String repeatingRegEx = "(\\d)\\d{0," + (repeatingLimit - 1) + "}\\1";
			/**
			 * Compiled regex pattern of {@link #repeatingRegEx}
			 */
			repeatingPattern = Pattern.compile(repeatingRegEx);
		}

		/**
		 * Regex for matching repeating block of digits like 482xx482, 4827xx4827 (x is
		 * any digit).<br/>
		 * If repeating block limit is 3, then <b>Regex:</b> (\d{3,}).*?\1<br/>
		 * <b>Explanation:</b><br/>
		 * <b>1st Capturing Group (\d{3,})</b><br/>
		 * <b>\d{3,}</b> matches a digit (equal to [0-9])<br/>
		 * <b>{3,}</b> Quantifier — Matches between 3 and unlimited times, as many times
		 * as possible, giving back as needed (greedy)<br/>
		 * <b>.*?</b> matches any character (except for line terminators)<br/>
		 * <b>*?</b> Quantifier — Matches between zero and unlimited times, as few times
		 * as possible, expanding as needed (lazy)<br/>
		 * <b>\1</b> matches the same text as most recently matched by the 1st capturing
		 * group<br/>
		 */

		if (repeatingBlockLimit > 0) {
			String repeatingBlockRegex = "(\\d{" + repeatingBlockLimit + ",}).*?\\1";

			/**
			 * Compiled regex pattern of {@link #repeatingBlockRegex}
			 */
			repeatingBlockpattern = Pattern.compile(repeatingBlockRegex);
		}
	}

	/**
	 * Checks if the input id is valid by passing the id through
	 * {@link #sequenceLimit} filter, {@link #repeatingLimit} filter and
	 * {@link #repeatingBlockLimit} filters
	 * 
	 * @param id The input id to validate
	 * @return true if the input id is valid
	 */
	public boolean isValidId(String id) {

		return !(sequenceFilter(id) || regexFilter(id, repeatingPattern) || regexFilter(id, repeatingBlockpattern)
				|| validateNotStartWith(id) || validateIdLength(id) || restrictedAdminFilter(id));
	}

	/**
	 * Checks the input id for {@link #restrictedNumbers} filter
	 * 
	 * @param id The input id to validate
	 * @return true if the id matches the filter
	 */
	private boolean restrictedAdminFilter(String id) {
		return restrictedAdminDigits.parallelStream().anyMatch(id::contains);
	}

	/**
	 * Checks the input id for {@link #sequenceLimit} filter
	 * 
	 * @param id The input id to validate
	 * @return true if the id matches the filter
	 */
	private boolean sequenceFilter(String id) {
		if (sequenceLimit > 0) {
			return IntStream.rangeClosed(0, id.length() - sequenceLimit).parallel()
					.mapToObj(index -> id.subSequence(index, index + sequenceLimit))
					.anyMatch(idSubSequence -> SEQ_ASC.contains(idSubSequence) || SEQ_DEC.contains(idSubSequence));
		}
		return false;
	}

	/**
	 * Checks the input id if it matched the given regex pattern
	 * ({@link #REPEATING_PATTERN}, {@link #repeatingBlockpattern})
	 * 
	 * @param id      The input id to validate
	 * @param pattern The input regex Pattern
	 * @return true if the id matches the given regex pattern
	 */
	private static boolean regexFilter(String id, Pattern pattern) {
		if (pattern != null)
			return pattern.matcher(id).find();
		return false;
	}

	/**
	 * Method to validate that the prid should not contains the specified digit at
	 * first index
	 * 
	 * @param id The input id to validate
	 * @return true if found otherwise false
	 */
	private boolean validateNotStartWith(String id) {
		if (notStartWith != null && !notStartWith.isEmpty()) {
			for (String str : notStartWith) {
				if (id.startsWith(str))
					return true;
			}
		}
		return false;
	}

	private boolean validateIdLength(String id) {
		return (id.length() != pridLength);
	}

}
