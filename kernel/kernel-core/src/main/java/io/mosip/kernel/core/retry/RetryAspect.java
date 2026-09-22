package io.mosip.kernel.core.retry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.util.RetryUtil;

/**
 * Around-advice that retries methods annotated with {@link WithRetry}.
 * <p>
 * Contract: delegates to {@link RetryUtil#doWithRetry}. The join point must be
 * a Spring bean method. Thrown exceptions follow the configured retry policy.
 * </p>
 *
 * @author Loganathan Sekar
 */
@Aspect
@Component
public class RetryAspect {

	/** Retry helper that executes the join point through {@link RetryTemplate}. */
	@Autowired
	private RetryUtil retryUtil;

	/**
	 * Matches methods annotated with {@link WithRetry}.
	 */
	@Pointcut("@annotation(WithRetry)")
	public void withRetryMethods() {
	}

	/**
	 * Proceeds the join point through the kernel retry template.
	 *
	 * @param pjp never-null intercepted method
	 * @return the method result; may be null
	 * @throws Throwable the last exception after retries are exhausted, or the method's own throwable
	 */
	@Around("withRetryMethods()")
	public Object processMethodsWithRetry(final ProceedingJoinPoint pjp) throws Throwable {
		return retryUtil.<Object, Throwable>doWithRetry(() -> pjp.proceed());
	}
}
