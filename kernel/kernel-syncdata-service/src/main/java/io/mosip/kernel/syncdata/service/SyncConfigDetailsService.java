package io.mosip.kernel.syncdata.service;

import io.mosip.kernel.syncdata.dto.ConfigDto;
import io.mosip.kernel.syncdata.dto.PublicKeyResponse;
import net.minidev.json.JSONObject;

/**
 * Configuration Sync service
 * 
 * @author Srinivasan
 * @author Bal Vikash Sharma
 * @since 1.0.0
 *
 */
public interface SyncConfigDetailsService {
	/**
	 * This service will fetch all Configaration details available from server
	 * 
	 * @return JSONObject - config synced data
	 */
	public ConfigDto getConfigDetails();

	/**
	 * This service will fetch all Global Configaration details available from
	 * server
	 * 
	 * @return JSONObject - global config synced data
	 */
	public JSONObject getGlobalConfigDetails();

	/**
	 * This service will fetch all Registration center specific config details from
	 * server
	 *
	 * @param regId - registration Id
	 * @return JSONObject - registration center config synced data
	 */
	public JSONObject getRegistrationCenterConfigDetails(String regId);

	ConfigDto getConfiguration(String registrationCenterId);

	/**
	 * Function to get public key along with server active profile
	 * 
	 * @param applicationId applicationId
	 * @param timeStamp     timeStamp
	 * @param referenceId   referenceId
	 * @return {@link PublicKeyResponse} instance
	 */
	public PublicKeyResponse<String> getPublicKey(String applicationId, String timeStamp, String referenceId);

	/**
	 * This service will fetch all Configaration details available from server
	 * encrypt it based on machine key
	 *
	 * @return JSONObject - config synced data
	 */
	public ConfigDto getConfigDetails(String machineName);
}
