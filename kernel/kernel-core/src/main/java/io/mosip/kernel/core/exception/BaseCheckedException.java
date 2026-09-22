package io.mosip.kernel.core.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclass of all MOSIP checked exceptions that carry an error code and
 * message chain.
 * <p>
 * Contract: callers typically construct with a MOSIP {@code errorCode} and
 * {@code errorMessage}. {@link #addInfo(String, String)} appends nested error
 * items. {@link #getErrorCode()} and {@link #getErrorText()} return the
 * first-added item and throw if none were added. Does not perform I/O.
 * </p>
 *
 * @author Shashank Agrawal
 * @since 1.0
 * @see InfoItem
 * @see ExceptionUtils
 */
public class BaseCheckedException extends Exception {

	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = -924722202100630614L;

	/**
	 * Ordered error-code / error-text pairs accumulated on this exception.
	 */
	private final List<InfoItem> infoItems = new ArrayList<>();

	/**
	 * Constructs a new checked exception with no detail or error items.
	 */
	public BaseCheckedException() {
		super();
	}

	/**
	 * Constructs a new checked exception with a detail message only.
	 * <p>
	 * Contract: does not populate {@link #infoItems}; {@link #getErrorCode()} is
	 * unsafe until {@link #addInfo(String, String)} is called.
	 * </p>
	 *
	 * @param errorMessage the detail message; may be null
	 */
	public BaseCheckedException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * Constructs a new checked exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code such as {@code KER-UTL-001}
	 * @param errorMessage never-null human-readable description; may be empty
	 */
	public BaseCheckedException(String errorCode, String errorMessage) {
		super(errorCode + " --> " + errorMessage);
		addInfo(errorCode, errorMessage);
	}

	/**
	 * Constructs a new checked exception with MOSIP error code, message, and
	 * cause. Nested {@link BaseCheckedException} info items are copied.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public BaseCheckedException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode + " --> " + errorMessage, rootCause);
		addInfo(errorCode, errorMessage);
		if (rootCause instanceof BaseCheckedException) {
			BaseCheckedException bce = (BaseCheckedException) rootCause;
			infoItems.addAll(bce.infoItems);
		}
	}

	/**
	 * Appends an error-code / error-text pair to this exception.
	 *
	 * @param errorCode never-null MOSIP error code
	 * @param errorText never-null detail message; may be empty
	 * @return this instance for chaining; never null
	 */
	public BaseCheckedException addInfo(String errorCode, String errorText) {
		this.infoItems.add(new InfoItem(errorCode, errorText));
		return this;
	}

	/**
	 * Returns the detail message, appending the nested cause when present.
	 *
	 * @return composed message; may be null if neither message nor cause exists
	 * @see ExceptionUtils#buildMessage(String, Throwable)
	 */
	@Override
	public String getMessage() {
		return ExceptionUtils.buildMessage(super.getMessage(), getCause());
	}

	/**
	 * Returns error codes in reverse insertion order (most recently added first).
	 *
	 * @return never-null new list; empty if no info items were added
	 */
	public List<String> getCodes() {
		List<String> codes = new ArrayList<>();
		for (int i = this.infoItems.size() - 1; i >= 0; i--)
			codes.add(this.infoItems.get(i).errorCode);
		return codes;
	}

	/**
	 * Returns error texts in reverse insertion order (most recently added first).
	 *
	 * @return never-null new list; empty if no info items were added
	 */
	public List<String> getErrorTexts() {
		List<String> errorTexts = new ArrayList<>();
		for (int i = this.infoItems.size() - 1; i >= 0; i--)
			errorTexts.add(this.infoItems.get(i).errorText);
		return errorTexts;
	}

	/**
	 * Returns the first-added error code (the original constructor code).
	 *
	 * @return first error code; never null if this exception was constructed with
	 *         a code
	 * @throws java.lang.IndexOutOfBoundsException if no info items exist
	 */
	public String getErrorCode() {
		return infoItems.get(0).errorCode;
	}

	/**
	 * Returns the first-added error text.
	 *
	 * @return first error text; never null if this exception was constructed with
	 *         a message
	 * @throws java.lang.IndexOutOfBoundsException if no info items exist
	 */
	public String getErrorText() {
		return infoItems.get(0).errorText;
	}

}
