package io.mosip.kernel.clientcrypto.service.impl;

import io.mosip.kernel.clientcrypto.constant.ClientCryptoErrorConstants;
import io.mosip.kernel.clientcrypto.constant.ClientCryptoManagerConstant;
import io.mosip.kernel.clientcrypto.exception.ClientCryptoException;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoService;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;

@Component
public class ClientCryptoFacade {

    //we are using 2048 bit RSA key
    private static final int ENC_SYM_KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;
    private static final int AAD_LENGTH = 12;
    private static final Logger LOGGER = KeymanagerLogger.getLogger(ClientCryptoFacade.class);
    private static ClientCryptoService clientCryptoService = null;
    private static Boolean isTPMRequired = null;
    private static SecureRandom secureRandom = null;

    @Autowired
    private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

    @Autowired
    private Environment environment;

    public static void setIsTPMRequired(boolean flag) {
        if(isTPMRequired == null) { isTPMRequired = flag; }
    }

    private void initializeClientSecurity() {
        LOGGER.debug(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                ClientCryptoManagerConstant.EMPTY, "initializeClientSecurity >>> started");

        if(isTPMRequired) {
            try {
                clientCryptoService = new TPMClientCryptoServiceImpl();
            } catch(Throwable e) {
                LOGGER.debug(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                        ClientCryptoManagerConstant.EMPTY, ExceptionUtils.getStackTrace(e));
            }
        }
        else {
            try {
                clientCryptoService = new LocalClientCryptoServiceImpl(cryptoCore);
            } catch (Throwable ex) {
                LOGGER.error(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                        ClientCryptoManagerConstant.EMPTY, ExceptionUtils.getStackTrace(ex));
            }
        }

        if(clientCryptoService == null) {
            LOGGER.error(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                    ClientCryptoManagerConstant.EMPTY, "Failed to get client security instance.");
            throw new ClientCryptoException(ClientCryptoErrorConstants.INITIALIZATION_ERROR.getErrorCode(),
                    ClientCryptoErrorConstants.INITIALIZATION_ERROR.getErrorMessage());
        }

        LOGGER.debug(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                ClientCryptoManagerConstant.EMPTY, "initializeClientSecurity >>> Completed");
    }

    public ClientCryptoService getClientSecurity() {
        if(isTPMRequired == null)
            throw new ClientCryptoException(ClientCryptoErrorConstants.TPM_REQUIRED_FLAG_NOT_SET.getErrorCode(),
                    ClientCryptoErrorConstants.TPM_REQUIRED_FLAG_NOT_SET.getErrorMessage());

        if(clientCryptoService == null) {
            initializeClientSecurity();
        }
        return clientCryptoService;
    }

    public boolean validateSignature(byte[] publicKey, byte[] signature, byte[] actualData, boolean isTPM) {
        if(!isTPM) {
            return LocalClientCryptoServiceImpl.validateSignature(publicKey, signature, actualData);
        }
        return TPMClientCryptoServiceImpl.validateSignature(publicKey, signature, actualData);
    }

    public byte[] encrypt(byte[] publicKey, byte[] dataToEncrypt, boolean isTPM) {
        SecretKey secretKey = getSecretKey();
        byte[] iv = generateRandomBytes(IV_LENGTH);
        byte[] aad = generateRandomBytes(AAD_LENGTH);
        byte[] cipher = cryptoCore.symmetricEncrypt(secretKey, dataToEncrypt, iv, aad);

        LocalClientCryptoServiceImpl.cryptoCore = this.cryptoCore;

        byte[] encryptedSecretKey = isTPM ? TPMClientCryptoServiceImpl.asymmetricEncrypt(publicKey, secretKey.getEncoded()) :
                LocalClientCryptoServiceImpl.asymmetricEncrypt(publicKey, secretKey.getEncoded());

        byte[] processedData = new byte[cipher.length+encryptedSecretKey.length+iv.length+aad.length];
        System.arraycopy(encryptedSecretKey,0,processedData, 0, encryptedSecretKey.length);
        System.arraycopy(iv, 0, processedData, encryptedSecretKey.length, iv.length);
        System.arraycopy(aad, 0, processedData, encryptedSecretKey.length + iv.length, aad.length);
        System.arraycopy(cipher, 0, processedData, encryptedSecretKey.length + iv.length + aad.length, cipher.length);
        return processedData;
    }

    public byte[] decrypt(byte[] dataToDecrypt) {
        Assert.assertNotNull(getClientSecurity());
        byte[] encryptedSecretKey = Arrays.copyOfRange(dataToDecrypt, 0, ENC_SYM_KEY_LENGTH);
        byte[] secretKeyBytes = getClientSecurity().asymmetricDecrypt(encryptedSecretKey);
        byte[] iv = Arrays.copyOfRange(dataToDecrypt, ENC_SYM_KEY_LENGTH, ENC_SYM_KEY_LENGTH+IV_LENGTH);
        byte[] aad = Arrays.copyOfRange(dataToDecrypt, ENC_SYM_KEY_LENGTH + IV_LENGTH, ENC_SYM_KEY_LENGTH+IV_LENGTH+AAD_LENGTH);
        byte[] cipher = Arrays.copyOfRange(dataToDecrypt, ENC_SYM_KEY_LENGTH + IV_LENGTH + AAD_LENGTH,
                dataToDecrypt.length);

        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
        return cryptoCore.symmetricDecrypt(secretKey, cipher, iv, aad);
    }

    public static byte[] generateRandomBytes(int length) {
        if(secureRandom == null)
            secureRandom = new SecureRandom();

        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    private static SecretKey getSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.info(ClientCryptoManagerConstant.SESSIONID, "Client Security FACADE",
                    ClientCryptoManagerConstant.EMPTY, "Failed to generate secret key " + ExceptionUtils.getStackTrace(e));
        }
        return null;
    }
}
