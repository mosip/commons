package io.mosip.kernel.clientcrypto.service.spi;


import io.mosip.kernel.clientcrypto.exception.ClientCryptoException;

import javax.validation.constraints.NotNull;

/**
 * @author Anusha Sunkada
 * @since 1.1.2
 */
public interface ClientCryptoService {


    /**
     * Signs the input data by private key provided
     *
     * @param dataToSign plain data to be signed
     * @return signature bytes
     */
    byte[] signData(@NotNull byte[] dataToSign) throws ClientCryptoException;


    /**
     * Validates the signed data against the actual data using the public part of underlying security module
     *
     * @param signature - signature to verify against
     * @param actualData - plain data
     * @return true if successful signature verification
     */
    boolean validateSignature(@NotNull byte[] signature, @NotNull byte[] actualData)
            throws ClientCryptoException;

    /**
     * Encrypts the input data
     *
     * @param plainData - plain data to encrypt
     * @return encrypted data
     */
    byte[] asymmetricEncrypt(@NotNull byte[] plainData) throws ClientCryptoException;

    /**
     * Decrypts provided cipher text
     *
     * @param cipher - encrypted data
     * @return plain data
     */
    byte[] asymmetricDecrypt(@NotNull byte[] cipher) throws ClientCryptoException;

    /**
     *
     *
     * @return public key as byte array
     */
    byte[] getSigningPublicPart();

    /**
     * Closes underlying security implementation
     */
    void closeSecurityInstance() throws ClientCryptoException;

    /**
     *
     * @return true if the implementation was specific to TPM
     */
    boolean isTPMInstance();

    /**
     *
     * @return public key as byte array
     */
    byte[] getEncryptionPublicPart();

}
