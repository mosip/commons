package io.mosip.kernel.syncdata.service.helper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.mosip.kernel.syncdata.dto.BlacklistedWordsDto;
import io.mosip.kernel.syncdata.dto.FoundationalTrustProviderDto;
import io.mosip.kernel.syncdata.dto.HolidayDto;
import io.mosip.kernel.syncdata.dto.ProcessListDto;
import io.mosip.kernel.syncdata.dto.ScreenAuthorizationDto;
import io.mosip.kernel.syncdata.dto.ScreenDetailDto;
import io.mosip.kernel.syncdata.dto.SyncJobDefDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataBaseDto;
import io.mosip.kernel.syncdata.entity.BlacklistedWords;
import io.mosip.kernel.syncdata.entity.FoundationalTrustProvider;
import io.mosip.kernel.syncdata.entity.Holiday;
import io.mosip.kernel.syncdata.entity.ProcessList;
import io.mosip.kernel.syncdata.entity.ScreenAuthorization;
import io.mosip.kernel.syncdata.entity.ScreenDetail;
import io.mosip.kernel.syncdata.entity.SyncJobDef;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;

public class MiscellaneousDataHelper {
	
	private String machineId;
	private LocalDateTime lastUpdated;
	private LocalDateTime currentTimestamp;
	
	CompletableFuture<List<HolidayDto>> holidays = null;
	CompletableFuture<List<BlacklistedWordsDto>> blacklistedWords = null;
	CompletableFuture<List<ScreenAuthorizationDto>> screenAuthorizations = null;
	CompletableFuture<List<ScreenDetailDto>> screenDetails = null;
	CompletableFuture<List<ProcessListDto>> processList = null;
	CompletableFuture<List<SyncJobDefDto>> syncJobDefDtos = null;

	private String publicKey;
	
	public MiscellaneousDataHelper(String machineId, LocalDateTime lastUpdated, LocalDateTime currentTimestamp, String publicKey) {
		this.machineId = machineId;
		this.lastUpdated = lastUpdated;
		this.currentTimestamp = currentTimestamp;
		this.publicKey = publicKey;
	}
	
	public void retrieveData(final SyncMasterDataServiceHelper serviceHelper, final List<CompletableFuture> futures) {
		this.holidays = serviceHelper.getHolidays(this.lastUpdated, this.machineId, this.currentTimestamp);
		this.blacklistedWords = serviceHelper.getBlackListedWords(this.lastUpdated, this.currentTimestamp);
	
		this.screenAuthorizations = serviceHelper.getScreenAuthorizationDetails(this.lastUpdated, this.currentTimestamp);
		this.screenDetails = serviceHelper.getScreenDetails(this.lastUpdated, this.currentTimestamp);
		
		this.processList = serviceHelper.getProcessList(this.lastUpdated, this.currentTimestamp);		

		this.syncJobDefDtos = serviceHelper.getSyncJobDefDetails(this.lastUpdated, this.currentTimestamp);
		
		futures.add(this.holidays);
		futures.add(this.blacklistedWords);
		
		futures.add(this.screenAuthorizations);
		futures.add(this.screenDetails);
		
		futures.add(this.processList);

		futures.add(this.syncJobDefDtos);
	}
	
	public void fillRetrievedData(final SyncMasterDataServiceHelper serviceHelper, final List<SyncDataBaseDto> list) 
			throws InterruptedException, ExecutionException {
		list.add(serviceHelper.getSyncDataBaseDto(Holiday.class, "structured", this.holidays.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(BlacklistedWords.class, "structured", this.blacklistedWords.get(), this.publicKey));
		
		list.add(serviceHelper.getSyncDataBaseDto(ScreenAuthorization.class, "structured", this.screenAuthorizations.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(ScreenDetail.class, "structured", this.screenDetails.get(), this.publicKey));
		
		list.add(serviceHelper.getSyncDataBaseDto(ProcessList.class, "structured", this.processList.get(), this.publicKey));

		list.add(serviceHelper.getSyncDataBaseDto(SyncJobDef.class, "structured", this.syncJobDefDtos.get(), this.publicKey));
	}
}
