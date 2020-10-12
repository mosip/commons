package io.mosip.kernel.clientcrypto.service.spi;

import io.mosip.kernel.clientcrypto.dto.*;

/**
 *
 * @author Anusha Sunkada
 * @since 1.1.2
 *
 */
public interface ClientCryptoManagerService {

    /**
     * Signs with TPM private key
     * @param tpmSignRequestDto
     * @return TpmSignResponseDto
     */
    public TpmSignResponseDto csSign(TpmSignRequestDto tpmSignRequestDto);

    /**
     * Verify provided signature and data with TPM public key
     * @param tpmSignVerifyRequestDto
     * @return TpmSignVerifyResponseDto
     */
    public TpmSignVerifyResponseDto csVerify(TpmSignVerifyRequestDto tpmSignVerifyRequestDto);


    /**
     * Encrypt data with TPM public key
     * @param tpmCryptoRequestDto
     * @return TpmCryptoResponseDto
     */
    public TpmCryptoResponseDto csEncrypt(TpmCryptoRequestDto tpmCryptoRequestDto);

    /**
     * Decrypts cipher with TPM private key
     * @param tpmCryptoRequestDto
     * @return TpmCryptoResponseDto
     */
    public TpmCryptoResponseDto csDecrypt(TpmCryptoRequestDto tpmCryptoRequestDto);

    /**
     * Returns TPM public key
     * @param publicKeyRequestDto
     * @return
     */
    public PublicKeyResponseDto getSigningPublicKey(PublicKeyRequestDto publicKeyRequestDto);

    /**
     * Returns TPM encryption public key
     * @param publicKeyRequestDto
     * @return
     */
    public PublicKeyResponseDto getEncPublicKey(PublicKeyRequestDto publicKeyRequestDto);

}
