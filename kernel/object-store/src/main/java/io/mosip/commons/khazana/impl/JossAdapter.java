package io.mosip.commons.khazana.impl;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.commons.khazana.util.MockPacket;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.client.factory.AuthenticationMethod;
import org.javaswift.joss.command.shared.identity.tenant.Tenant;
import org.javaswift.joss.model.Account;
import org.javaswift.joss.model.Container;
import org.javaswift.joss.model.StoredObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
@Qualifier("JossAdapter")
public class JossAdapter implements ObjectStoreAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JossAdapter.class);



    @Value("object.store.username")
    private String userName;

    @Value("object.store.password")
    private String password;

    @Value("object.store.authurl")
    private String authUrl;

    private List<Account> accounts = new ArrayList<Account>();


    public InputStream getObject(String account, String containerName, String objectName) {
        //TODO ----- MOCK IMPL ------------- START
        if (true)
            return MockPacket.getMockPacket(containerName, objectName);
        //TODO ----- MOCK IMPL ------------- END

        Container container = getConnection(account).getContainer(containerName);
        if (!container.exists())
            container = getConnection(account).getContainer(containerName).create();
        return container.getObject(objectName).downloadObjectAsInputStream();
    }

    public boolean putObject(String account, String containerName, String objectName, InputStream data) {
        //TODO ----- MOCK IMPL ------------- START
        if (true)
            return MockPacket.putMock(containerName, objectName, data);
        //TODO ----- MOCK IMPL ------------- END

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
        Container container = getConnection(account).getContainer(containerName);
        if (!container.exists())
            return null;
        StoredObject storedObject = container.getObject(objectName);
        return storedObject.getMetadata();
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
        config.setAuthenticationMethod(AuthenticationMethod.BASIC);
        Account account = new AccountFactory(config).setAllowReauthenticate(true).createAccount();
        accounts.add(account);
        return account;
    }
}
