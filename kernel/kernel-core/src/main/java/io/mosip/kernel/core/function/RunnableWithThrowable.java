package io.mosip.kernel.core.function;

/**
 * Runnable that may throw a checked exception.
 * <p>
 * Contract: use in place of {@link Runnable} when the lambda performs I/O or
 * MOSIP operations that throw {@code E}.
 * </p>
 *
 * @param <E> the element type which can be a Throwable
 * 
 * @author Loganathan Sekar
 * 
 */
@FunctionalInterface
public interface RunnableWithThrowable<E extends Throwable> {

	/**
	 * Runs the operation.
	 *
	 * @throws E if the operation fails
	 */
	void run() throws E;

}
