package io.mosip.kernel.syncdata.service.helper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.mosip.kernel.syncdata.dto.TemplateDto;
import io.mosip.kernel.syncdata.dto.TemplateFileFormatDto;
import io.mosip.kernel.syncdata.dto.TemplateTypeDto;
import io.mosip.kernel.syncdata.dto.response.SyncDataBaseDto;
import io.mosip.kernel.syncdata.entity.Template;
import io.mosip.kernel.syncdata.entity.TemplateFileFormat;
import io.mosip.kernel.syncdata.entity.TemplateType;
import io.mosip.kernel.syncdata.utils.SyncMasterDataServiceHelper;

public class TemplateDataHelper {
	
	private LocalDateTime lastUpdated;
	private LocalDateTime currentTimestamp;
	
	CompletableFuture<List<TemplateDto>> templates =null;
	CompletableFuture<List<TemplateFileFormatDto>> templateFileFormats = null;
	CompletableFuture<List<TemplateTypeDto>> templateTypes = null;

	private String publicKey;
	
	public TemplateDataHelper( LocalDateTime lastUpdated, LocalDateTime currentTimestamp, String publicKey) {
		this.lastUpdated = lastUpdated;
		this.currentTimestamp = currentTimestamp;
		this.publicKey = publicKey;
	}
	
	public void retrieveData(final SyncMasterDataServiceHelper serviceHelper, final List<CompletableFuture> futures) {
		this.templates = serviceHelper.getTemplates(this.lastUpdated, this.currentTimestamp);
		this.templateFileFormats = serviceHelper.getTemplateFileFormats(this.lastUpdated, this.currentTimestamp);
		this.templateTypes = serviceHelper.getTemplateTypes(this.lastUpdated, this.currentTimestamp);	
		
		futures.add(this.templates);
		futures.add(this.templateFileFormats);
		futures.add(this.templateTypes);
	}
	
	public void fillRetrievedData(final SyncMasterDataServiceHelper serviceHelper, final List<SyncDataBaseDto> list) 
			throws InterruptedException, ExecutionException {
		list.add(serviceHelper.getSyncDataBaseDto(Template.class, "structured", this.templates.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(TemplateFileFormat.class, "structured", this.templateFileFormats.get(), this.publicKey));
		list.add(serviceHelper.getSyncDataBaseDto(TemplateType.class, "structured", this.templateTypes.get(), this.publicKey));
	}
}
