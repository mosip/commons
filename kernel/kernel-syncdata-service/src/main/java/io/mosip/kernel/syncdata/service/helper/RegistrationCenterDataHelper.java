package io.mosip.kernel.syncdata.service.helper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.mosip.kernel.syncdata.dto.RegistrationCenterDeviceDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineDeviceDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterTypeDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserMachineMappingDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataBaseDto;
import io.mosip.kernel.syncdata.entity.RegistrationCenter;

import io.mosip.kernel.syncdata.entity.RegistrationCenterType;
import io.mosip.kernel.syncdata.service.helper.beans.RegistrationCenterDevice;
import io.mosip.kernel.syncdata.service.helper.beans.RegistrationCenterMachine;
import io.mosip.kernel.syncdata.service.helper.beans.RegistrationCenterMachineDevice;
import io.mosip.kernel.syncdata.service.helper.beans.RegistrationCenterUser;
import io.mosip.kernel.syncdata.service.helper.beans.RegistrationCenterUserMachine;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;

public class RegistrationCenterDataHelper {
	
	private String regCenterId;
	private String machineId;
	private LocalDateTime lastUpdated;
	private LocalDateTime currentTimestamp;
	
	CompletableFuture<List<RegistrationCenterTypeDto>> registrationCenterTypes = null;
	CompletableFuture<List<RegistrationCenterDto>> registrationCenters = null;
	CompletableFuture<List<RegistrationCenterMachineDto>> registrationCenterMachines = null;
	CompletableFuture<List<RegistrationCenterDeviceDto>> registrationCenterDevices = null;
	CompletableFuture<List<RegistrationCenterMachineDeviceDto>> registrationCenterMachineDevices = null;
	CompletableFuture<List<RegistrationCenterUserMachineMappingDto>> registrationCenterUserMachines = null;
	CompletableFuture<List<RegistrationCenterUserDto>> registrationCenterUsers = null;

	private String publicKey;

	
	public RegistrationCenterDataHelper(String regCenterId, String machineId, LocalDateTime lastUpdated, LocalDateTime currentTimestamp, String publicKey) {
		this.machineId = machineId;
		this.lastUpdated = lastUpdated;
		this.currentTimestamp = currentTimestamp;
		this.regCenterId = regCenterId;
		this.publicKey = publicKey;
	}
	
	public void retrieveData(final SyncMasterDataServiceHelper serviceHelper, final List<CompletableFuture> futures) {
		this.registrationCenterTypes = serviceHelper.getRegistrationCenterType(this.machineId, this.lastUpdated, this.currentTimestamp);
		this.registrationCenters = serviceHelper.getRegistrationCenter(this.machineId, this.lastUpdated, this.currentTimestamp);
		this.registrationCenterMachines = serviceHelper.getRegistrationCenterMachines(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.registrationCenterDevices = serviceHelper.getRegistrationCenterDevices(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.registrationCenterMachineDevices = serviceHelper.getRegistrationCenterMachineDevices(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.registrationCenterUserMachines = serviceHelper.getRegistrationCenterUserMachines(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.registrationCenterUsers = serviceHelper.getRegistrationCenterUsers(this.regCenterId, this.lastUpdated, this.currentTimestamp);

		futures.add(this.registrationCenterTypes);
		futures.add(this.registrationCenters);
		futures.add(this.registrationCenterMachines);
		futures.add(this.registrationCenterDevices);
		futures.add(this.registrationCenterMachineDevices);
		futures.add(this.registrationCenterUserMachines);
		futures.add(this.registrationCenterUsers);
	}
	
	public void fillRetrievedData(final SyncMasterDataServiceHelper serviceHelper, final List<SyncDataBaseDto> list) 
			throws InterruptedException, ExecutionException {
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterType.class, "structured", this.registrationCenterTypes.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenter.class, "structured", this.registrationCenters.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterMachine.class, "structured", this.registrationCenterMachines.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterDevice.class, "structured", this.registrationCenterDevices.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterMachineDevice.class, "structured", this.registrationCenterMachineDevices.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterUserMachine.class, "structured", this.registrationCenterUserMachines.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterUser.class, "structured", this.registrationCenterUsers.get(), this.publicKey));
	}

}
