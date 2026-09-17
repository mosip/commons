package io.mosip.kernel.core.util;

import java.util.Collection;
import java.util.Map;

/**
 * Null and empty checks that avoid {@link NullPointerException} on common types.
 * <p>
 * Contract: static helpers only; this class is not instantiable. A trimmed
 * blank string is treated as empty. Does not perform I/O.
 * </p>
 *
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
public final class EmptyCheckUtils {

	/**
	 * Prevents instantiation of this utility.
	 */
	private EmptyCheckUtils() {
		super();
	}

	/**
	 * Returns whether {@code obj} is null.
	 *
	 * @param obj any reference; may be null
	 * @return {@code true} if {@code obj} is null
	 */
	public static boolean isNullEmpty(Object obj) {
		return obj == null;
	}

	/**
	 * Returns whether {@code str} is null or blank after trim.
	 *
	 * @param str string to test; may be null
	 * @return {@code true} if null or {@code trim().length() == 0}
	 */
	public static boolean isNullEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}

	/**
	 * Returns whether {@code collection} is null or contains no elements.
	 *
	 * @param collection collection to test; may be null
	 * @return {@code true} if null or empty
	 */
	public static boolean isNullEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * Returns whether {@code array} is null or has length zero.
	 *
	 * @param array byte array to test; may be null
	 * @return {@code true} if null or empty
	 */
	public static boolean isNullEmpty(byte[] array) {
		return array == null || array.length == 0;
	}

	/**
	 * Returns whether {@code map} is null or contains no entries.
	 *
	 * @param map map to test; may be null
	 * @return {@code true} if null or empty
	 */
	public static boolean isNullEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

}
