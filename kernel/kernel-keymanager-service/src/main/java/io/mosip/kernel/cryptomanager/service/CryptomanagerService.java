/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.cryptomanager.service;

import org.springframework.stereotype.Service;

import io.mosip.kernel.cryptomanager.dto.CryptoWithPinRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptoWithPinResponseDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerResponseDto;

/**
 * This interface provides the methods which can be used for Encryption and
 * Decryption.
 *
 * @author Urvil Joshi
 * @author Srinivasan
 * @since 1.0.0
 */
@Service
public interface CryptomanagerService {

	/**
	 * Encrypt the data requested with metadata.
	 *
	 * @param cryptoRequestDto {@link CryptomanagerRequestDto} instance
	 * @return encrypted data
	 */
	public CryptomanagerResponseDto encrypt(CryptomanagerRequestDto cryptoRequestDto);

	/**
	 * Decrypt data requested with metadata.
	 *
	 * @param cryptoRequestDto {@link CryptomanagerRequestDto} instance
	 * @return decrypted data
	 */
	public CryptomanagerResponseDto decrypt(CryptomanagerRequestDto cryptoRequestDto);

	/**
	 * Encrypt the data requested with metadata.
	 *
	 * @param requestDto {@link CryptoWithPinRequestDto} instance
	 * @return encrypted data
	 */
	public CryptoWithPinResponseDto encryptWithPin(CryptoWithPinRequestDto requestDto);

	/**
	 * Decrypt data requested with metadata.
	 *
	 * @param requestDto {@link CryptoWithPinRequestDto} instance
	 * @return decrypted data
	 */
	public CryptoWithPinResponseDto decryptWithPin(CryptoWithPinRequestDto requestDto);
}
