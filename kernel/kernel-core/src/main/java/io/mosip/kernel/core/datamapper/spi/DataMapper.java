package io.mosip.kernel.core.datamapper.spi;

/**
 * Maps a source Java bean onto a destination type using a configured Orika
 * (or similar) mapper.
 * <p>
 * Contract: implementations copy properties; they do not persist or perform
 * HTTP. {@code source} must be non-null. Destination instances are created or
 * mutated in place. Call from service layers when converting entities to DTOs.
 * </p>
 *
 * @param <S> source bean type
 * @param <D> destination bean type
 * @author Neha
 * @since 1.0.0
 * @see DataMapperBuilder
 */
public interface DataMapper<S, D> {

	/**
	 * Creates a new destination instance and maps {@code source} onto it.
	 *
	 * @param source never-null source bean
	 * @return never-null mapped destination
	 * @throws io.mosip.kernel.core.datamapper.exception.DataMapperException when
	 *                                                                        mapping
	 *                                                                        fails
	 */
	public D map(S source);

	/**
	 * Maps {@code source} onto an existing {@code destination} instance.
	 *
	 * @param source      never-null source bean
	 * @param destination never-null destination to mutate
	 * @throws io.mosip.kernel.core.datamapper.exception.DataMapperException when
	 *                                                                        mapping
	 *                                                                        fails
	 */
	public void map(S source, D destination);

	/**
	 * Maps {@code source} onto {@code destination} using a custom converter.
	 *
	 * @param source        never-null source bean
	 * @param destination   never-null destination to mutate
	 * @param dataConverter never-null converter invoked for the pair
	 * @throws io.mosip.kernel.core.datamapper.exception.DataMapperException when
	 *                                                                        mapping
	 *                                                                        fails
	 */
	public void map(S source, D destination, DataConverter<S, D> dataConverter);
}
