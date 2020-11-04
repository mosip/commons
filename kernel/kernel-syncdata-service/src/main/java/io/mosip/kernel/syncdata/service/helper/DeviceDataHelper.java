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

	private String publicKey;

	public DeviceDataHelper(String regCenterId, LocalDateTime lastUpdated, LocalDateTime currentTimestamp, String publicKey) {
		this.lastUpdated = lastUpdated;
		this.currentTimestamp = currentTimestamp;
		this.regCenterId = regCenterId;
		this.publicKey = publicKey;
	}
	
	public void retrieveData(final SyncMasterDataServiceHelper serviceHelper, final List<CompletableFuture> futures) {
		this.devices = serviceHelper.getDevices(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.deviceSpecifications = serviceHelper.getDeviceSpecifications(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.deviceTypes = serviceHelper.getDeviceType(this.regCenterId, this.lastUpdated, this.currentTimestamp);

		futures.add(this.devices);
		futures.add(this.deviceSpecifications);
		futures.add(this.deviceTypes);
	}
	
	public void fillRetrievedData(final SyncMasterDataServiceHelper serviceHelper, final List<SyncDataBaseDto> list) 
			throws InterruptedException, ExecutionException {
		list.add(serviceHelper.getSyncDataBaseDto(Device.class, "structured", this.devices.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(DeviceSpecification.class, "structured", this.deviceSpecifications.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(DeviceType.class, "structured", this.deviceTypes.get(), this.publicKey));
	}
}
