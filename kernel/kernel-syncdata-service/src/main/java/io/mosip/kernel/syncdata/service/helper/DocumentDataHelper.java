package io.mosip.kernel.syncdata.service.helper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.mosip.kernel.syncdata.dto.ApplicantValidDocumentDto;
import io.mosip.kernel.syncdata.dto.DocumentCategoryDto;
import io.mosip.kernel.syncdata.dto.DocumentTypeDto;
import io.mosip.kernel.syncdata.dto.ValidDocumentDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataBaseDto;
import io.mosip.kernel.syncdata.entity.ApplicantValidDocument;
import io.mosip.kernel.syncdata.entity.DocumentCategory;
import io.mosip.kernel.syncdata.entity.DocumentType;
import io.mosip.kernel.syncdata.entity.ValidDocument;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;

public class DocumentDataHelper {
	
	private LocalDateTime lastUpdated;
	private LocalDateTime currentTimestamp;
	
	CompletableFuture<List<DocumentCategoryDto>> documentCategories = null;
	CompletableFuture<List<DocumentTypeDto>> documentTypes = null;
	CompletableFuture<List<ValidDocumentDto>> validDocumentsMapping = null;
	CompletableFuture<List<ApplicantValidDocumentDto>> applicantValidDocumentList = null;

	private String publicKey;
	
	public DocumentDataHelper( LocalDateTime lastUpdated, LocalDateTime currentTimestamp, String publicKey) {
		this.lastUpdated = lastUpdated;
		this.currentTimestamp = currentTimestamp;
		this.publicKey = publicKey;
	}
	
	public void retrieveData(final SyncMasterDataServiceHelper serviceHelper, final List<CompletableFuture> futures) {
		this.documentCategories = serviceHelper.getDocumentCategories(this.lastUpdated, this.currentTimestamp);
		this.documentTypes = serviceHelper.getDocumentTypes(this.lastUpdated, this.currentTimestamp);								
		this.validDocumentsMapping = serviceHelper.getValidDocuments(this.lastUpdated, this.currentTimestamp);		
		this.applicantValidDocumentList = serviceHelper.getApplicantValidDocument(this.lastUpdated, this.currentTimestamp);
		
		futures.add(this.documentCategories);
		futures.add(this.documentTypes);
		futures.add(this.validDocumentsMapping);
		futures.add(this.applicantValidDocumentList);
	}
	
	public void fillRetrievedData(final SyncMasterDataServiceHelper serviceHelper, final List<SyncDataBaseDto> list) 
			throws InterruptedException, ExecutionException {
		list.add(serviceHelper.getSyncDataBaseDto(DocumentCategory.class, "structured", this.documentCategories.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(DocumentType.class, "structured", this.documentTypes.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(ValidDocument.class, "structured", this.validDocumentsMapping.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(ApplicantValidDocument.class, "structured", this.applicantValidDocumentList.get(), this.publicKey));
	}
}
