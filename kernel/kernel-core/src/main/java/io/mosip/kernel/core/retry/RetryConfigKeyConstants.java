package io.mosip.kernel.core.retry;

/**
 * Property keys that control kernel retry limits, backoff, and exception lists.
 * <p>
 * Contract: values are read from Spring Environment / mosip-config. This class
 * is not instantiable.
 * </p>
 *
 * @author Loganathan Sekar
 */
public final class RetryConfigKeyConstants {
	
	/**
	 * Maximum extra attempts after the first invocation. Property:
	 * {@code kernel.retry.attempts.limit}.
	 */
	public static final String KERNEL_RETRY_ATTEMPTS_LIMIT = "kernel.retry.attempts.limit";
	/**
	 * Initial wait in milliseconds before the first retry. Property:
	 * {@code kernel.retry.exponential.backoff.initial.interval.millisecs}.
	 */
	public static final String KERNEL_RETRY_EXPONENTIAL_BACKOFF_INITIAL_INTERVAL_MILLISECS ="kernel.retry.exponential.backoff.initial.interval.millisecs";
	/**
	 * Multiplier applied to the wait after each retry. Property:
	 * {@code kernel.retry.exponential.backoff.multiplier}.
	 */
	public static final String KERNEL_RETRY_EXPONENTIAL_BACKOFF_MULTIPLIER = "kernel.retry.exponential.backoff.multiplier";
	/**
	 * Cap on the wait between retries in milliseconds. Property:
	 * {@code kernel.retry.exponential.backoff.max.interval.millisecs}.
	 */
	public static final String KERNEL_RETRY_EXPONENTIAL_BACKOFF_MAX_INTERVAL_MILLISECS = "kernel.retry.exponential.backoff.max.interval.millisecs";
	/**
	 * When {@code true}, retry matching walks the exception cause chain. Property:
	 * {@code kernel.retry.traverse.root.cause.enabled}.
	 */
	public static final String KERNEL_RETRY_TRAVERSE_ROOT_CAUSE_ENABLED = "kernel.retry.traverse.root.cause.enabled";
	/**
	 * Comma-separated fully qualified exception class names that may be retried.
	 * Property: {@code kernel.retry.retryable.exceptions}.
	 */
	public static final String KERNEL_RETRYABLE_EXCEPTIONS = "kernel.retry.retryable.exceptions";
	/**
	 * Comma-separated fully qualified exception class names that must not be retried.
	 * Property: {@code kernel.retry.nonretryable.exceptions}.
	 */
	public static final String KERNEL_NONRETRYABLE_EXCEPTIONS = "kernel.retry.nonretryable.exceptions";

	/**
	 * Prevents instantiation of this constants holder.
	 */
	private RetryConfigKeyConstants() {
	}

}
