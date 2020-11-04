package io.mosip.kernel.syncdata.service.helper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.mosip.kernel.syncdata.dto.RegistrationCenterDeviceHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineDeviceHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterMachineHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserHistoryDto;
import io.mosip.kernel.syncdata.dto.RegistrationCenterUserMachineMappingHistoryDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataBaseDto;
import io.mosip.kernel.syncdata.service.helper.beans.RegistrationCenterDeviceHistory;
import io.mosip.kernel.syncdata.service.helper.beans.RegistrationCenterMachineDeviceHistory;
import io.mosip.kernel.syncdata.service.helper.beans.RegistrationCenterMachineHistory;
import io.mosip.kernel.syncdata.service.helper.beans.RegistrationCenterUserHistory;
import io.mosip.kernel.syncdata.service.helper.beans.RegistrationCenterUserMachineHistory;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;

public class HistoryDataHelper {
	
	private String regCenterId;
	private LocalDateTime lastUpdated;
	private LocalDateTime currentTimestamp;
	
	CompletableFuture<List<RegistrationCenterUserHistoryDto>> registrationCenterUserHistoryList = null;
	CompletableFuture<List<RegistrationCenterUserMachineMappingHistoryDto>> registrationCenterUserMachineMappingHistoryList = null;
	CompletableFuture<List<RegistrationCenterMachineDeviceHistoryDto>> registrationCenterMachineDeviceHistoryList = null;
	CompletableFuture<List<RegistrationCenterDeviceHistoryDto>> registrationCenterDeviceHistoryList = null;
	CompletableFuture<List<RegistrationCenterMachineHistoryDto>> registrationCenterMachineHistoryList = null;

	private String publicKey;
	
	public HistoryDataHelper(String regCenterId, LocalDateTime lastUpdated, LocalDateTime currentTimestamp, String publicKey) {
		this.regCenterId = regCenterId;
		this.lastUpdated = lastUpdated;
		this.currentTimestamp = currentTimestamp;
		this.publicKey = publicKey;
	}
	
	public void retrieveData(final SyncMasterDataServiceHelper serviceHelper, final List<CompletableFuture> futures) {
		this.registrationCenterUserHistoryList = serviceHelper.getRegistrationCenterUserHistory(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.registrationCenterUserMachineMappingHistoryList = serviceHelper
				.getRegistrationCenterUserMachineMapping(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.registrationCenterMachineDeviceHistoryList = serviceHelper.getRegistrationCenterMachineDeviceHistoryDetails(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.registrationCenterDeviceHistoryList = serviceHelper.getRegistrationCenterDeviceHistoryDetails(this.regCenterId, this.lastUpdated, this.currentTimestamp);
		this.registrationCenterMachineHistoryList = serviceHelper.getRegistrationCenterMachineHistoryDetails(this.regCenterId, this.lastUpdated, this.currentTimestamp);
	
		futures.add(this.registrationCenterUserHistoryList);
		futures.add(this.registrationCenterUserMachineMappingHistoryList);
		futures.add(this.registrationCenterMachineDeviceHistoryList);
		futures.add(this.registrationCenterDeviceHistoryList);
		futures.add(this.registrationCenterMachineHistoryList);
	}
	
	public void fillRetrievedData(final SyncMasterDataServiceHelper serviceHelper, final List<SyncDataBaseDto> list) 
			throws InterruptedException, ExecutionException {
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterUserHistory.class, "structured", this.registrationCenterUserHistoryList.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterUserMachineHistory.class, "structured", this.registrationCenterUserMachineMappingHistoryList.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterMachineDeviceHistory.class, "structured", this.registrationCenterMachineDeviceHistoryList.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterDeviceHistory.class, "structured", this.registrationCenterDeviceHistoryList.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(RegistrationCenterMachineHistory.class, "structured", this.registrationCenterMachineHistoryList.get(), this.publicKey));
	}
}
