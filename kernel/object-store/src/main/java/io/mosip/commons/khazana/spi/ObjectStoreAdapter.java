package io.mosip.commons.khazana.spi;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import io.mosip.commons.khazana.dto.ObjectDto;

public interface ObjectStoreAdapter {

    public InputStream getObject(String account, String container, String source, String process, String objectName);

    public boolean exists(String account, String container, String source, String process, String objectName);

    public boolean putObject(String account, String container, String source, String process, String objectName, InputStream data);

    public Map<String, Object> addObjectMetaData(String account, String container, String source, String process, String objectName, Map<String, Object> metadata);

    public Map<String, Object> addObjectMetaData(String account, String container, String source, String process, String objectName, String key, String value);

    public Map<String, Object> getMetaData(String account, String container, String source, String process, String objectName);

    public Integer incMetadata(String account, String container, String source, String process, String objectName, String metaDataKey);

    public Integer decMetadata(String account, String container, String source, String process, String objectName, String metaDataKey);

    public boolean deleteObject(String account, String container, String source, String process, String objectName);

    public boolean removeContainer(String account, String container, String source, String process);

    public boolean pack(String account, String container, String source, String process, String refId);

    public List<ObjectDto> getAllObjects(String account, String container);

	public Map<String, String> addTags(String account, String container, Map<String, String> tags);

	public Map<String, String> getTags(String account, String container);
	
	public void deleteTags(String account, String container, List<String> tags);
}
