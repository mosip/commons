package io.mosip.kernel.datamapper.orika.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import io.mosip.kernel.core.datamapper.exception.DataMapperException;
import io.mosip.kernel.core.datamapper.model.IncludeDataField;
import io.mosip.kernel.core.datamapper.spi.DataConverter;
import io.mosip.kernel.core.datamapper.spi.DataMapper;
import io.mosip.kernel.datamapper.orika.constant.DataMapperErrorCodes;
import io.mosip.kernel.datamapper.orika.provider.MapperFactoryProvider;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * Orika-backed {@link DataMapper} that copies {@code S} onto {@code D}.
 * <p>
 * The constructor registers a class map (null handling, includes, excludes,
 * {@code byDefault}) on the shared {@link MapperFactoryProvider} factory, then
 * obtains a {@link BoundMapperFacade}. Mapping failures become
 * {@link DataMapperException} with {@link DataMapperErrorCodes#MAPPING_ERR}.
 * </p>
 *
 * @author Urvil Joshi
 * @author Neha
 * @since 1.0.0
 * @param <S> source type
 * @param <D> destination type
 */
@Component
public class DataMapperImpl<S, D> implements DataMapper<S, D> {

	/** Bound Orika facade for {@code S} → {@code D}. */
	private BoundMapperFacade<S, D> mapper;

	/**
	 * Registers the class map and binds the Orika facade.
	 *
	 * @param sourceClass      source type
	 * @param destinationClass destination type
	 * @param mapNull          whether null source fields are copied
	 * @param byDefault        whether remaining same-named fields are mapped
	 * @param includeDataField extra source/destination field pairs; {@code null} means none
	 * @param excludeDataField destination field names to skip; {@code null} means none
	 */
	public DataMapperImpl(Class<S> sourceClass, Class<D> destinationClass, boolean mapNull, boolean byDefault,
			List<IncludeDataField> includeDataField, List<String> excludeDataField) {
		DefaultMapperFactory mapperFactory = MapperFactoryProvider.getMapperFactory();
		MapperKey mapperKey = new MapperKey(TypeFactory.valueOf(sourceClass), TypeFactory.valueOf(destinationClass));
		ClassMapBuilder<?, ?> classMapBuilder = mapperFactory.classMap(mapperKey.getAType(), mapperKey.getBType());
		classMapBuilder.mapNulls(mapNull);
		if (excludeDataField != null && !(excludeDataField.isEmpty())) {
			excludeDataField.forEach(classMapBuilder::exclude);
		}

		if (includeDataField != null && !(includeDataField.isEmpty())) {
			includeDataField.forEach(includedField -> classMapBuilder.mapNulls(includedField.isMapIncludeFieldNull())
					.field(includedField.getSourceField(), includedField.getDestinationField()));
		}

		if (byDefault) {
			classMapBuilder.byDefault().register();
		} else {
			classMapBuilder.register();
		}
		this.mapper = mapperFactory.getMapperFacade(sourceClass, destinationClass, false);
	}

	/**
	 * Maps {@code source} to a new destination instance.
	 *
	 * @param source source object
	 * @return mapped destination
	 * @throws DataMapperException if Orika mapping fails
	 */
	@Override
	public D map(S source) {
		try {
			return mapper.map(source);
		} catch (Exception e) {
			throw new DataMapperException(DataMapperErrorCodes.MAPPING_ERR.getErrorCode(),
					DataMapperErrorCodes.MAPPING_ERR.getErrorMessage(), e);
		}
	}

	/**
	 * Maps {@code source} onto the existing {@code destination} instance.
	 *
	 * @param source      source object
	 * @param destination destination to mutate
	 * @throws DataMapperException if Orika mapping fails
	 */
	@Override
	public void map(S source, D destination) {
		try {
			mapper.map(source, destination);
		} catch (Exception e) {
			throw new DataMapperException(DataMapperErrorCodes.MAPPING_ERR.getErrorCode(),
					DataMapperErrorCodes.MAPPING_ERR.getErrorMessage(), e);
		}
	}

	/**
	 * Maps {@code source} onto {@code destination} using a caller-supplied converter.
	 *
	 * @param source        source object
	 * @param destination   destination to mutate
	 * @param dataConverter converter invoked instead of the Orika facade
	 * @throws DataMapperException if the converter fails
	 */
	@Override
	public void map(S source, D destination, DataConverter<S, D> dataConverter) {
		try {
			dataConverter.convert(source, destination);
		} catch (Exception e) {
			throw new DataMapperException(DataMapperErrorCodes.MAPPING_ERR.getErrorCode(),
					DataMapperErrorCodes.MAPPING_ERR.getErrorMessage(), e);
		}
	}
}