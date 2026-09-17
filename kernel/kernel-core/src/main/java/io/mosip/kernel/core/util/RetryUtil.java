package io.mosip.kernel.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.function.ConsumerWithThrowable;
import io.mosip.kernel.core.function.FunctionWithThrowable;
import io.mosip.kernel.core.function.RunnableWithThrowable;
import io.mosip.kernel.core.function.SupplierWithThrowable;

/**
 * Executes lambdas through the kernel {@link RetryTemplate}.
 * <p>
 * Contract: retry limits and exception lists come from
 * {@link io.mosip.kernel.core.retry.RetryConfig}. The last exception is
 * rethrown when retries are exhausted. Does not perform MOSIP HTTP itself.
 * </p>
 */
@Component
public class RetryUtil {
	
	/** Spring Retry template configured by {@link io.mosip.kernel.core.retry.RetryConfig}. */
	@Autowired
	private RetryTemplate retryTemplate;
	
	/**
	 * Retries {@code func.apply(t)} until it succeeds or the policy is exhausted.
	 *
	 * @param <R>  result type
	 * @param <T>  argument type
	 * @param <E>  throwable type declared by {@code func}
	 * @param func never-null function to invoke
	 * @param t    argument passed to {@code func}; may be null
	 * @return result of {@code func}; may be null
	 * @throws E the last failure after retries are exhausted
	 */
	public <R, T, E extends Throwable> R doWithRetry(FunctionWithThrowable<R, T, E> func, T t) throws E {
		return doProcessWithRetry(func, t);
	}
	
	/**
	 * Retries {@code func.get()} until it succeeds or the policy is exhausted.
	 *
	 * @param <R>  result type
	 * @param <E>  throwable type declared by {@code func}
	 * @param func never-null supplier to invoke
	 * @return result of {@code func}; may be null
	 * @throws E the last failure after retries are exhausted
	 */
	public <R, E extends Throwable> R doWithRetry(SupplierWithThrowable<R, E> func) throws E {
		return doProcessWithRetry(t -> func.get(), null);
	}
	
	/**
	 * Retries {@code func.accept(t)} until it succeeds or the policy is exhausted.
	 *
	 * @param <T>  argument type
	 * @param <E>  throwable type declared by {@code func}
	 * @param func never-null consumer to invoke
	 * @param t    argument passed to {@code func}; may be null
	 * @throws E the last failure after retries are exhausted
	 */
	public <T, E extends Throwable> void doWithRetry(ConsumerWithThrowable<T, E> func,T t) throws E {
		this.<Void, T, E>doProcessWithRetry(t1 -> {
			func.accept(t1);
			return null;
		},t );
	}
	
	/**
	 * Retries {@code func.run()} until it succeeds or the policy is exhausted.
	 *
	 * @param <E>  throwable type declared by {@code func}
	 * @param func never-null runnable to invoke
	 * @throws E the last failure after retries are exhausted
	 */
	public <E extends Throwable> void doWithRetry(RunnableWithThrowable<E> func) throws E {
		this.<Void, Void, E>doProcessWithRetry(t -> {
			func.run();
			return null;
		}, null);
	}
	
	/**
	 * Executes {@code func} through {@link RetryTemplate}.
	 *
	 * @param <R>  result type
	 * @param <T>  argument type
	 * @param <E>  throwable type declared by {@code func}
	 * @param func never-null function to invoke
	 * @param t    argument passed to {@code func}; may be null
	 * @return result of {@code func}; may be null
	 * @throws E the last failure after retries are exhausted
	 */
	private <R, T, E extends Throwable> R doProcessWithRetry(FunctionWithThrowable<R, T, E> func, T t) throws E {
		R result = retryTemplate.execute(context -> func.apply(t));
		return result;
	}

}
