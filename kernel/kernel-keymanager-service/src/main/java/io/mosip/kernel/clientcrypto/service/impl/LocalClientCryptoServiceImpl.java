package io.mosip.kernel.clientcrypto.service.impl;

import io.mosip.kernel.clientcrypto.constant.ClientCryptoErrorConstants;
import io.mosip.kernel.clientcrypto.constant.ClientCryptoManagerConstant;
import io.mosip.kernel.clientcrypto.exception.ClientCryptoException;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoService;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.zkcryptoservice.constant.ZKCryptoManagerConstants;
import org.apache.commons.io.FileUtils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Objects;

/**
 * This is TPM Fallback implementation,
 * Note: This implementation must not be supported in PROD environments.
 *
 * @author Anusha Sunkada
 * @since 1.1.2
 *
 */
class LocalClientCryptoServiceImpl implements ClientCryptoService {

    private static final Logger LOGGER = KeymanagerLogger.getLogger(LocalClientCryptoServiceImpl.class);
    private static final String ALGORITHM = "RSA";
    private static final int KEY_LENGTH = 2048;
    private static final String SIGN_ALGORITHM = "SHA256withRSA";
    private static final String PRIVATE_KEY = "reg.key";
    private static final String PUBLIC_KEY = "reg.pub";
    private static final String README = "readme.txt";

    private static SecureRandom secureRandom = null;
    protected static CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;


    /**
     * Creates RSA Key pair under user's home directory and the same is used for further
     * crypto operations.
     * @throws Throwable
     */
    LocalClientCryptoServiceImpl(@NotNull CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCoreImpl)
            throws  Throwable {
        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.NON_TPM,
                ClientCryptoManagerConstant.EMPTY, "Getting the instance of NON_TPM Security");

