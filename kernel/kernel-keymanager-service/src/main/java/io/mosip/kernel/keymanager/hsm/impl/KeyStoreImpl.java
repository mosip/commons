package io.mosip.kernel.keymanager.hsm.impl;

import java.lang.reflect.Constructor;
import java.security.Key;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.keymanager.exception.KeystoreProcessingException;
import io.mosip.kernel.core.keymanager.model.CertificateParameters;
import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.keymanager.hsm.constant.KeymanagerConstant;
import io.mosip.kernel.keymanager.hsm.constant.KeymanagerErrorCode;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;


/**
 * HSM Keymanager implementation based on OpenDNSSEC that handles and stores
 * its cryptographic keys via the PKCS#11 interface. This is a software
 * implementation of a generic cryptographic device. SoftHSM can work with other
 * cryptographic device because of the PKCS#11 interface.
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@ConfigurationProperties(prefix = "mosip.kernel.keymanager.hsm")
@Component
public class KeyStoreImpl implements KeyStore, InitializingBean {

	private static final Logger LOGGER = KeymanagerLogger.getLogger(KeyStoreImpl.class);

	private static final Map<String, String> DEFAULT_KS_IMPL_CLASSES = new HashMap<>();

	static {
		DEFAULT_KS_IMPL_CLASSES.put(KeymanagerConstant.KEYSTORE_TYPE_PKCS11, KeymanagerConstant.PKCS11_KS_IMPL_CLAZZ);
		DEFAULT_KS_IMPL_CLASSES.put(KeymanagerConstant.KEYSTORE_TYPE_PKCS12, KeymanagerConstant.PKCS12_KS_IMPL_CLAZZ);
		DEFAULT_KS_IMPL_CLASSES.put(KeymanagerConstant.KEYSTORE_TYPE_OFFLINE, KeymanagerConstant.OFFLINE_KS_IMPL_CLAZZ);
	}

	/**
	 * The type of keystore, e.g. PKCS11, PKCS12, JCE
	 */
	@Value("${mosip.kernel.keymanager.hsm.keystore-type:PKCS11}")
	private String keystoreType;

	/**
	 * Path of HSM PKCS11 config file or the Keystore in caes of bouncy castle
	 * provider
	 */
	@Value("${mosip.kernel.keymanager.hsm.config-path:\"\"}")
	private String configPath;

	/**
	 * The passkey for Keystore
	 */
	@Value("${mosip.kernel.keymanager.hsm.keystore-pass:\"\"}")
	private String keystorePass;

	/**
	 * Symmetric key algorithm Name
	 */
	@Value("${mosip.kernel.keygenerator.symmetric-algorithm-name:AES}")
	private String symmetricKeyAlgorithm;

	/**
	 * Symmetric key length
	 */
	@Value("${mosip.kernel.keygenerator.symmetric-key-length:256}")
	private int symmetricKeyLength;

	/**
	 * Asymmetric key algorithm Name
	 */
	@Value("${mosip.kernel.keygenerator.asymmetric-algorithm-name:RSA}")
	private String asymmetricKeyAlgorithm;

	/**
	 * Asymmetric key length
	 */
	@Value("${mosip.kernel.keygenerator.asymmetric-key-length:2048}")
	private int asymmetricKeyLength;

	/**
	 * Certificate Signing Algorithm
	 * 
	 */
	@Value("${mosip.kernel.certificate.sign.algorithm:SHA256withRSA}")
	private String signAlgorithm;

	/**
	 * Key Reference Cache Enable flag
	 * 
	 */
	@Value("${mosip.kernel.keymanager.keystore.keyreference.enable.cache:true}")
	private boolean enableKeyReferenceCache;

	/**
	 * JCE Implementation Clazz Name and other required information.
	 * 
	 */
	private Map<String, String> jceParams = new HashMap<String, String>();
	
	/**
	 * Algorithms names & Key Size Information.
	 * 
	 */
	private Map<String, String> keystoreParams = new HashMap<String, String>();

	/**
	 * Delegate Object.
	 * 
	 */
	private KeyStore keyStore = null;

	@Override
	public void afterPropertiesSet() throws Exception {

		// Adding supported algorithms from properties file.
		setAlgorithmProperties();
		String clazzName = DEFAULT_KS_IMPL_CLASSES.get(keystoreType);
		if (Objects.isNull(clazzName)) {
			clazzName = jceParams.get(KeymanagerConstant.JCE_CLAZZ_NAME);
			mergeJceParams();
		} else {
			addPKCSParams();
		}
		// Still clazzName is null, loading the keystore as offline to support only encryption.
		if (Objects.isNull(clazzName)) {
			LOGGER.info("ksSessionId", "KeyStoreImpl-Main", "KeyStoreImpl", "No Clazz Found to load " +
							"for Keystore Impl, So loading default offline clazz.");
			clazzName = DEFAULT_KS_IMPL_CLASSES.get(KeymanagerConstant.OFFLINE_KS_IMPL_CLAZZ);
		}
		LOGGER.info("ksSessionId", "KeyStoreImpl-Main", "KeyStoreImpl", "Found Clazz to load for Keystore Impl: " + clazzName);
		Class<?> object = Class.forName(clazzName);
		Optional<Constructor<?>> resConstructor = ReflectionUtils.findConstructor(object, keystoreParams);
		if (resConstructor.isPresent()) {
			Constructor<?> constructor = resConstructor.get();
			constructor.setAccessible(true);
			keyStore = (KeyStore) constructor.newInstance(keystoreParams);
		} else {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_NO_CONSTRUCTOR_FOUND.getErrorCode(),
						KeymanagerErrorCode.KEYSTORE_NO_CONSTRUCTOR_FOUND.getErrorMessage());
		}
		LOGGER.info("ksSessionId", "KeyStoreImpl-Main", "KeyStoreImpl", "Successfully loaded Clazz for Keystore Impl: " + clazzName);
	}

	private void setAlgorithmProperties() {
		keystoreParams.put(KeymanagerConstant.SYM_KEY_ALGORITHM, symmetricKeyAlgorithm);
		keystoreParams.put(KeymanagerConstant.SYM_KEY_SIZE, Integer.toString(symmetricKeyLength));
		keystoreParams.put(KeymanagerConstant.ASYM_KEY_ALGORITHM, asymmetricKeyAlgorithm);
		keystoreParams.put(KeymanagerConstant.ASYM_KEY_SIZE, Integer.toString(asymmetricKeyLength));
		keystoreParams.put(KeymanagerConstant.CERT_SIGN_ALGORITHM, signAlgorithm);
		keystoreParams.put(KeymanagerConstant.FLAG_KEY_REF_CACHE, Boolean.toString(enableKeyReferenceCache));
	}

	private void addPKCSParams() {
		keystoreParams.put(KeymanagerConstant.CONFIG_FILE_PATH, configPath);
		keystoreParams.put(KeymanagerConstant.PKCS11_KEYSTORE_PASSWORD, keystorePass);
	}


	private void mergeJceParams(){

		jceParams.forEach((key, value) -> {
			if(!key.equals(KeymanagerConstant.JCE_CLAZZ_NAME)){
				keystoreParams.put(key, value);
			}
		});
	}
		/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getAllAlias()
	 */
	@Override
	public List<String> getAllAlias() {
		return keyStore.getAllAlias();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getKey(java.lang.String)
	 */
	@Override
	public Key getKey(String alias) {
		return keyStore.getKey(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getAsymmetricKey(java.
	 * lang.String)
	 */
	@Override
	public PrivateKeyEntry getAsymmetricKey(String alias) {
		return keyStore.getAsymmetricKey(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getPrivateKey(java.lang.
	 * String)
	 */
	@Override
	public PrivateKey getPrivateKey(String alias) {
		return keyStore.getPrivateKey(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getPublicKey(java.lang.
	 * String)
	 */
	@Override
	public PublicKey getPublicKey(String alias) {
		return keyStore.getPublicKey(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getCertificate(java.lang.
	 * String)
	 */
	@Override
	public X509Certificate getCertificate(String alias) {
		return (X509Certificate) keyStore.getCertificate(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getSymmetricKey(java.lang
	 * .String)
	 */
	@Override
	public SecretKey getSymmetricKey(String alias) {
		return keyStore.getSymmetricKey(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#deleteKey(java.lang.
	 * String)
	 */
	@Override
	public void deleteKey(String alias) {
		keyStore.deleteKey(alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#storeAsymmetricKey(java.
	 * security.KeyPair, java.lang.String)
	 */
	@Override
	public void generateAndStoreAsymmetricKey(String alias, String signKeyAlias, CertificateParameters certParams) {
		keyStore.generateAndStoreAsymmetricKey(alias, signKeyAlias, certParams);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#storeSymmetricKey(javax.
	 * crypto.SecretKey, java.lang.String)
	 */
	@Override
	public void generateAndStoreSymmetricKey(String alias) {
		keyStore.generateAndStoreSymmetricKey(alias);
	}

	@Override
	public void storeCertificate(String alias, PrivateKey privateKey, Certificate certificate) {
		keyStore.storeCertificate(alias, privateKey, certificate);
	}

	@Override
	public String getKeystoreProviderName() {
		return keyStore.getKeystoreProviderName();
	}

	public void setJce(Map<String, String> jce) {
		this.jceParams = jce;
	}
}
