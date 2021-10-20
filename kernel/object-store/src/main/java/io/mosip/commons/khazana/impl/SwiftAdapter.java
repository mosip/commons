package io.mosip.commons.khazana.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Swift adapter has not been tested.
 */
@Service
@Qualifier("SwiftAdapter")
public class SwiftAdapter implements ObjectStoreAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftAdapter.class);


    @Value("object.store.swift.username:test")
    private String userName;

    @Value("object.store.swift.password:test")
    private String password;

    @Value("object.store.swift.url:null")
    private String authUrl;

    private Map<String, Account> accounts = new HashMap<>();


    public InputStream getObject(String account, String containerName, String source, String process, String objectName) {
        Container container = getConnection(account).getContainer(containerName);
        if (!container.exists())
            container = getConnection(account).getContainer(containerName).create();
        return container.getObject(objectName).downloadObjectAsInputStream();
    }

    public boolean putObject(String account, String containerName, String source, String process, String objectName, InputStream data) {
        Container container = getConnection(account).getContainer(containerName);
        if (!container.exists())
            container = getConnection(account).getContainer(containerName).create();
        StoredObject storedObject = container.getObject(objectName);
        storedObject.uploadObject(data);

        return true;
    }

    public boolean exists(String account, String containerName, String source, String process, String objectName) {
        Container container = getConnection(account).getContainer(containerName);
        return container.exists() && container.getObject(objectName).exists();
    }

    public Map<String, Object> addObjectMetaData(String account, String containerName, String source, String process, String objectName, Map<String, Object> metadata) {

        Container container = getConnection(account).getContainer(containerName);
        if (!container.exists())
            return null;
        StoredObject storedObject = container.getObject(objectName);
        storedObject.setMetadata(metadata);
        storedObject.saveMetadata();
        return metadata;
    }

    public Map<String, Object> addObjectMetaData(String account, String containerName, String source, String process, String objectName, String key, String value) {
        Container container = getConnection(account).getContainer(containerName);
        if (!container.exists())
            return null;
        StoredObject storedObject = container.getObject(objectName);
        storedObject.getMetadata();
        Map<String, Object> existingMetadata = storedObject.getMetadata();
        existingMetadata.put(key, value);
        storedObject.setMetadata(existingMetadata);
        storedObject.saveMetadata();
        return existingMetadata;
    }

    public Map<String, Object> getMetaData(String account, String containerName, String source, String process, String objectName) {
        Map<String, Object> metaData = new HashMap<>();
        Container container = getConnection(account).getContainer(containerName);
        if (!container.exists())
            return null;
        if (objectName == null)
            container.list().forEach(obj -> metaData.put(obj.getName(), obj.getMetadata()));
        else {
            StoredObject storedObject = container.getObject(objectName);
            metaData.put(storedObject.getName(), storedObject.getMetadata());
        }
        return metaData;
    }

    private Account getConnection(String accountName) {
        if (!accounts.isEmpty() && accounts.get(accountName) != null)
            return accounts.get(accountName);

        AccountConfig config = new AccountConfig();
        config.setUsername(userName);
        config.setPassword(password);
        config.setAuthUrl(authUrl);
        config.setTenantName(accountName);
        config.setAuthenticationMethod(AuthenticationMethod.BASIC);
        Account account = new AccountFactory(config).setAllowReauthenticate(true).createAccount();
        accounts.put(accountName, account);
        return account;
    }

    @Override
    public Integer incMetadata(String account, String container, String source, String process, String objectName, String metaDataKey) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Integer decMetadata(String account, String container, String source, String process, String objectName, String metaDataKey) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean deleteObject(String account, String container, String source, String process, String objectName) {
        return true;
    }

    /**
     * Not Supported in SwiftAdapter
     *
     * @param account
     * @param container
     * @param source
     * @param process
     * @return
     */
    @Override
    public boolean removeContainer(String account, String container, String source, String process) {
        return false;
    }

    /**
     * Not Supported in SwiftAdapter
     *
     * @param account
     * @param container
     * @param source
     * @param process
     * @param objectName
     * @param data
     * @return
     */
    @Override
    public boolean pack(String account, String container, String source, String process) {
        return false;
    }

	@Override
	public Map<String, String> addTags(String account, String containerName, Map<String, String> tags) {
		Map<String, Object> tagMap = new HashMap<>();
		Container container = getConnection(account).getContainer(containerName);
		 if (!container.exists())
	            container = getConnection(account).getContainer(containerName).create();
		Map<String, String> existingTags = getTags(account, containerName);
		existingTags.entrySet().forEach(m -> tagMap.put(m.getKey(), m.getValue()));
		tags.entrySet().stream().forEach(m -> tagMap.put(m.getKey(), m.getValue()));
		container.setMetadata(tagMap);
		container.saveMetadata();
		return tags;
	}

	@Override
	public Map<String, String> getTags(String account, String containerName) {
		Map<String, String> metaData = new HashMap<>();
		Container container = getConnection(account).getContainer(containerName);
		 if (!container.exists())
	            container = getConnection(account).getContainer(containerName).create();
		if (container.getMetadata() != null) {
			container.getMetadata().entrySet().stream().forEach(m -> metaData.put(m.getKey(), m.getValue().toString()));

		}

		return metaData;

	}

    /**
     * Not supported in swift adapter
     *
     * @param account
     * @param container
     * @return
     */
    public List<ObjectDto> getAllObjects(String account, String container) {
        return null;
    }
}
