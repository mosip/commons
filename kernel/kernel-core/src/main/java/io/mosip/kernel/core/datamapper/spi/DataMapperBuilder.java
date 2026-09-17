package io.mosip.kernel.core.datamapper.spi;

import java.util.List;

import io.mosip.kernel.core.datamapper.model.IncludeDataField;

/**
 * Fluent builder for a configured {@link DataMapper}.
 * <p>
 * Contract: each method returns {@code this}. Call {@link #build()} when
 * configuration is complete. Does not perform I/O.
 * </p>
 *
 * @param <S> source bean type
 * @param <D> destination bean type
 * @author Urvil Joshi
 * @since 1.0.0
 * @see DataMapper
 */
public interface DataMapperBuilder<S, D> {

	/**
	 * Configures whether null source values are copied onto the destination.
	 *
	 * @param mapNulls {@code true} to map nulls
	 * @return this builder; never null
	 */
	DataMapperBuilder<S, D> mapNulls(boolean mapNulls);

	/**
	 * Configures whether unlisted fields are mapped by default.
	 *
	 * @param byDefault {@code true} to map remaining fields automatically
	 * @return this builder; never null
	 */
	DataMapperBuilder<S, D> byDefault(boolean byDefault);

	/**
	 * Restricts mapping to the given source/destination field pairs.
	 *
	 * @param includeFields never-null list; may be empty
	 * @return this builder; never null
	 */
	DataMapperBuilder<S, D> includeFields(List<IncludeDataField> includeFields);

	/**
	 * Excludes the given destination field names from mapping.
	 *
	 * @param excludeFields never-null list of field names; may be empty
	 * @return this builder; never null
	 */
	DataMapperBuilder<S, D> excludeFields(List<String> excludeFields);

	/**
	 * Builds a configured {@link DataMapper}.
	 *
	 * @return never-null mapper
	 */
	DataMapper<S, D> build();

}
