package io.mosip.kernel.syncdata.service.helper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.mosip.kernel.syncdata.dto.BiometricAttributeDto;
import io.mosip.kernel.syncdata.dto.BiometricTypeDto;
import io.mosip.kernel.syncdata.dto.GenderDto;
import io.mosip.kernel.syncdata.dto.IdTypeDto;
import io.mosip.kernel.syncdata.dto.IndividualTypeDto;
import io.mosip.kernel.syncdata.dto.LanguageDto;
import io.mosip.kernel.syncdata.dto.LocationDto;
import io.mosip.kernel.syncdata.dto.PostReasonCategoryDto;
import io.mosip.kernel.syncdata.dto.ReasonListDto;
import io.mosip.kernel.syncdata.dto.TitleDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataBaseDto;
import io.mosip.kernel.syncdata.entity.BiometricAttribute;
import io.mosip.kernel.syncdata.entity.BiometricType;
import io.mosip.kernel.syncdata.entity.Gender;
import io.mosip.kernel.syncdata.entity.IdType;
import io.mosip.kernel.syncdata.entity.IndividualType;
import io.mosip.kernel.syncdata.entity.Language;
import io.mosip.kernel.syncdata.entity.Location;
import io.mosip.kernel.syncdata.entity.ReasonCategory;
import io.mosip.kernel.syncdata.entity.ReasonList;
import io.mosip.kernel.syncdata.entity.Title;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;

public class IndividualDataHelper {
	
	private LocalDateTime lastUpdated;
	private LocalDateTime currentTimestamp;
	
	CompletableFuture<List<TitleDto>> titles = null;
	CompletableFuture<List<LanguageDto>> languages = null;
	CompletableFuture<List<GenderDto>> genders = null;
	CompletableFuture<List<IdTypeDto>> idTypes =null;
	CompletableFuture<List<LocationDto>> locationHierarchy = null;
	CompletableFuture<List<PostReasonCategoryDto>> reasonCategory = null;
	CompletableFuture<List<ReasonListDto>> reasonList = null;
	CompletableFuture<List<IndividualTypeDto>> individualTypeList = null;
	CompletableFuture<List<BiometricTypeDto>> biometricTypes = null;
	CompletableFuture<List<BiometricAttributeDto>> biometricAttributes = null;

	private String publicKey;
	
	public IndividualDataHelper( LocalDateTime lastUpdated, LocalDateTime currentTimestamp, String publicKey) {
		this.lastUpdated = lastUpdated;
		this.currentTimestamp = currentTimestamp;
		this.publicKey = publicKey;
	}
	
	public void retrieveData(final SyncMasterDataServiceHelper serviceHelper, final List<CompletableFuture> futures) {
		this.titles = serviceHelper.getTitles(this.lastUpdated, this.currentTimestamp);
		this.languages = serviceHelper.getLanguages(this.lastUpdated, this.currentTimestamp);
		this.genders = serviceHelper.getGenders(this.lastUpdated, this.currentTimestamp);
		this.idTypes = serviceHelper.getIdTypes(this.lastUpdated, this.currentTimestamp);
		this.locationHierarchy = serviceHelper.getLocationHierarchy(this.lastUpdated, this.currentTimestamp);
		this.reasonCategory = serviceHelper.getReasonCategory(this.lastUpdated, this.currentTimestamp);
		this.reasonList = serviceHelper.getReasonList(this.lastUpdated, this.currentTimestamp);
		this.individualTypeList = serviceHelper.getIndividualType(this.lastUpdated, this.currentTimestamp);
		this.biometricTypes = serviceHelper.getBiometricTypes(this.lastUpdated, this.currentTimestamp);
		this.biometricAttributes = serviceHelper.getBiometricAttributes(this.lastUpdated, this.currentTimestamp);	
		
		futures.add(this.titles);
		futures.add(this.languages);
		futures.add(this.genders);
		futures.add(this.idTypes);
		futures.add(this.locationHierarchy);
		futures.add(this.reasonCategory);
		futures.add(this.reasonList);
		futures.add(this.individualTypeList);
		futures.add(this.biometricTypes);
		futures.add(this.biometricAttributes);
	}
	
	public void fillRetrievedData(final SyncMasterDataServiceHelper serviceHelper, final List<SyncDataBaseDto> list) 
			throws InterruptedException, ExecutionException {
		list.add(serviceHelper.getSyncDataBaseDto(Title.class, "structured", this.titles.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(Language.class, "structured", this.languages.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(Gender.class, "structured", this.genders.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(IdType.class, "structured", this.idTypes.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(Location.class, "structured", this.locationHierarchy.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(ReasonCategory.class, "structured", this.reasonCategory.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(ReasonList.class, "structured",this.reasonList.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(IndividualType.class, "structured", this.individualTypeList.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(BiometricType.class, "structured", this.biometricTypes.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(BiometricAttribute.class, "structured", this.biometricAttributes.get(), this.publicKey));
	}
}
