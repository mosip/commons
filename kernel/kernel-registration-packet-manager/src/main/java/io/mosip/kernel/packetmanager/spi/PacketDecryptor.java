package io.mosip.kernel.packetmanager.spi;


import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import io.mosip.kernel.packetmanager.exception.PacketDecryptionFailureException;

import java.io.InputStream;


/**
 * The Interface Decryptor.
 * 
 * @author Sowmya
 */
public interface PacketDecryptor {

	/**
	 * This Method provide the functionality to decrypt packet.
	 *
	 * @param input          encrypted packet to be decrypted
	 * @param registrationId the registration id
	 * @return decrypted packet
	 * @throws PacketDecryptionFailureException if error occured while decrypting
	 * @throws ApiNotAccessibleException      if error occured while
	 */
	public InputStream decrypt(InputStream input, String registrationId)
			throws PacketDecryptionFailureException, ApiNotAccessibleException;

}
