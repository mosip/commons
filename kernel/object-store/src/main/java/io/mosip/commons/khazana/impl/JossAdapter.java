package io.mosip.commons.khazana.impl;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.util.Map;

public class JossAdapter implements ObjectStoreAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JossAdapter.class);

    @Value("object.store.username")
    private String userName;

    @Value("object.store.password")
    private String password;

    @Value("object.store.authurl")
    private String authUrl;

    private Account account = null;


    public InputStream getObject(String account, String containerName, String objectName) {
        Container container = getConnection().getContainer(containerName);
        if (!container.exists())
            container = getConnection().getContainer(containerName).create();
        return container.getObject(objectName).downloadObjectAsInputStream();
    }

    public boolean exists(String account, String containerName, String objectName) {
        Container container = getConnection().getContainer(containerName);
        return container.exists() && container.getObject(objectName).exists();
    }

    public boolean putObject(String account, String containerName, String objectName, InputStream data) {
        Container container = getConnection().getContainer(containerName);
        if (!container.exists())
            container = getConnection().getContainer(containerName).create();
        StoredObject storedObject = container.getObject(objectName);
        storedObject.uploadObject(data);
        return true;
    }

    public Map<String, Object> addObjectMetaData(String account, String containerName, String objectName, Map<String, Object> metadata) {
        Container container = getConnection().getContainer(containerName);
        if (!container.exists())
            return null;
        StoredObject storedObject = container.getObject(objectName);
        storedObject.setMetadata(metadata);
        storedObject.saveMetadata();
        return metadata;
    }

    public Map<String, Object> addObjectMetaData(String account, String containerName, String objectName, String key, String value) {
        Container container = getConnection().getContainer(containerName);
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

    public Map<String, Object> getMetaData(String account, String containerName, String objectName) {
        Container container = getConnection().getContainer(containerName);
        if (!container.exists())
            return null;
        StoredObject storedObject = container.getObject(objectName);
        return storedObject.getMetadata();
    }

    private Account getConnection() {
        if (account != null) {
            try {
                account.authenticate();
                return account;
            } catch (Exception e) {
                LOGGER.error("exception occured. Will create a new connection.", e);
            }
        } else {
            AccountConfig config = new AccountConfig();
            config.setUsername(userName);
            config.setPassword(password);
            config.setAuthUrl(authUrl);
            config.setAuthenticationMethod(AuthenticationMethod.BASIC);
            account = new AccountFactory(config).setAllowReauthenticate(true).createAccount();
        }
        return account;
    }
}
