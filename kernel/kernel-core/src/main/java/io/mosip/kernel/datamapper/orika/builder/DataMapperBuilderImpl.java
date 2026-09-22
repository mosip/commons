package io.mosip.kernel.datamapper.orika.builder;

import java.util.List;

import io.mosip.kernel.core.datamapper.model.IncludeDataField;
import io.mosip.kernel.core.datamapper.spi.DataMapper;
import io.mosip.kernel.core.datamapper.spi.DataMapperBuilder;
import io.mosip.kernel.datamapper.orika.impl.DataMapperImpl;

/**
 * DataMapper Builder implementation for configuring {@link DataMapperImpl} with
 * configurations {@link #mapNulls} {@link #byDefault} {@link #sourceClass}
 * {@link #destinationClass} {@link #includeFields} {@link #excludeFields}
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
public class DataMapperBuilderImpl<S, D> implements DataMapperBuilder<S, D> {

	/**
	 * Configure map null in mapping
	 */
	private boolean mapNulls = true;
	/**
	 * Configure byDefault in mapping
	 */
	private boolean byDefault = true;
	/**
	 * Configure source class in mapping
	 */
	private Class<S> sourceClass;
	/**
	 * Configure destination class in mapping
	 */
	private Class<D> destinationClass;
	/**
	 * Configure included field in mapping
	 */
	private List<IncludeDataField> includeFields = null;
	/**
	 * Configure excluded fields in mapping
	 */
	private List<String> excludeFields = null;

	/**
	 * Creates a builder for mapping {@code sourceClass} onto {@code destinationClass}.
	 *
	 * @param sourceClass      Orika source type
	 * @param destinationClass Orika destination type
	 */
	public DataMapperBuilderImpl(Class<S> sourceClass, Class<D> destinationClass) {
		this.sourceClass = sourceClass;
		this.destinationClass = destinationClass;

	}

	/**
	 * Sets whether {@code null} source fields are copied to the destination.
	 *
	 * @param mapNulls {@code true} to map nulls (default)
	 * @return this builder
	 */
	@Override
	public DataMapperBuilder<S, D> mapNulls(boolean mapNulls) {
		this.mapNulls = mapNulls;
		return this;
	}

	/**
	 * Sets whether Orika applies default field-name matching after explicit maps.
	 *
	 * @param byDefault {@code true} to call {@code byDefault()} on the class map (default)
	 * @return this builder
	 */
	@Override
	public DataMapperBuilder<S, D> byDefault(boolean byDefault) {
		this.byDefault = byDefault;
		return this;
	}

	/**
	 * Restricts mapping to the listed source/destination field pairs.
	 *
	 * @param includeFields fields to map; {@code null} means none extra
	 * @return this builder
	 */
	@Override
	public DataMapperBuilder<S, D> includeFields(List<IncludeDataField> includeFields) {
		this.includeFields = includeFields;
		return this;
	}

	/**
	 * Excludes the listed destination field names from mapping.
	 *
	 * @param excludeFields field names to skip; {@code null} means none
	 * @return this builder
	 */
	@Override
	public DataMapperBuilder<S, D> excludeFields(List<String> excludeFields) {
		this.excludeFields = excludeFields;
		return this;
	}

	/**
	 * Builds a {@link DataMapperImpl} with the configured class map.
	 *
	 * @return mapper ready to copy {@code S} onto {@code D}
	 */
	@Override
	public DataMapper<S, D> build() {
		return new DataMapperImpl<>(sourceClass, destinationClass, mapNulls, byDefault, includeFields, excludeFields);
	}

}