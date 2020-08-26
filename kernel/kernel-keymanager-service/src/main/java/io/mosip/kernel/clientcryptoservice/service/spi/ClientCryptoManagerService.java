package io.mosip.kernel.clientcryptoservice.service.spi;

/**
 * @since 1.2.0
 *
 */
public interface ClientCryptoManagerService {

    /**
     * Signs with TPM private key
     * @param data
     * @return signature bytes
     */
    public byte[] csSign(byte[] data);

    /**
     * Verify provided signature and data with TPM public key
     * @param data
     * @param signature
     * @return true only if signature is valid
     */
    public boolean csVerify(byte[] data, byte[] signature);

    /**
     * Encrypt data based on the provided refId
     * @param refId
     * @param data
     * @return cipher
     */
    public byte[] csEncrypt(String refId, byte[] data);

    /**
     * Decrypt cipher based on the provided refId
     * @param refId
     * @param cipher
     * @return plain data
     */
    public byte[] csDecrypt(String refId, byte[] cipher);

    /**
     * Encrypt data with TPM public key
     * @param data
     * @return cipher
     */
    public byte[] csEncrypt(byte[] data);

    /**
     * Decrypts cipher with TPM private key
     * @param cipher
     * @return plain data
     */
    public byte[] csDecrypt(byte[] cipher);

}
