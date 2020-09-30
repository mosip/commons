package io.mosip.kernel.syncdata.service.helper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.mosip.kernel.syncdata.dto.DeviceDto;
import io.mosip.kernel.syncdata.dto.DeviceProviderDto;
import io.mosip.kernel.syncdata.dto.DeviceServiceDto;
import io.mosip.kernel.syncdata.dto.DeviceSpecificationDto;
import io.mosip.kernel.syncdata.dto.DeviceSubTypeDPMDto;
import io.mosip.kernel.syncdata.dto.DeviceTypeDPMDto;
import io.mosip.kernel.syncdata.dto.DeviceTypeDto;
import io.mosip.kernel.syncdata.dto.RegisteredDeviceDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataBaseDto;
import io.mosip.kernel.syncdata.entity.Device;
import io.mosip.kernel.syncdata.entity.DeviceProvider;
import io.mosip.kernel.syncdata.entity.DeviceService;
import io.mosip.kernel.syncdata.entity.DeviceSpecification;
import io.mosip.kernel.syncdata.entity.DeviceSubTypeDPM;
import io.mosip.kernel.syncdata.entity.DeviceType;
import io.mosip.kernel.syncdata.entity.DeviceTypeDPM;
import io.mosip.kernel.syncdata.entity.RegisteredDevice;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;

public class DeviceDataHelper {
	
	private String regCenterId;	
	private LocalDateTime lastUpdated;
	private LocalDateTime currentTimestamp;
	
	CompletableFuture<List<DeviceDto>> devices = null;
	CompletableFuture<List<DeviceSpecificationDto>> deviceSpecifications = null;
	CompletableFuture<List<DeviceTypeDto>> deviceTypes = null;
	/*CompletableFuture<List<DeviceTypeDPMDto>> deviceTypeDPMs = null;
	CompletableFuture<List<DeviceSubTypeDPMDto>> deviceSubTypeDPMs = null;
	CompletableFuture<List<DeviceProviderDto>> deviceProviders = null;
	CompletableFuture<List<DeviceServiceDto>> deviceServices = null;
	CompletableFuture<List<RegisteredDeviceDto>> registeredDevices = null;*/


	
	public DeviceDataHelper(String regCenterId, LocalDateTime lastUpdated, LocalDateTime currentTimestamp) {		
		this.lastUpdated = lastUpdated;
		this.currentTimestamp = currentTimestamp;
		this.regCenterId = regCenterId;
	}
	
	public void retrieveData(final SyncMasterDataServiceHelper serviceHelper, final List<CompletableFuture> futures) {
		this.devices = serviceHelper.getDevices(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.deviceSpecifications = serviceHelper.getDeviceSpecifications(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.deviceTypes = serviceHelper.getDeviceType(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		/*this.deviceTypeDPMs = serviceHelper.getDeviceTypeDetails(this.lastUpdated, this.currentTimestamp);
		this.deviceSubTypeDPMs = serviceHelper.getDeviceSubTypeDetails(this.lastUpdated, this.currentTimestamp);
		this.deviceProviders = serviceHelper.getDeviceProviderDetails(this.lastUpdated, currentTimestamp);
		this.deviceServices = serviceHelper.getDeviceServiceDetails(this.lastUpdated, this.currentTimestamp);
		this.registeredDevices = serviceHelper.getRegisteredDeviceDetails(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		*/

		futures.add(this.devices);
		futures.add(this.deviceSpecifications);
		futures.add(this.deviceTypes);
		/*futures.add(this.deviceTypeDPMs);
		futures.add(this.deviceSubTypeDPMs);
		futures.add(this.deviceProviders);
		futures.add(this.deviceServices);
		futures.add(this.registeredDevices);*/
	}
	
	public void fillRetrievedData(final SyncMasterDataServiceHelper serviceHelper, final List<SyncDataBaseDto> list) 
			throws InterruptedException, ExecutionException {
		list.add(serviceHelper.getSyncDataBaseDto(Device.class, "structured", this.devices.get()));
		list.add(serviceHelper.getSyncDataBaseDto(DeviceSpecification.class, "structured", this.deviceSpecifications.get()));
		list.add(serviceHelper.getSyncDataBaseDto(DeviceType.class, "structured", this.deviceTypes.get()));
		/*list.add(serviceHelper.getSyncDataBaseDto(DeviceTypeDPM.class, "structured", this.deviceTypeDPMs.get()));
		list.add(serviceHelper.getSyncDataBaseDto(DeviceSubTypeDPM.class, "structured", this.deviceSubTypeDPMs.get()));
		list.add(serviceHelper.getSyncDataBaseDto(DeviceProvider.class, "structured", this.deviceProviders.get()));
		list.add(serviceHelper.getSyncDataBaseDto(DeviceService.class, "structured", this.deviceServices.get()));
		list.add(serviceHelper.getSyncDataBaseDto(RegisteredDevice.class, "structured", this.registeredDevices.get()));*/
	}
}
