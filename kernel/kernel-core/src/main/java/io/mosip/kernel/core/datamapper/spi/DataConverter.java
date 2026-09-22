package io.mosip.kernel.core.datamapper.spi;

/**
 * Custom conversion hook invoked by {@link DataMapper} for a source /
 * destination pair.
 * <p>
 * Contract: implementations mutate {@code destination} from {@code source}.
 * Both arguments are non-null. Does not perform I/O unless the converter
 * itself does.
 * </p>
 *
 * @param <S> the type of the source object
 * @param <D> the type of the destination object
 * @author Neha
 * @since 1.0.0
 */
public interface DataConverter<S, D> {

	/**
	 * Copies or converts values from {@code source} onto {@code destination}.
	 *
	 * @param source      never-null source bean
	 * @param destination never-null destination to mutate
	 */
	public void convert(S source, D destination);
}
