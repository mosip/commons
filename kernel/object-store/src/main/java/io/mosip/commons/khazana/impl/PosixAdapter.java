package io.mosip.commons.khazana.impl;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;

import java.io.InputStream;
import java.util.Map;

public class PosixAdapter implements ObjectStoreAdapter {

    public InputStream getObject(String account, String container, String objectName) {
        return null;
    }

    public boolean exists(String account, String container, String objectName) {
        return false;
    }

    public boolean putObject(String account, String container, String objectName, InputStream data) {
        return false;
    }

    public Map<String, Object> addObjectMetaData(String account, String container, String objectName, Map<String, Object> metadata) {
        return null;
    }

    public Map<String, Object> addObjectMetaData(String account, String container, String objectName, String key, String value) {
        return null;
    }

    public Map<String, Object> getMetaData(String account, String container, String objectName) {
        return null;
    }
}
