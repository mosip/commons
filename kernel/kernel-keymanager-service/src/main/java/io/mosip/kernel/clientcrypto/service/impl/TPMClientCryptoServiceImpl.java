package io.mosip.kernel.clientcrypto.service.impl;

import io.mosip.kernel.clientcrypto.constant.ClientCryptoErrorConstants;
import io.mosip.kernel.clientcrypto.constant.ClientCryptoManagerConstant;
import io.mosip.kernel.clientcrypto.exception.ClientCryptoException;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoService;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import org.junit.Assert;
import tss.*;
import tss.tpm.CreatePrimaryResponse;
import tss.tpm.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


/**
 * TPM is Strong and secure.
 * <p>
 * Strong - key is derived from true random source and large key space.
 * <p>
 * Secure - The private key material never leaves the TPM secure boundary in plain form.
 * When a key leaves the TPM - in order to be loaded and used later - it is wrapped (encrypted) by its parent key.
 * Keys, therefore, form a tree: each key is wrapped by its parent, all the way to the root of the tree,
 * where the primary key is derived from a fixed seed. The seed is stored in the TPM's NVDATA, under a reserved index,
 * and cannot be read externally.
 * <p>
 * The TPM stores keys on one of four hierarchies: *
 * 1. Endorsement hierarchy.
 * 2. Platform hierarchy.
 * 3. Owner hierarchy, also known as storage hierarchy.
 * 4. Null hierarchy.
 * <p>
 * A hierarchy is a logical collection of entities: keys and nv data blobs. Each hierarchy has a different seed and
 * different authorization policy. Hierarchies differ by when their seeds are created and by who certifies their primary keys.
 * Generally speaking, the endorsement hierarchy is reserved for objects created and certified by the TPM manufacturer.
 * The endorsement seed (eseed) is randomly generated at manufacturing time and never changes during the lifetime of the device.
 * The primary endorsement key is certified by the TPM manufacturer, and because its seed never changes,
 * it can be used to identify the device. Since there's only one TPM device per machine, the primary endorsement key can
 * also be used as the machine's identity
 *
 * Primary keys are derived from the primary seeds using a deterministic key derivation function (KDF). More accurately,
 * the KDF takes as input the fixed seed and the key's template that describes its properties.
 *
 *
 * @author Balaji Sridharan
 * @author Anusha Sunkada
 * @since 1.1.2
 */
class TPMClientCryptoServiceImpl implements ClientCryptoService {

    private static final Logger LOGGER = KeymanagerLogger.getLogger(TPMClientCryptoServiceImpl.class);
    private static final byte[] NULL_VECTOR = new byte[0];

    //Zero terminated string - RSA encoding params
    private static byte[] label = Helpers.concatenate(Charset.forName("UTF-8").encode(new String(NULL_VECTOR)).array(),
            new byte[] { 0 });

    //Note: TPM is single threaded
    private static Tpm tpm;
    private static CreatePrimaryResponse signingPrimaryResponse;
    private static CreatePrimaryResponse encPrimaryResponse;

