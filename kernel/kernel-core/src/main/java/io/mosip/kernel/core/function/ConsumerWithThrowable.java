package io.mosip.kernel.core.function;

/**
 * Consumer that accepts one argument and may throw a checked exception.
 * <p>
 * Contract: use in place of {@link java.util.function.Consumer} when the
 * lambda performs I/O or MOSIP operations that throw {@code E}. {@code t} may
 * be null if the caller allows it.
 * </p>
 *
 * @param <T> the generic type
 * @param <E> the element type that can be any Throwable
 * 
 * @author Loganathan Sekar
 * 
 */
@FunctionalInterface
public interface ConsumerWithThrowable<T, E extends Throwable> {

	/**
	 * Performs this operation on the given argument.
	 *
	 * @param t the argument; nullability is caller-defined
	 * @throws E if the operation fails
	 */
	void accept(T t) throws E;

}