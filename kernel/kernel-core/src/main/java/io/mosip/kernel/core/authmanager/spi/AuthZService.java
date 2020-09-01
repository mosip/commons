/**
 * 
 */
package io.mosip.kernel.core.authmanager.spi;

import io.mosip.kernel.core.authmanager.model.MosipUserTokenDto;

/**
 * @author Ramadurai Pandian
 *
 */
public interface AuthZService {

	MosipUserTokenDto validateToken(String token) throws Exception;

}
