package io.mosip.kernel.core.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;


/**
 * The Class RetryListenerImpl - to log failed invocations.
 * @author Loganathan Sekar
 */
@Component
public class RetryListenerImpl implements RetryListener {
	/** The mosipLogger. */
	private Logger mosipLogger = LoggerFactory.getLogger(RetryListenerImpl.class);

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {
		mosipLogger.error("", this.getClass().getSimpleName(), "onError",
				throwable.getMessage() + " : " + String.valueOf(context), throwable);
	}

}
