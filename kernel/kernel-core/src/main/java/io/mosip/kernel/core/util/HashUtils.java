package io.mosip.kernel.core.util;

import java.security.MessageDigest;

import io.mosip.kernel.core.util.constant.HashUtilConstants;
import io.mosip.kernel.core.util.exception.HashUtilException;

/**
 * Incremental hash-code builder modelled on Apache Commons HashCodeBuilder.
 * <p>
 * Contract: {@link #append} methods accept null arrays by multiplying the
 * running total without adding an element. {@link #HashUtils(long, long)}
 * requires odd initial and multiplier values. Does not perform I/O.
 * </p>
 *
 * @version 1.0 10 August 2018
 * @author Jyoti Prakash Nayak
 */
public class HashUtils {
	/**
	 * Constant to use in building the hashCode.
	 */
	private final long multiplierConstant;

	/**
	 * Running total of the hashCode.
	 */
	private long total = 0;

	/**
	 * Constructs a builder with default odd initial value 7919 and multiplier 7664345821815920749.
	 */
	public HashUtils() {
		multiplierConstant = 7664345821815920749l;
		total = 7919l;
	}

	/**
	 * Constructs a builder with caller-supplied odd initial and multiplier values.
	 *
	 * @param initialOddNumber    odd starting total
	 * @param multiplierOddNumber odd multiplier applied before each append
	 * @throws HashUtilException if either number is even
	 */
	public HashUtils(final long initialOddNumber, final long multiplierOddNumber) throws HashUtilException {
		if (initialOddNumber % 2 == 0) {
			throw new HashUtilException(
					HashUtilConstants.MOSIP_ILLEGAL_ARGUMENT_INITIALODDNUMBER_ERROR_CODE.getErrorCode(),
					HashUtilConstants.MOSIP_ILLEGAL_ARGUMENT_INITIALODDNUMBER_ERROR_CODE.getErrorMessage());
		} else if (multiplierOddNumber % 2 == 0) {
			throw new HashUtilException(
					HashUtilConstants.MOSIP_ILLEGAL_ARGUMENT_MULTIPLIERODDNUMBER_ERROR_CODE.getErrorCode(),
					HashUtilConstants.MOSIP_ILLEGAL_ARGUMENT_MULTIPLIERODDNUMBER_ERROR_CODE.getErrorMessage());
		} else {
			multiplierConstant = multiplierOddNumber;
			total = initialOddNumber;
		}

	}

	/**
	 * Append a hashCode for a boolean. This adds 1 when true, and 0 when false to
	 * the hashCode. This is in contrast to the standard java.lang.Boolean.hashCode
	 * handling, which computes a hashCode value of 1231 for java.lang.Boolean
	 * instances that represent true or 1237 for java.lang.Boolean instances that
	 * represent false. This is in accordance with the Effective Java design.
	 * 
	 * @param value the boolean to add to the hashCode
	 * @return this
	 */
	public HashUtils append(boolean value) {
		total = total * multiplierConstant + (value ? 0l : 1l);
		return this;
	}

	/**
	 * Append a hashCode for a boolean array.
	 * 
	 * @param array the array to add to the hashCode
	 * @return this
	 */
	public HashUtils append(boolean[] array) {
		if (array == null) {
			total = total * multiplierConstant;
		} else {
			for (final boolean element : array) {
				append(element);
			}
		}
		return this;
	}

	/**
	 * Append a hashCode for a byte.
	 * 
	 * @param value the byte to add to the hashcode
	 * @return this
	 */
	public HashUtils append(byte value) {
		total = total * multiplierConstant + (long) value;
		return this;
	}

	/**
	 * Append a hashcode for a byte array
	 * 
	 * @param array array to add to the hashcode
	 * @return this
	 */
	public HashUtils append(byte[] array) {
		if (array == null) {
			total = total * multiplierConstant;
		} else {
			for (final byte element : array) {
				append(element);
			}
		}
		return this;
	}

	/**
	 * Append a hashcode for a character
	 * 
	 * @param value the character to add to the hashcode
	 * @return this
	 */
	public HashUtils append(char value) {
		total = total * multiplierConstant + (long) value;
		return this;
	}

	/**
	 * Append a hashcode for a character array
	 * 
	 * @param array array to add to the hashcode
	 * @return this
	 */
	public HashUtils append(char[] array) {
		if (array == null) {
			total = total * multiplierConstant;
		} else {
			for (final char element : array) {
				append(element);
			}
		}
		return this;
	}

	/**
	 * Append a hashcode for a double
	 * 
	 * @param value double to add to the hash code
	 * @return this
	 */
	public HashUtils append(double value) {
		return append(Double.doubleToLongBits(value));
	}

