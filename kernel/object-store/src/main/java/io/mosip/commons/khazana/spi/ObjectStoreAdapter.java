package io.mosip.commons.khazana.spi;

import java.io.InputStream;
import java.util.Map;

public interface ObjectStoreAdapter {

    public InputStream getObject(String account, String container, String objectName);

    public boolean exists(String account, String container, String objectName);

    public boolean putObject(String account, String container, String objectName, InputStream data);

    public Map<String, Object> addObjectMetaData(String account, String container, String objectName, Map<String, Object> metadata);

    public Map<String, Object> addObjectMetaData(String account, String container, String objectName, String key, String value);

    public Map<String, Object> getMetaData(String account, String container, String objectName);
	
	public int incMetadata(String account, String container, String objectName,String metaDataKey);
	//Stops at zero
	public int decMetadata(String account, String container, String objectName,String metaDataKey);

}
