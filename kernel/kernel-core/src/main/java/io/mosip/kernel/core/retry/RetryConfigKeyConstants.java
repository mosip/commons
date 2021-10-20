package io.mosip.kernel.core.retry;

/**
 * The Class RetryConfigKeyConstants.
 * @author Loganathan Sekar
 */
public final class RetryConfigKeyConstants {
	
	public static final String KERNEL_RETRY_ATTEMPTS_LIMIT = "kernel.retry.attempts.limit";
	public static final String KERNEL_RETRY_EXPONENTIAL_BACKOFF_INITIAL_INTERVAL_MILLISECS ="kernel.retry.exponential.backoff.initial.interval.millisecs";
	public static final String KERNEL_RETRY_EXPONENTIAL_BACKOFF_MULTIPLIER = "kernel.retry.exponential.backoff.multiplier";
	public static final String KERNEL_RETRY_EXPONENTIAL_BACKOFF_MAX_INTERVAL_MILLISECS = "kernel.retry.exponential.backoff.max.interval.millisecs";
	public static final String KERNEL_RETRY_TRAVERSE_ROOT_CAUSE_ENABLED = "kernel.retry.traverse.root.cause.enabled";
	public static final String KERNEL_RETRYABLE_EXCEPTIONS = "kernel.retry.retryable.exceptions";
	public static final String KERNEL_NONRETRYABLE_EXCEPTIONS = "kernel.retry.nonretryable.exceptions";

	/**
	 * Instantiates a new retry config key constants.
	 */
	private RetryConfigKeyConstants() {
	}

}