    TPMClientCryptoServiceImpl() throws Throwable {
        LOGGER.debug(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                ClientCryptoManagerConstant.EMPTY, "TPMClientCryptoServiceImpl constructor invoked");

        if (tpm == null) {
            LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                    ClientCryptoManagerConstant.EMPTY, "Instantiating Platform TPM");

            tpm = TpmFactory.platformTpm();
            if( !isKernelModeTRM() ) { //checks if its not connected to software TPM
                LOGGER.warn(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                        ClientCryptoManagerConstant.EMPTY, "UNABLE TO CONNECT TO KERNEL/SYSTEM TPM RESOURCE MANAGER");
                tpm = null;
            }

            LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                    ClientCryptoManagerConstant.EMPTY, "Completed getting the instance of Platform TPM");
        }
    }

    @Override
    public byte[] signData(byte[] dataToSign) throws ClientCryptoException {
        try {
            Assert.assertNotNull(tpm);
            CreatePrimaryResponse signingKey = createSigningKey();
            TPMU_SIGNATURE signedData = null;
            synchronized(tpm) {
                signedData = tpm.Sign(signingKey.handle,
                        TPMT_HA.fromHashOf(TPM_ALG_ID.SHA256, dataToSign).digest, new TPMS_NULL_SIG_SCHEME(),
                        TPMT_TK_HASHCHECK.nullTicket());
            }
            Assert.assertNotNull(signedData);
            LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                    ClientCryptoManagerConstant.EMPTY, "Completed Signing data using TPM");
            return ((TPMS_SIGNATURE_RSASSA) signedData).sig;
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    @Override
    public boolean validateSignature(byte[] signature, byte[] actualData) throws ClientCryptoException {
        return validateSignature(getSigningPublicPart(), signature, actualData);
    }

    @Override
    public byte[] asymmetricEncrypt(byte[] plainData)  throws ClientCryptoException{
        try {
            CreatePrimaryResponse primaryResponse = createRSAKey();
            return asymmetricEncrypt(primaryResponse.outPublic.toTpm(), plainData);
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    @Override
    public byte[] asymmetricDecrypt(byte[] dataToDecrypt)  throws ClientCryptoException{
        try {
            Assert.assertNotNull(tpm);
            CreatePrimaryResponse primaryResponse = createRSAKey();

            synchronized (tpm) {
                return tpm.RSA_Decrypt(primaryResponse.handle, dataToDecrypt, new TPMS_NULL_ASYM_SCHEME(),
                        label);
            }
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    @Override
    public byte[] getSigningPublicPart()  throws ClientCryptoException{
        try {
            return createSigningKey().outPublic.toTpm();
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }

    @Override
    public synchronized void closeSecurityInstance() {
        try {
            if (tpm != null)
                tpm.close();
        } catch (IOException e) {
            LOGGER.error(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                    ClientCryptoManagerConstant.EMPTY, ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * single call can generate at most 48 bytes,
     * the size of the largest hash digest implemented by the TPM (TPM2_ALG_SHA384 in this case)
     *
     * @param length
     * @return
     */
    public synchronized static byte[] generateRandomBytes(int length) {
        return tpm.GetRandom(length);
    }

    @Override
    public byte[] getEncryptionPublicPart() {
        try {
            return createRSAKey().outPublic.toTpm();
        } catch (Exception ex) {
            throw new ClientCryptoException(ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorCode(),
                    ClientCryptoErrorConstants.CRYPTO_FAILED.getErrorMessage(), ex);
        }
    }


    public static boolean validateSignature(byte[] publicKey, byte[] signature, byte[] actualData)
            throws ClientCryptoException {
        TPMT_PUBLIC tpmPublic = TPMT_PUBLIC.fromTpm(publicKey);
        // Create Signature from signed data and algorithm
        TPMU_SIGNATURE rsaSignature = new TPMS_SIGNATURE_RSASSA(TPM_ALG_ID.SHA256, signature);
        // Validate the Signature using Public Template
        return tpmPublic.validateSignature(actualData, rsaSignature);
    }


    public static byte[] asymmetricEncrypt(byte[] publicKey, byte[] dataToEncrypt) throws ClientCryptoException {
        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                ClientCryptoManagerConstant.EMPTY, "TpmClientSecurity Asymmetric encrypt");
        TPMT_PUBLIC tpmPublic = TPMT_PUBLIC.fromTpm(publicKey);
        return tpmPublic.encrypt(dataToEncrypt, new String(NULL_VECTOR));
    }

    @Override
    public boolean isTPMInstance() {
        return true;
    }


    /**
     * Note: If either the seed or the template changes, a completely different primary key is created
     *
     * @return
     */
    private CreatePrimaryResponse createSigningKey() {
        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                ClientCryptoManagerConstant.EMPTY, "Creating the Key from Platform TPM");

        if(signingPrimaryResponse != null)
            return signingPrimaryResponse;

        TPMT_PUBLIC template = new TPMT_PUBLIC(TPM_ALG_ID.SHA1,
                new TPMA_OBJECT(TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent, TPMA_OBJECT.sign,
                        TPMA_OBJECT.sensitiveDataOrigin, TPMA_OBJECT.userWithAuth),
                new byte[0],
                new TPMS_RSA_PARMS(new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.NULL, 0, TPM_ALG_ID.NULL),
                        new TPMS_SIG_SCHEME_RSASSA(TPM_ALG_ID.SHA256), 2048, 65537),
                new TPM2B_PUBLIC_KEY_RSA());
        TPM_HANDLE primaryHandle = TPM_HANDLE.from(TPM_RH.ENDORSEMENT);
        TPMS_SENSITIVE_CREATE dataToBeSealedWithAuth = new TPMS_SENSITIVE_CREATE(NULL_VECTOR, NULL_VECTOR);

        synchronized (tpm) {
            //everytime this is called key never changes until unless either seed / template change.
            signingPrimaryResponse = tpm.CreatePrimary(primaryHandle, dataToBeSealedWithAuth, template,
                    NULL_VECTOR, new TPMS_PCR_SELECTION[0]);
        }

        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                ClientCryptoManagerConstant.EMPTY, "Completed creating the Signing Key from Platform TPM");
        return signingPrimaryResponse;
    }

    /**
     * Note: If either the seed or the template changes, a completely different primary key is created
     * @return
     */
    private CreatePrimaryResponse createRSAKey() {
        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                ClientCryptoManagerConstant.EMPTY, "Getting Asymmetric Key Creation from tpm");

        if(encPrimaryResponse != null)
            return encPrimaryResponse;

        LocalDateTime localDateTime = LocalDateTime.now();
        // This policy is a "standard" policy that is used with vendor-provided
        // EKs
        byte[] standardEKPolicy = new byte[] { (byte) 0x83, 0x71, (byte) 0x97, 0x67, 0x44, (byte) 0x84, (byte) 0xb3,
                (byte) 0xf8, 0x1a, (byte) 0x90, (byte) 0xcc, (byte) 0x8d, 0x46, (byte) 0xa5, (byte) 0xd7, 0x24,
                (byte) 0xfd, 0x52, (byte) 0xd7, 0x6e, 0x06, 0x52, 0x0b, 0x64, (byte) 0xf2, (byte) 0xa1, (byte) 0xda,
                0x1b, 0x33, 0x14, 0x69, (byte) 0xaa };

        TPMT_PUBLIC template = new TPMT_PUBLIC(TPM_ALG_ID.SHA256,
                new TPMA_OBJECT(TPMA_OBJECT.fixedTPM, TPMA_OBJECT.fixedParent,
                        TPMA_OBJECT.decrypt, TPMA_OBJECT.sensitiveDataOrigin, TPMA_OBJECT.userWithAuth),
                standardEKPolicy,
                new TPMS_RSA_PARMS(new TPMT_SYM_DEF_OBJECT(TPM_ALG_ID.NULL, 0, TPM_ALG_ID.NULL),
                        new TPMS_ENC_SCHEME_OAEP(TPM_ALG_ID.SHA256), 2048, 65537),
                new TPM2B_PUBLIC_KEY_RSA());
        TPMS_SENSITIVE_CREATE dataToBeSealedWithAuth = new TPMS_SENSITIVE_CREATE(NULL_VECTOR, NULL_VECTOR);
        TPM_HANDLE primaryHandle = TPM_HANDLE.from(TPM_RH.ENDORSEMENT);

        synchronized (tpm) {
            encPrimaryResponse = tpm.CreatePrimary(primaryHandle, dataToBeSealedWithAuth, template,
                    null, null);
        }

        long secondsTaken = localDateTime.until(LocalDateTime.now(), ChronoUnit.SECONDS);
        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                ClientCryptoManagerConstant.EMPTY,
                String.format("Completed Asymmetric Key Creation using tpm. Time taken is %s seconds",
                        String.valueOf(secondsTaken)));
        return encPrimaryResponse;
    }

    private static SecretKey getSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.TPM,
                    ClientCryptoManagerConstant.EMPTY, "Failed to generate secret key " + ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    /**
     * check if connected to kernel/system mode TPM resource manager
     * @return
     */
    private boolean isKernelModeTRM() {
        synchronized (tpm) {
            if(tpm != null && tpm._getDevice() != null &&
                    (tpm._getDevice() instanceof TpmDeviceTbs || tpm._getDevice() instanceof TpmDeviceLinux)) {
                return true;
            }
        }
        return false;
    }
}
