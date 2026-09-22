package io.mosip.kernel.datamapper.orika.provider;

import lombok.Getter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

/**
 * Holds the process-wide Orika {@link DefaultMapperFactory}.
 * <p>
 * {@link DataMapperImpl} registers class maps on this singleton factory.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class MapperFactoryProvider {

	/**
	 * Prevents instantiation.
	 */
	private MapperFactoryProvider() {

	}

	/**
	 * Shared Orika factory used by all {@link io.mosip.kernel.datamapper.orika.impl.DataMapperImpl} instances.
	 */
	@Getter
	private static DefaultMapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

}
