/**
 * CBEFF processing exceptions.
 */
package io.mosip.kernel.core.cbeffutil.exception;

/**
 * Checked exception thrown when CBEFF XML or ISO image processing fails.
 * <p>
 * Contract: raised for format-identifier mismatches, unreadable ISO files, or
 * invalid CBEFF structure. Callers should treat this as a data-format error.
 * </p>
 *
 * @author Ramadurai Pandian
 */
public class CbeffException extends Exception {

	private static final long serialVersionUID = 9190616446912282298L;

	/**
	 * Constructs a CBEFF exception with a detail message.
	 *
	 * @param message never-null human-readable description; may be empty
	 */
	public CbeffException(String message) {
		super(message);
	}

}
