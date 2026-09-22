package io.mosip.kernel.core.function;

/**
 * Function that accepts one argument, returns a result, and may throw a
 * checked exception.
 * <p>
 * Contract: use in place of {@link java.util.function.Function} when the
 * lambda performs I/O or MOSIP operations that throw {@code E}. {@code t} may
 * be null if the caller allows it.
 * </p>
 *
 * @param <R> return type
 * @param <T> argument type
 * @param <E> checked exception type the function may throw
 * 
 * @author Loganathan Sekar
 * 
 */
@FunctionalInterface
public interface FunctionWithThrowable<R, T, E extends Throwable> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param t the argument; nullability is caller-defined
	 * @return the result; may be null
	 * @throws E if the operation fails
	 */
	R apply(T t) throws E;

}
