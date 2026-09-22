package io.mosip.kernel.core.bioapi.model;

import lombok.Data;

/**
 * String key/value pair used as a Bio API flag or analytics entry.
 * <p>
 * Contract: {@code key} should be non-blank when present in a flags array.
 * {@code value} may be null. Does not perform I/O.
 * </p>
 *
 * @author Sanjay Murali
 */
@Data
public class KeyValuePair {
	/**
	 * Flag or analytics name; should be non-blank when used as input.
	 */
	private String key;
	/**
	 * Associated value; may be null.
	 */
	private String value;
}
