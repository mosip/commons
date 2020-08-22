package io.mosip.commons.khazana.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.khazana.constant.KhazanaErrorCodes;
import io.mosip.commons.khazana.exception.FileNotFoundInDestinationException;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.kernel.core.util.FileUtils;

@Service
@Qualifier("PosixAdapter")
public class PosixAdapter implements ObjectStoreAdapter {

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftAdapter.class);

    @Value("${object.store.base.location:home}")
    private String baseLocation;

    private static final String ZIP = ".zip";
    private static final String JSON = ".json";

    public InputStream getObject(String account, String container, String objectName) {
        try {
            File accountLoc = new File(baseLocation + "/" + account);
            if (!accountLoc.exists())
                return null;
            File containerZip = new File(accountLoc.getPath() + "/" + container + ZIP);
            if (!containerZip.exists())
                throw new FileNotFoundInDestinationException(KhazanaErrorCodes.CONTAINER_NOT_PRESENT_IN_DESTINATION.getErrorCode(),
                        KhazanaErrorCodes.CONTAINER_NOT_PRESENT_IN_DESTINATION.getErrorMessage());

            InputStream ios = new FileInputStream(containerZip);
            Map<ZipEntry, ByteArrayOutputStream> entries = getAllExistingEntries(ios);

            Optional<ZipEntry> zipEntry = entries.keySet().stream().filter(e -> e.getName().contains(objectName + ZIP)).findAny();

            if (zipEntry.isPresent() && zipEntry.get() != null)
                return new ByteArrayInputStream(entries.get(zipEntry.get()).toByteArray());

        } catch (FileNotFoundInDestinationException e) {
            LOGGER.error("exception occured to get object for id - " + container, e);
        } catch (IOException e) {
            LOGGER.error("exception occured to get object for id - " + container, e);
        }
        return null;
    }

    public boolean exists(String account, String container, String objectName) {
        return getObject(account, container, objectName) != null;
    }

    public boolean putObject(String account, String container, String objectName, InputStream data) {
        try {
            createContainerZipWithSubpacket(account, container, objectName + ZIP, data);
            return true;
        } catch (Exception e) {
            LOGGER.error("exception occured. Will create a new connection.", e);
        }
        return false;
    }

    public Map<String, Object> addObjectMetaData(String account, String container, String objectName, Map<String, Object> metadata) {
        try {
            JSONObject jsonObject = objectMetadata(account, container, objectName, metadata);
            createContainerZipWithSubpacket(account, container, objectName + JSON,
                    new ByteArrayInputStream(jsonObject.toString().getBytes()));
        } catch (io.mosip.kernel.core.exception.IOException | IOException e) {
            LOGGER.error("exception occured to add metadata for id - " + container, e);
        }
        return metadata;
    }

    public Map<String, Object> addObjectMetaData(String account, String container, String objectName, String key, String value) {
        try {
            Map<String, Object> metaMap = new HashMap<>();
            metaMap.put(key, value);
            JSONObject jsonObject = objectMetadata(account, container, objectName, metaMap);
            createContainerZipWithSubpacket(account, container, objectName + JSON, new ByteArrayInputStream(jsonObject.toString().getBytes()));
            return metaMap;
        } catch (io.mosip.kernel.core.exception.IOException e) {
            LOGGER.error("exception occured to add metadata for id - " + container, e);
        } catch (IOException e) {
            LOGGER.error("exception occured to add metadata for id - " + container, e);
        }
        return null;
    }

    public Map<String, Object> getMetaData(String account, String container, String objectName) {
        Map<String, Object> metaMap = null;
        try {
            File accountLoc = new File(baseLocation + "/" + account);
            if (!accountLoc.exists())
                return null;
            File containerZip = new File(accountLoc.getPath() + "/" + container + ZIP);
            if (!containerZip.exists())
                throw new FileNotFoundInDestinationException(KhazanaErrorCodes.CONTAINER_NOT_PRESENT_IN_DESTINATION.getErrorCode(),
                        KhazanaErrorCodes.CONTAINER_NOT_PRESENT_IN_DESTINATION.getErrorMessage());

            InputStream ios = new FileInputStream(containerZip);
            Map<ZipEntry, ByteArrayOutputStream> entries = getAllExistingEntries(ios);

            Optional<ZipEntry> zipEntry = entries.keySet().stream().filter(e -> e.getName().contains(objectName + JSON)).findAny();

            if (zipEntry.isPresent() && zipEntry.get() != null) {
                String string = entries.get(zipEntry.get()).toString();
                JSONObject jsonObject = objectMapper.readValue(string, JSONObject.class);
                metaMap = objectMapper.readValue(jsonObject.toString(), HashMap.class);
            }
        } catch (FileNotFoundInDestinationException e) {
            LOGGER.error("exception occured. Will create a new connection.", e);
            throw e;
        } catch (IOException e) {
            LOGGER.error("exception occured to get metadata for id - " + container, e);
        }
        return metaMap;
    }

    private void createContainerZipWithSubpacket(String account, String container, String objectName, InputStream data) throws io.mosip.kernel.core.exception.IOException, IOException {
        File accountLocation = new File(baseLocation + "/" + account);
        if (!accountLocation.exists())
            accountLocation.mkdir();
        File containerZip = new File(accountLocation.getPath() + "/" + container + ZIP);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!containerZip.exists()) {
            try (ZipOutputStream packetZip = new ZipOutputStream(new BufferedOutputStream(out))) {
                addEntryToZip(String.format(objectName),
                        IOUtils.toByteArray(data), packetZip);
            }
        } else {
            InputStream ios = new FileInputStream(containerZip);
            Map<ZipEntry, ByteArrayOutputStream> entries = getAllExistingEntries(ios);
            try (ZipOutputStream packetZip = new ZipOutputStream(out)) {
                entries.entrySet().forEach(e -> {
                    try {
                        packetZip.putNextEntry(e.getKey());
                        packetZip.write(e.getValue().toByteArray());
                    } catch (IOException e1) {
                        LOGGER.error("exception occured. Will create a new connection.", e1);
                    }
                });
                addEntryToZip(String.format(objectName),
                        IOUtils.toByteArray(data), packetZip);
            }
        }

        FileUtils.copyToFile(new ByteArrayInputStream(out.toByteArray()), containerZip);
    }

    private void addEntryToZip(String fileName, byte[] data, ZipOutputStream zipOutputStream) {
        try {
            if (data != null) {
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(data);
            }
        } catch (IOException e) {
            LOGGER.error("exception occured. Will create a new connection.", e);
        }
    }

    private Map<ZipEntry, ByteArrayOutputStream> getAllExistingEntries(InputStream packetStream) throws IOException {
        Map<ZipEntry, ByteArrayOutputStream> entries = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(packetStream)) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                int len;
                byte[] buffer = new byte[2048];
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while ((len = zis.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                entries.put(ze, out);
                zis.closeEntry();
                ze = zis.getNextEntry();
                out.close();
            }
            zis.closeEntry();
        } finally {
            packetStream.close();
        }
        return entries;
    }

    private JSONObject objectMetadata(String account, String container,
                                      String objectName, Map<String, Object> metadata) {
        JSONObject jsonObject = new JSONObject(metadata);
        Map<String, Object> existingMetaData = getMetaData(account, container, objectName);
        if (!CollectionUtils.isEmpty(existingMetaData))
            existingMetaData.entrySet().forEach(entry -> {
                try {
                    jsonObject.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {
                    LOGGER.error("exception occured to add metadata for id - " + container, e);
                }
            });
        return jsonObject;
    }

	@Override
	public int incMetadata(String account, String container, String objectName, String metaDataKey) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int decMetadata(String account, String container, String objectName, String metaDataKey) {
		// TODO Auto-generated method stub
		return 0;
	}
}
