package io.mosip.kernel.core.deviceprovidermanager.spi;

/**
 * Creates, updates, and validates MOSIP device providers.
 * <p>
 * Contract: implementations typically perform HTTP or database I/O against
 * masterdata. DTOs must be non-null and satisfy bean validation. Call from
 * partner / device-provider APIs.
 * </p>
 *
 * @param <ResponseDto>           validation-result type
 * @param <ValidateDeviceDto>     device-validation request type
 * @param <DeviceProviderDto>     create request type
 * @param <DeviceProviderExtnDto> created/updated provider type
 * @param <DeviceProviderPutDto>  update request type
 * @author M1046464
 */
public interface DeviceProviderService<ResponseDto, ValidateDeviceDto, DeviceProviderDto, DeviceProviderExtnDto, DeviceProviderPutDto> {

	/**
	 * Validates that the given devices belong to registered providers.
	 *
	 * @param validateDeviceDto never-null validation request
	 * @return never-null validation result
	 */
	public ResponseDto validateDeviceProviders(ValidateDeviceDto validateDeviceDto);


	/**
	 * Creates a device provider.
	 *
	 * @param dto never-null create payload
	 * @return never-null created provider including generated identifiers
	 */
	public DeviceProviderExtnDto createDeviceProvider(DeviceProviderDto dto);

	/**
	 * Updates an existing device provider.
	 *
	 * @param dto never-null update payload identifying the provider
	 * @return never-null updated provider
	 */
	public DeviceProviderExtnDto updateDeviceProvider(DeviceProviderPutDto dto);
}