	/**
	 * Append a hashcode for a double array
	 * 
	 * @param array array to add to the hash code
	 * @return this
	 */
	public HashUtils append(double[] array) {
		if (array == null) {
			total = total * multiplierConstant;
		} else {
			for (final double element : array) {
				append(element);
			}
		}
		return this;
	}

	/**
	 * Append a hashcode for a float
	 * 
	 * @param value float to add to the hash code
	 * @return this
	 */
	public HashUtils append(float value) {
		total = total * multiplierConstant + (long) Float.floatToIntBits(value);
		return this;
	}

	/**
	 * Append a hashcode for a float array
	 * 
	 * @param array array to add to the hash code
	 * @return this
	 */
	public HashUtils append(float[] array) {
		if (array == null) {
			total = total * multiplierConstant;
		} else {
			for (final float element : array) {
				append(element);
			}
		}
		return this;
	}

	/**
	 * Append a hashcode for a integer
	 * 
	 * @param value integer to add to the hash code
	 * @return this
	 */
	public HashUtils append(int value) {
		total = total * multiplierConstant + (long) value;
		return this;
	}

	/**
	 * Append a hashcode for a integer array
	 * 
	 * @param array array to add to the hash code
	 * @return this
	 */
	public HashUtils append(int[] array) {
		if (array == null) {
			total = total * multiplierConstant;
		} else {
			for (final int element : array) {
				append(element);
			}
		}
		return this;
	}

	/**
	 * Append a hashcode for a long
	 * 
	 * @param value long to add to the hash code
	 * @return this
	 */
	public HashUtils append(long value) {
		total = total * multiplierConstant + value;
		return this;

	}

	/**
	 * Append a hashcode for a long array
	 * 
	 * @param array array to add to the hashcode
	 * @return this
	 */
	public HashUtils append(long[] array) {
		if (array == null) {
			total = total * multiplierConstant;
		} else {
			for (final long element : array) {
				append(element);
			}
		}
		return this;
	}

	/**
	 * Append a hashcode for a object
	 * 
	 * @param value object to add to the hashcode
	 * @return this
	 */
	public HashUtils append(Object value) {
		if (value == null) {
			total = total * multiplierConstant;

		} else {
			if (value.getClass().isArray()) {

				appendArray(value);
			} else if (value instanceof String) {
				char[] chr = ((String) value).toCharArray();
				append(chr);
			} else {
				total = total * multiplierConstant + (long) value.hashCode();
			}
		}
		return this;
	}

	/**
	 * 
	 * Append a <code>hashCode</code> for an array.
	 * 
	 * @param object the array to add to the <code>hashCode</code>
	 */
	private void appendArray(final Object object) {
		// 'Switch' on type of array, to dispatch to the correct handler
		// This handles multi dimensional arrays
		if (object instanceof long[]) {
			append((long[]) object);
		} else if (object instanceof int[]) {
			append((int[]) object);
		} else if (object instanceof short[]) {
			append((short[]) object);
		} else if (object instanceof char[]) {
			append((char[]) object);
		} else if (object instanceof byte[]) {
			append((byte[]) object);
		} else if (object instanceof double[]) {
			append((double[]) object);
		} else if (object instanceof float[]) {
			append((float[]) object);
		} else if (object instanceof boolean[]) {
			append((boolean[]) object);
		} else {
			// Not an array of primitives
			append((Object[]) object);
		}
	}

	/**
	 * Append a hashcode for a object array
	 * 
	 * @param array array to add to the hashcode
	 * @return this
	 */
	public HashUtils append(Object[] array) {
		if (array == null) {
			total = total * multiplierConstant;
		} else {
			for (final Object element : array) {
				append(element);
			}
		}
		return this;
	}

	/**
	 * Append a hashcode for a short
	 * 
	 * @param value value to add to the hashcode
	 * @return this
	 */
	public HashUtils append(short value) {
		total = total * multiplierConstant + (long) value;
		return this;
	}

	/**
	 * Append a hashcode for a short array
	 * 
	 * @param array array to add to the hashcode
	 * @return this
	 */
	public HashUtils append(short[] array) {
		if (array == null) {
			total = total * multiplierConstant;
		} else {
			for (final short element : array) {
				append(element);
			}
		}
		return this;
	}

	/**
	 * Returns the computed hashCode.
	 * 
	 * @return integer hashcode based on the fields appended
	 */
	public long build() {
		return Long.valueOf(toHashCode());
	}

	/**
	 * Return the computed hashCode.
	 * 
	 * @return integer hashcode based on the fields appended
	 */
	public long toHashCode() {
		return total;
	}

	/**
	 * Constant-time comparison of two digests.
	 *
	 * @param source never-null first digest
	 * @param target never-null second digest
	 * @return {@code true} if the arrays are equal
	 */
	public boolean isDigestEqual(byte[] source, byte[] target) {
		return MessageDigest.isEqual(source, target);
	}

}