        backwardCompatibilityFix();
        if(!doesKeysExists()) {
            setupKeysDir();
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyGenerator.initialize(KEY_LENGTH, new SecureRandom());
            KeyPair keypair = keyGenerator.generateKeyPair();
            createKeyFile(PRIVATE_KEY, keypair.getPrivate().getEncoded());
            createKeyFile(PUBLIC_KEY, keypair.getPublic().getEncoded());
            createReadMe(keypair.getPublic());

            LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.NON_TPM,
                    ClientCryptoManagerConstant.EMPTY, "TPM NOT AVAILABLE - GENERATED NEW KEY PAIR SUCCESSFULLY.");

        }
        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.NON_TPM,
                ClientCryptoManagerConstant.EMPTY, "Completed initializing Local Security Impl");

        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.NON_TPM,
                ClientCryptoManagerConstant.EMPTY, "Check this file for publicKey and KeyIndex : "
                        + getKeysDirPath() + File.separator + README);

        //set cryptoCore
        cryptoCore = cryptoCoreImpl;
    }

    @Override
    public byte[] signData(@NotNull byte[] dataToSign) throws ClientCryptoException {
        try {
            Signature sign = Signature.getInstance(SIGN_ALGORITHM);
            sign.initSign(getPrivateKey());

            try(ByteArrayInputStream in = new ByteArrayInputStream(dataToSign)) {
                byte[] buffer = new byte[2048];
                int len = 0;
                while((len = in.read(buffer)) != -1) {
                    sign.update(buffer, 0, len);
                }
                return sign.sign();
            }
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    @Override
    public boolean validateSignature(@NotNull byte[] signature, @NotNull byte[] actualData)
            throws ClientCryptoException{
        try {
           return validateSignature(getPublicKey(),signature, actualData);
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    @Override
    public byte[] asymmetricEncrypt(@NotNull byte[] dataToEncrypt) throws ClientCryptoException{
        try {
            return cryptoCore.asymmetricEncrypt(getPublicKey(), dataToEncrypt);
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    @Override
    public byte[] asymmetricDecrypt(@NotNull byte[] dataToDecrypt) throws ClientCryptoException{
        try {
            return cryptoCore.asymmetricDecrypt(getPrivateKey(), dataToDecrypt);
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    @Override
    public byte[] getSigningPublicPart() throws ClientCryptoException {
        try {
            return getPublicKey().getEncoded();
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    @Override
    public void closeSecurityInstance() throws ClientCryptoException {
        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.NON_TPM,
                ClientCryptoManagerConstant.EMPTY, "Nothing to do, as Local NON-TPM Security Impl is in use");
    }

    @Override
    public boolean isTPMInstance() {
        return false;
    }

    public static byte[] generateRandomBytes(int length) {
        if(secureRandom == null)
            secureRandom = new SecureRandom();

        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    @Override
    public byte[] getEncryptionPublicPart() {
        try {
            return getPublicKey().getEncoded();
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    public static boolean validateSignature(@NotNull byte[] publicKey, @NotNull byte[] signature, @NotNull byte[] actualData) throws ClientCryptoException {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
            return validateSignature(kf.generatePublic(keySpec), signature, actualData);
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    public static byte[] asymmetricEncrypt(byte[] publicKey, byte[] dataToEncrypt) throws ClientCryptoException {
        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.NON_TPM,
                ClientCryptoManagerConstant.EMPTY, "LocalClientSecurity Asymmetric encrypt");
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
            return cryptoCore.asymmetricEncrypt(kf.generatePublic(keySpec), dataToEncrypt);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    private static boolean validateSignature(PublicKey publicKey, byte[] signature, byte[] actualData)
            throws ClientCryptoException {
        try {
            Signature sign = Signature.getInstance(SIGN_ALGORITHM);
            sign.initVerify(publicKey);

            try(ByteArrayInputStream in = new ByteArrayInputStream(actualData)) {
                byte[] buffer = new byte[2048];
                int len = 0;

                while((len = in.read(buffer)) != -1) {
                    sign.update(buffer, 0, len);
                }
                return sign.verify(signature);
            }
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    private void setupKeysDir() {
        File keysDir = new File(getKeysDirPath());
        keysDir.mkdirs();
    }

    private boolean doesKeysExists() {
        File keysDir = new File(getKeysDirPath());
        return (keysDir.exists() && Objects.requireNonNull(keysDir.list()).length >= 2);
    }

    //Copy ${user.home}/.mosipkeys/db.conf to ${user.dir}/.mosipkeys/db.conf
    private void backwardCompatibilityFix() {
        Path targetPrivateKey = Paths.get(ClientCryptoManagerConstant.KEY_PATH, ClientCryptoManagerConstant.KEYS_DIR, PRIVATE_KEY);
        if(targetPrivateKey.toFile().exists()) {
            LOGGER.info("Backward compatibility fix not applicable");
            return;
        }

        Path target = Paths.get(ClientCryptoManagerConstant.KEY_PATH, ClientCryptoManagerConstant.KEYS_DIR);
        File existingKeysDir = new File(System.getProperty("user.home") + File.separator + ClientCryptoManagerConstant.KEYS_DIR);
        if(existingKeysDir.exists() && Objects.requireNonNull(existingKeysDir.list()).length >= 2) {
            try {
                FileUtils.copyDirectory(existingKeysDir, target.toFile());
                LOGGER.info("Successfully performed backward compatible fix. Copied {} to {}",
                        existingKeysDir, target);
            } catch (IOException e) {
                LOGGER.error("Failed to perform backward compatible fix. Failed to copy {} to {} due to {}",
                        existingKeysDir, target, e);
            }
        }
    }

    private String getKeysDirPath() {
        return ClientCryptoManagerConstant.KEY_PATH + File.separator + ClientCryptoManagerConstant.KEYS_DIR;
    }

    private void createKeyFile(String fileName, byte[] key) throws IOException {
        try(FileOutputStream os =
                    new FileOutputStream(getKeysDirPath() + File.separator + fileName)) {
            os.write(key);
        }
    }

    private PrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] key = Files.readAllBytes(Paths.get(getKeysDirPath() + File.separator + PRIVATE_KEY));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
        return kf.generatePrivate(keySpec);
    }

    private PublicKey getPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] key = Files.readAllBytes(Paths.get(getKeysDirPath() + File.separator + PUBLIC_KEY));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
        KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
        return kf.generatePublic(keySpec);
    }



    private void createReadMe(PublicKey publicKey) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("MachineName: ");
        builder.append(InetAddress.getLocalHost().getHostName().toLowerCase());
        builder.append("\r\n");
        builder.append("PublicKey: ");
        builder.append(CryptoUtil.encodeBase64String(publicKey.getEncoded()));
        builder.append("\r\n");
        builder.append("KeyIndex: ");
        builder.append(CryptoUtil.computeFingerPrint(publicKey.getEncoded(), null).toLowerCase());
        builder.append("\r\n");
        builder.append("Note : Use the above public key and client/machine name to create client machine using admin API");
        builder.append("\r\n");
        builder.append("Note : If the keys are lost/deleted, keys are regenerated on next instantiation of this instance. Corresponding client mappings need to be recreated once again.");
        builder.append("\r\n");

        Files.write(Paths.get(getKeysDirPath() + File.separator + README),
                builder.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE);
    }
}
