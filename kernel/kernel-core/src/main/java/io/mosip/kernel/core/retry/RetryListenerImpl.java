package io.mosip.kernel.core.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;


/**
 * Logs each failed retry attempt for {@link RetryTemplate} invocations.
 * <p>
 * Contract: registered as a Spring {@link RetryListener}. {@link #open} always
 * returns {@code true} so retries proceed. Does not swallow the failure.
 * </p>
 *
 * @author Loganathan Sekar
 */
@Component
public class RetryListenerImpl implements RetryListener {
	/** Logger used to record failed retry attempts. */
	private Logger mosipLogger = LoggerFactory.getLogger(RetryListenerImpl.class);

	/**
	 * Called before the first attempt; always allows the retry sequence.
	 *
	 * @param <T>      callback result type
	 * @param <E>      exception type thrown by the callback
	 * @param context  never-null retry context
	 * @param callback never-null operation being retried
	 * @return {@code true} so the retry proceeds
	 */
	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		return true;
	}

	/**
	 * Called after the last attempt; this implementation is a no-op.
	 *
	 * @param <T>       callback result type
	 * @param <E>       exception type thrown by the callback
	 * @param context   never-null retry context
	 * @param callback  never-null operation being retried
	 * @param throwable last failure; null if the last attempt succeeded
	 */
	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {
	}

	/**
	 * Logs the failed attempt and current retry context.
	 *
	 * @param <T>       callback result type
	 * @param <E>       exception type thrown by the callback
	 * @param context   never-null retry context
	 * @param callback  never-null operation being retried
	 * @param throwable never-null failure from this attempt
	 */
	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {
		mosipLogger.error("", this.getClass().getSimpleName(), "onError",
				throwable.getMessage() + " : " + String.valueOf(context), throwable);
	}

}
