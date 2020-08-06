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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Qualifier("SwiftAdapter")
public class SwiftAdapter implements ObjectStoreAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftAdapter.class);


    @Value("object.store.username:test")
    private String userName;

    @Value("object.store.password:testing")
    private String password;

    @Value("object.store.authurl:52.172.53.239:9000/auth/v1.0")
    private String authUrl;

    private List<Account> accounts = new ArrayList<Account>();


    public InputStream getObject(String account, String containerName, String objectName) {
        Container container = getConnection(account).getContainer(containerName);
        if (!container.exists())
            container = getConnection(account).getContainer(containerName).create();
        return container.getObject(objectName).downloadObjectAsInputStream();
    }

    public boolean putObject(String account, String containerName, String objectName, InputStream data) {
        Container container = getConnection(account).getContainer(containerName);
        if (!container.exists())
            container = getConnection(account).getContainer(containerName).create();
        StoredObject storedObject = container.getObject(objectName);
        storedObject.uploadObject(data);

        return true;
    }

    public boolean exists(String account, String containerName, String objectName) {
        Container container = getConnection(account).getContainer(containerName);
        return container.exists() && container.getObject(objectName).exists();
    }

    public Map<String, Object> addObjectMetaData(String account, String containerName, String objectName, Map<String, Object> metadata) {

        Container container = getConnection(account).getContainer(containerName);
        if (!container.exists())
            return null;
        StoredObject storedObject = container.getObject(objectName);
        storedObject.setMetadata(metadata);
        storedObject.saveMetadata();
        return metadata;
    }

    public Map<String, Object> addObjectMetaData(String account, String containerName, String objectName, String key, String value) {
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

    public Map<String, Object> getMetaData(String account, String containerName, String objectName) {
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
        if (!accounts.isEmpty()) {
            try {
                Optional<Account> account = accounts.stream().filter(acc ->
                        acc.getTenants().getEnabledTenants().stream().map(t ->
                                t.name).collect(Collectors.toSet()).contains(accountName)).findAny();
                if (account.isPresent())
                    return account.get();
                else
                    throw new Exception("Could not find existing account. Will create new connection.");
            } catch (Exception e) {
                LOGGER.error("exception occured. Will create a new connection.", e);
            }
        }
        AccountConfig config = new AccountConfig();
        config.setUsername(userName);
        config.setPassword(password);
        config.setAuthUrl(authUrl);
        config.setTenantName(accountName);
        //config.setMock(true);
        config.setAuthenticationMethod(AuthenticationMethod.BASIC);
        Account account = new AccountFactory(config).setAllowReauthenticate(true).createAccount();
        accounts.add(account);
        return account;
    }
}
