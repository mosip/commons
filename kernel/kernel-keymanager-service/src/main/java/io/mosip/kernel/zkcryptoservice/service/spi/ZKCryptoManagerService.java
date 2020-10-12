package io.mosip.kernel.zkcryptoservice.service.spi;

import org.springframework.stereotype.Service;

import io.mosip.kernel.zkcryptoservice.dto.ReEncryptRandomKeyResponseDto;
import io.mosip.kernel.zkcryptoservice.dto.ZKCryptoRequestDto;
import io.mosip.kernel.zkcryptoservice.dto.ZKCryptoResponseDto;

/**
 * This interface provides the methods which can be used for Zero Knowledge Encryption and
 * Decryption.
 *
 * @author Mahammed Taheer
 * @since 1.1.2
 */
@Service
public interface ZKCryptoManagerService {
    
    /**
	 * Encrypt the data requested with metadata.
	 *
	 * @param cryptoRequestDto {@link ZKCryptoRequestDto} instance
	 * @return {@link ZKCryptoResponseDto} encrypted data
	 */
	public ZKCryptoResponseDto zkEncrypt(ZKCryptoRequestDto cryptoRequestDto);

	/**
	 * Decrypt data requested with metadata.
	 *
	 * @param cryptoRequestDto {@link ZKCryptoRequestDto} instance
	 * @return {@link ZKCryptoResponseDto} decrypted data
	 */
	public ZKCryptoResponseDto zkDecrypt(ZKCryptoRequestDto cryptoRequestDto);


	/**
	 * Re-Encrypt Random Key with the master key.
	 *
	 * @param encryptedKey encrypted random key
	 * @return {@link ReEncryptRandomKeyResponseDto} encrypted key
	 */
	public ReEncryptRandomKeyResponseDto zkReEncryptRandomKey(String encryptedKey);
}