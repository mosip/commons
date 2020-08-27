/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.repository;

import java.util.Map;

/**
 * @author Ramadurai Pandian
 *
 */
public interface UserStoreFactory {

	Map<String, DataStore> getUserStores();

	DataStore getDataStoreBasedOnApp(String appId);
}
