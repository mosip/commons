package io.mosip.kernel.core.function;

/**
 * Supplier that returns a result and may throw a checked exception.
 * <p>
 * Contract: use in place of {@link java.util.function.Supplier} when the
 * lambda performs I/O or MOSIP operations that throw {@code E}.
 * </p>
 *
 * @param <R> the generic type that is returned
 * @param <E> the element type that can be any Throwable
 * 
 * @author Loganathan Sekar
 */
@FunctionalInterface
public interface SupplierWithThrowable<R, E extends Throwable> {

	/**
	 * Supplies a value.
	 *
	 * @return the result; may be null
	 * @throws E if the operation fails
	 */
	R get() throws E;

}
