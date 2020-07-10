package io.mosip.kernel.core.deviceprovidermanager.spi;

/**
 * @author M1046464
 *
 * @param <T> -
 * @param <D>
 * @param <S>
 * @param <U>
 */

public interface DeviceProviderService<ResponseDto, ValidateDeviceDto, DeviceProviderDto, DeviceProviderExtnDto, DeviceProviderPutDto> {

	/**
	 * Validate device providers.
	 *
	 * @param validateDeviceDto the validate device dto
	 * @return {@link ResponseDto}
	 */
	public ResponseDto validateDeviceProviders(ValidateDeviceDto validateDeviceDto);


	/**
	 * Method to create Device Provider
	 * 
	 * @param dto Device Provider dto from user
	 * @return DeviceProviderExtnDto device Provider dto which has created
	 */
	public DeviceProviderExtnDto createDeviceProvider(DeviceProviderDto dto);

	/**
	 * Method to update Device Provider
	 * 
	 * @param dto Device Provider dto from user
	 * @return DeviceProviderExtnDto device Provider dto which has updated
	 */
	public DeviceProviderExtnDto updateDeviceProvider(DeviceProviderPutDto dto);
}
