package io.mosip.commons.mstore;

import java.io.InputStream;

/**
 * The commons mstore store keeper.
 *
 */
public class StoreKeeper {

    /**
     * This method stores data into persistence storage and returns the key.
     *
     * @param data : data to be stored in persistence storage
     * @return String : generated key
     */
    public String store(InputStream data) {
        return null;
    }

    /**
     * Generate URL based on key
     *
     * @param key : input key
     * @return String : URL
     */
    public String generateUrl(String key) {
        return null;
    }

    /**
     * Save key and data into persistence storage
     *
     * @param key : input key
     * @param data : input data
     * @return boolean : success/failure
     */
    public boolean put(String key, InputStream data) {
        return false;
    }

    /**
     * Get data from persistence storage by key
     *
     * @param key : input key
     * @return InputStream : data
     */
    public InputStream fetch(String key) {
        return null;
    }

    public InputStream get(String url) {
        // get key from url
        return null;
    }

}
