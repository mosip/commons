package io.mosip.kernel.core.util;

/**
 * Verhoeff checksum generation and validation for numeric MOSIP identifiers.
 * <p>
 * Contract: {@code num} must be a non-null string of digits. Validation expects
 * the check digit as the last character. Does not perform I/O.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public final class ChecksumUtils {

	/**
	 * Private constructor for IdChecksum
	 */
	private ChecksumUtils() {
	}

	/**
	 * The multiplication table.
	 */
	private static int[][] d = new int[][] { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 },
			{ 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 },
			{ 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 }, { 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 },
			{ 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

	/**
	 * The permutation table.
	 */
	private static int[][] p = new int[][] { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 },
			{ 5, 8, 0, 3, 7, 9, 6, 1, 4, 2 }, { 8, 9, 1, 6, 0, 4, 3, 5, 2, 7 }, { 9, 4, 5, 3, 1, 2, 6, 8, 7, 0 },
			{ 4, 2, 8, 6, 5, 7, 3, 9, 0, 1 }, { 2, 7, 9, 3, 8, 0, 6, 4, 1, 5 }, { 7, 0, 4, 6, 9, 1, 3, 2, 5, 8 } };

	/**
	 * The inverse table.
	 */
	private static int[] inv = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

	/**
	 * Computes the Verhoeff check digit for {@code num} without appending it.
	 *
	 * @param num never-null digit string (without check digit)
	 * @return never-null single-digit check character
	 */
	public static String generateChecksumDigit(String num) {
		int c = 0;
		int[] myArray = stringToReversedIntArray(num);
		for (int i = 0; i < myArray.length; i++) {
			c = d[c][p[((i + 1) % 8)][myArray[i]]];
		}
		return Integer.toString(inv[c]);
	}

	/**
	 * Returns whether {@code num} (including its last check digit) is Verhoeff-valid.
	 *
	 * @param num never-null digit string whose last character is the check digit
	 * @return {@code true} if the checksum is compliant
	 */
	public static boolean validateChecksum(String num) {
		int c = 0;
		int[] myArray = stringToReversedIntArray(num);
		for (int i = 0; i < myArray.length; i++) {
			c = d[c][p[(i % 8)][myArray[i]]];
		}
		return (c == 0);
	}

	/**
	 * Converts a string to a reversed integer array.
	 * 
	 * @param num The numeric string data converted to reversed int array.
	 * @return Integer array containing the digits in the numeric string provided in
	 *         reverse.
	 */
	private static int[] stringToReversedIntArray(String num) {
		int[] myArray = new int[num.length()];
		for (int i = 0; i < num.length(); i++) {
			myArray[i] = Integer.parseInt(num.substring(i, i + 1));
		}
		myArray = reverse(myArray);
		return myArray;
	}

	/**
	 * Reverses an int array.
	 * 
	 * @param myArray The input array which needs to be reversed
	 * @return The array provided in reverse order.
	 */
	private static int[] reverse(int[] myArray) {
		int[] reversed = new int[myArray.length];
		for (int i = 0; i < myArray.length; i++) {
			reversed[i] = myArray[myArray.length - (i + 1)];
		}
		return reversed;
	}
}