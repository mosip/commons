package io.mosip.kernel.keymigrate.service.spi;

import io.mosip.kernel.keymigrate.dto.KeyMigrateBaseKeyRequestDto;
import io.mosip.kernel.keymigrate.dto.KeyMigrateBaseKeyResponseDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateCertficateResponseDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateRequestDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateResponseDto;

/**
 * This interface provides the methods which can be used for Key Migration from source HSM to
 *  destination HSM.
 *
 * @author Mahammed Taheer
 * @since 1.1.6
 */
public interface KeyMigratorService {

    /**
	 * Key Migrate request with key data.
	 *
	 * @param keyMigrateRequest {@link KeyMigrateBaseKeyRequestDto} instance
	 * @return {@link KeyMigrateBaseKeyResponseDto} migrate status
	 */
	public KeyMigrateBaseKeyResponseDto migrateBaseKey(KeyMigrateBaseKeyRequestDto keyMigrateRequest);

	/**
	 * ZK Keys Migrate request for temporary certificate.
	 *
	 * 
	 * @return {@link ZKKeyMigrateCertficateResponseDto} certificate response
	 */
	public ZKKeyMigrateCertficateResponseDto getZKTempCertificate();
    
	/**
	 * ZK Keys Migrate request to migrate keys.
	 *
	 * @param migrateZKKeysRequestDto {@link ZKKeyMigrateRequestDto} instance
	 * @return {@link ZKKeyMigrateResponseDto} migrate status
	 */
	public ZKKeyMigrateResponseDto migrateZKKeys(ZKKeyMigrateRequestDto migrateZKKeysRequestDto);
}
