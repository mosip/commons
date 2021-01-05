package io.mosip.kernel.keymanager.hsm.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.keymanager.exception.KeystoreProcessingException;
import io.mosip.kernel.core.keymanager.exception.NoSuchSecurityProviderException;
import io.mosip.kernel.core.keymanager.model.CertificateParameters;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keygenerator.bouncycastle.constant.KeyGeneratorExceptionConstant;
import io.mosip.kernel.keymanager.hsm.constant.KeymanagerConstant;
import io.mosip.kernel.keymanager.hsm.constant.KeymanagerErrorCode;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.keymanager.hsm.util.CertificateUtility;


/**
 * Softhsm Keymanager implementation based on OpenDNSSEC that handles and stores
 * its cryptographic keys via the PKCS#11 interface. This is a software
 * implementation of a generic cryptographic device. SoftHSM can work with other
 * cryptographic device because of the PKCS#11 interface.
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Component
public class KeyStoreImpl implements io.mosip.kernel.core.keymanager.spi.KeyStore, InitializingBean {

	private static final Logger LOGGER = KeymanagerLogger.getLogger(KeyStoreImpl.class);

	/**
	 * Common name for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.softhsm.certificate.common-name}")
	private String commonName;

	/**
	 * Organizational Unit for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.softhsm.certificate.organizational-unit}")
	private String organizationalUnit;

	/**
	 * Organization for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.softhsm.certificate.organization}")
	private String organization;

	/**
	 * Country for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.softhsm.certificate.country}")
	private String country;

	/**
	 * Path of HSM PKCS11 config file or the Keystore in caes of bouncy castle
	 * provider
	 */
	@Value("${mosip.kernel.keymanager.softhsm.config-path}")
	private String configPath;

	/**
	 * The type of keystore, e.g. PKCS11, BouncyCastleProvider
	 */
	@Value("${mosip.kernel.keymanager.softhsm.keystore-type:PKCS11}")
	private String keystoreType;

	/**
	 * The passkey for Keystore
	 */
	@Value("${mosip.kernel.keymanager.softhsm.keystore-pass:\"\"}")
	private String keystorePass;

	/**
	 * Symmetric key algorithm Name
	 */
	@Value("${mosip.kernel.keygenerator.symmetric-algorithm-name}")
	private String symmetricKeyAlgorithm;

	/**
	 * Symmetric key length
	 */
	@Value("${mosip.kernel.keygenerator.symmetric-key-length}")
	private int symmetricKeyLength;

	/**
	 * Asymmetric key algorithm Name
	 */
	@Value("${mosip.kernel.keygenerator.asymmetric-algorithm-name}")
	private String asymmetricKeyAlgorithm;

	/**
	 * Asymmetric key length
	 */
	@Value("${mosip.kernel.keygenerator.asymmetric-key-length}")
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

	private Map<String, PrivateKeyEntry> privateKeyReferenceCache;

	private Map<String, SecretKey> secretKeyReferenceCache;

	/**
	 * The Keystore instance
	 */
	private KeyStore keyStore;

	private Provider provider = null;

	private LocalDateTime lastProviderLoadedTime;

	private static final int PROVIDER_ALLOWED_RELOAD_INTERVEL_IN_SECONDS = 60;

	private static final int NO_OF_RETRIES = 3;

	private char[] keystorePwdCharArr = null;

	@Override
	public void afterPropertiesSet() throws Exception {
		initKeyReferenceCache();
		if (!isConfigFileValid()) {
			LOGGER.info("sessionId", "KeyStoreImpl", "Creation", "Config File path is not valid or contents invalid entries. " 
						+ "So, Loading keystore as offline encryption.");
			BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
			Security.addProvider(bouncyCastleProvider);
			this.keyStore = getKeystoreInstance(KeymanagerConstant.KEYSTORE_TYPE_PKCS12, bouncyCastleProvider);
			loadKeystore();
			lastProviderLoadedTime = DateUtils.getUTCCurrentDateTime();
			return;
		}
		keystorePwdCharArr = getKeystorePwd();
		provider = setupProvider(configPath);
		Security.removeProvider(provider.getName());
		addProvider(provider);
		BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
		Security.addProvider(bouncyCastleProvider);
		this.keyStore = getKeystoreInstance(keystoreType, provider);
		loadKeystore();
		lastProviderLoadedTime = DateUtils.getUTCCurrentDateTime();
		// loadCertificate();
	}

	private boolean isConfigFileValid() {
		if (configPath.trim().length() == 0)
			return false;
		
		try {
			return Files.readString(Paths.get(configPath)).trim().length() != 0;
		} catch (IOException e) {
			LOGGER.error("sessionId", "KeyStoreImpl", "configFile", "Error reading pkcs11 config file.");
		}
		return true;
	}

	private char[] getKeystorePwd() {
		if (keystorePass.trim().length() == 0){
			return null;
		}
		return keystorePass.toCharArray();
	}

	/**
	 * Setup a new SunPKCS11 provider
	 * 
	 * @param configPath
	 *            The path of config file or keyStore in case of bouncycastle
	 *            provider
	 * @return Provider
	 */
	private synchronized Provider setupProvider(String configPath) {
		Provider configuredProvider;
		try {
			switch (keystoreType) {
			case "PKCS11":
				Provider sunPKCS11Provider = Security.getProvider("SunPKCS11");
				if(sunPKCS11Provider == null)
					throw new ProviderException("SunPKCS11 provider not found");
				configuredProvider = sunPKCS11Provider.configure(configPath);
				break;
			case "BouncyCastleProvider":
				configuredProvider = new BouncyCastleProvider();
				break;
			default:
				Provider sunPKCS11ProviderDefault = Security.getProvider("SunPKCS11");
				if(sunPKCS11ProviderDefault == null)
					throw new ProviderException("SunPKCS11 provider not found");
				configuredProvider = sunPKCS11ProviderDefault.configure(configPath);
				break;

			}
		} catch (ProviderException | InvalidParameterException providerException ) {
			throw new NoSuchSecurityProviderException(KeymanagerErrorCode.INVALID_CONFIG_FILE.getErrorCode(),
					KeymanagerErrorCode.INVALID_CONFIG_FILE.getErrorMessage(), providerException);
		}
		return configuredProvider;
	}

	/**
	 * Adds a provider to the next position available.
	 * 
	 * If there is a security manager, the
	 * java.lang.SecurityManager.checkSecurityAccess method is called with the
	 * "insertProvider" permission target name to see if it's ok to add a new
	 * provider. If this permission check is denied, checkSecurityAccess is called
	 * again with the "insertProvider."+provider.getName() permission target name.
	 * If both checks are denied, a SecurityException is thrown.
	 * 
	 * @param provider
	 *            the provider to be added
	 */
	private void addProvider(Provider provider) {
		if (-1 == Security.addProvider(provider)) {
			throw new NoSuchSecurityProviderException(KeymanagerErrorCode.NO_SUCH_SECURITY_PROVIDER.getErrorCode(),
					KeymanagerErrorCode.NO_SUCH_SECURITY_PROVIDER.getErrorMessage());
		}
	}

	/**
	 * Returns a keystore object of the specified type.
	 * 
	 * A new KeyStore object encapsulating the KeyStoreSpi implementation from the
	 * specified Provider object is returned. Note that the specified Provider
	 * object does not have to be registered in the provider list.
	 * 
	 * @param keystoreType
	 *            the type of keystore
	 * @param provider
	 *            provider
	 * @return a keystore object of the specified type.
	 */
	private KeyStore getKeystoreInstance(String keystoreType, Provider provider) {
		KeyStore mosipKeyStore = null;
		try {
			mosipKeyStore = KeyStore.getInstance(keystoreType, provider);
		} catch (KeyStoreException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
		}
		return mosipKeyStore;
	}

	/**
	 * 
	 * 
	 * Loads this KeyStore from the given input stream.
	 * 
	 * A password may be given to unlock the keystore (e.g. the keystore resides on
	 * a hardware token device), or to check the integrity of the keystore data. If
	 * a password is not given for integrity checking, then integrity checking is
	 * not performed.
	 * 
	 * In order to create an empty keystore, or if the keystore cannot be
	 * initialized from a stream, pass null as the stream argument.
	 * 
	 * Note that if this keystore has already been loaded, it is reinitialized and
	 * loaded again from the given input stream.
	 * 
	 */
	@SuppressWarnings("findsecbugs:HARD_CODE_PASSWORD")
	private void loadKeystore() {

		try {
			switch (keystoreType) {
			case "PKCS11":
				keyStore.load(null, keystorePwdCharArr);
				break;
			case "BouncyCastleProvider":
				// added try with res for sonar bug fix
				try (FileInputStream fis = new FileInputStream(configPath)) {
					keyStore.load(fis, keystorePwdCharArr);
				}
				break;
			default:
				keyStore.load(null, keystorePwdCharArr);
				break;
			}

		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getAllAlias()
	 */
	@Override
	public List<String> getAllAlias() {
		Enumeration<String> enumeration = null;
		try {
			enumeration = keyStore.aliases();
		} catch (KeyStoreException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
		}
		return Collections.list(enumeration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getKey(java.lang.String)
	 */
	@Override
	public Key getKey(String alias) {
		Key key = null;
		try {
			key = keyStore.getKey(alias, keystorePwdCharArr);
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
		}
		return key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getAsymmetricKey(java.
	 * lang.String)
	 */
	@SuppressWarnings("findsecbugs:HARD_CODE_PASSWORD")
	@Override
	public PrivateKeyEntry getAsymmetricKey(String alias) {
		PrivateKeyEntry privateKeyEntry = getPrivateKeyEntryFromCache(alias);
		if(privateKeyEntry != null)
			return privateKeyEntry;
		validatePKCS11KeyStore();
		int i = 0;
		boolean isException = false;
		String expMessage = "";
		Exception exp = null;
		do {
			try {
				if (keyStore.entryInstanceOf(alias, PrivateKeyEntry.class)) {
					LOGGER.debug("sessionId", "KeyStoreImpl", "getAsymmetricKey", "alias is instanceof keystore");
					ProtectionParameter password = getPasswordProtection();
					privateKeyEntry = (PrivateKeyEntry) keyStore.getEntry(alias, password);
					if (privateKeyEntry != null) {
						LOGGER.debug("sessionId", "KeyStoreImpl", "getAsymmetricKey", "privateKeyEntry is not null");
						break;
					}
				} else {
					throw new NoSuchSecurityProviderException(KeymanagerErrorCode.NO_SUCH_ALIAS.getErrorCode(),
							KeymanagerErrorCode.NO_SUCH_ALIAS.getErrorMessage() + alias);
				}
			} catch (NoSuchAlgorithmException | UnrecoverableEntryException e) {
				throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
						KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
			} catch (KeyStoreException kse) {
				isException = true;
				expMessage = kse.getMessage();
				exp = kse;
				LOGGER.debug("sessionId", "KeyStoreImpl", "getAsymmetricKey", expMessage);
			}
			if (isException) {
				reloadProvider();
				isException = false;
			}
		} while (i++ < NO_OF_RETRIES);
		if (Objects.isNull(privateKeyEntry)) {
			LOGGER.debug("sessionId", "KeyStoreImpl", "getAsymmetricKey", "privateKeyEntry is null");
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + expMessage, exp);
		}
		addPrivateKeyEntryToCache(alias, privateKeyEntry);
		return privateKeyEntry;
	}

	private synchronized void reloadProvider() {
		LOGGER.info("sessionId", "KeyStoreImpl", "KeyStoreImpl", "reloading provider");
		if(DateUtils.getUTCCurrentDateTime().isBefore(
				lastProviderLoadedTime.plusSeconds(PROVIDER_ALLOWED_RELOAD_INTERVEL_IN_SECONDS))) {
			LOGGER.warn("sessionId", "KeyStoreImpl", "reloadProvider", 
				"Last time successful reload done on " + lastProviderLoadedTime.toString() + 
					", so reloading not done before interval of " + 
					PROVIDER_ALLOWED_RELOAD_INTERVEL_IN_SECONDS + " sec");
			return;
		}
		String existingProviderName = null;
		if (Objects.nonNull(provider))
			existingProviderName = provider.getName();
		provider = setupProvider(configPath);
		if(existingProviderName != null)
			Security.removeProvider(existingProviderName);
		addProvider(provider);
		initKeyReferenceCache();
		this.keyStore = getKeystoreInstance(keystoreType, provider);
		loadKeystore();
		lastProviderLoadedTime = DateUtils.getUTCCurrentDateTime();
		LOGGER.info("sessionId", "KeyStoreImpl", "KeyStoreImpl", "reloading provider successfully completed");
	}

	private void validatePKCS11KeyStore() {
		if(KeymanagerConstant.KEYSTORE_TYPE_PKCS12.equals(keyStore.getType())){
			throw new KeystoreProcessingException(KeymanagerErrorCode.NOT_VALID_PKCS11_STORE_TYPE.getErrorCode(),
						KeymanagerErrorCode.NOT_VALID_PKCS11_STORE_TYPE.getErrorMessage() );
		}
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
		PrivateKeyEntry privateKeyEntry = getAsymmetricKey(alias);
		return privateKeyEntry.getPrivateKey();
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
		PrivateKeyEntry privateKeyEntry = getAsymmetricKey(alias);
		Certificate[] certificates = privateKeyEntry.getCertificateChain();
		return certificates[0].getPublicKey();
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
		PrivateKeyEntry privateKeyEntry = getAsymmetricKey(alias);
		X509Certificate[] certificates = (X509Certificate[]) privateKeyEntry.getCertificateChain();
		return certificates[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#storeAsymmetricKey(java.
	 * security.KeyPair, java.lang.String)
	 */
	@SuppressWarnings("findsecbugs:HARD_CODE_PASSWORD")
	@Override
	public void storeAsymmetricKey(KeyPair keyPair, String alias, LocalDateTime validityFrom,
			LocalDateTime validityTo) {
		
		X509Certificate[] chain = new X509Certificate[1];
		chain[0] = CertificateUtility.generateX509Certificate(keyPair.getPrivate(), keyPair.getPublic(), commonName, 
				organizationalUnit, organization, country, validityFrom, validityTo,
				signAlgorithm == null? KeymanagerConstant.SIGNATURE_ALGORITHM : signAlgorithm, 
				provider == null ? "BC": provider.getName());
		storeCertificate(alias, chain, keyPair.getPrivate());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getSymmetricKey(java.lang
	 * .String)
	 */
	@SuppressWarnings("findsecbugs:HARD_CODE_PASSWORD")
	@Override
	public SecretKey getSymmetricKey(String alias) {
		SecretKey secretKey = getSecretKeyFromCache(alias);
		if(secretKey != null)
			return secretKey;
		validatePKCS11KeyStore();
		int i = 0;
		boolean isException = false;
		String expMessage = "";
		Exception exp = null;
		do {
			try {
				if (keyStore.entryInstanceOf(alias, SecretKeyEntry.class)) {
					ProtectionParameter password = getPasswordProtection();
					SecretKeyEntry retrivedSecret = (SecretKeyEntry) keyStore.getEntry(alias, password);
					secretKey = retrivedSecret.getSecretKey();
					if (secretKey != null) {
						LOGGER.debug("sessionId", "KeyStoreImpl", "getSymmetricKey", "secretKey is not null");
						break;
					}
				} else {
					throw new NoSuchSecurityProviderException(KeymanagerErrorCode.NO_SUCH_ALIAS.getErrorCode(),
							KeymanagerErrorCode.NO_SUCH_ALIAS.getErrorMessage() + alias);
				}
			} catch (NoSuchAlgorithmException | UnrecoverableEntryException e) {
				throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
						KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
			} catch (KeyStoreException kse) {
				isException = true;
				expMessage = kse.getMessage();
				exp = kse;
				LOGGER.debug("sessionId", "KeyStoreImpl", "getSymmetricKey", expMessage);
			}
			if (isException) {
				reloadProvider();
				isException = false;
			}
		} while (i++ < NO_OF_RETRIES);
		if (Objects.isNull(secretKey)) {
			LOGGER.debug("sessionId", "KeyStoreImpl", "getSymmetricKey", "secretKey is null");
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + expMessage, exp);
		}
		addSecretKeyToCache(alias, secretKey);
		return secretKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#storeSymmetricKey(javax.
	 * crypto.SecretKey, java.lang.String)
	 */
	@SuppressWarnings("findsecbugs:HARD_CODE_PASSWORD")
	@Override
	public void storeSymmetricKey(SecretKey secretKey, String alias) {

		SecretKeyEntry secret = new SecretKeyEntry(secretKey);
		ProtectionParameter password = getPasswordProtection();
		try {
			keyStore.setEntry(alias, secret, password);
			keyStore.store(null, keystorePwdCharArr);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#deleteKey(java.lang.
	 * String)
	 */
	@Override
	public void deleteKey(String alias) {
		validatePKCS11KeyStore();
		try {
			keyStore.deleteEntry(alias);
		} catch (KeyStoreException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
		}
	}

	/**
	 * Sets keystore
	 * 
	 * @param keyStore
	 *            keyStore
	 */
	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}

	
	private void storeCertificate(String alias, Certificate[] chain, PrivateKey privateKey) {
		PrivateKeyEntry privateKeyEntry = new PrivateKeyEntry(privateKey, chain);
		ProtectionParameter password = getPasswordProtection();
		try {
			keyStore.setEntry(alias, privateKeyEntry, password);
			keyStore.store(null, keystorePwdCharArr);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#storeAsymmetricKey(java.
	 * security.KeyPair, java.lang.String)
	 */
	@SuppressWarnings("findsecbugs:HARD_CODE_PASSWORD")
	@Override
	public void generateAndStoreAsymmetricKey(String alias, String signKeyAlias, CertificateParameters certParams) {
		validatePKCS11KeyStore();
		KeyPair keyPair = null;
		PrivateKey signPrivateKey = null;
		X500Principal signerPrincipal = null;
		if (Objects.nonNull(signKeyAlias)) {
			PrivateKeyEntry signKeyEntry = getAsymmetricKey(signKeyAlias);
			signPrivateKey = signKeyEntry.getPrivateKey();
			X509Certificate signCert = (X509Certificate) signKeyEntry.getCertificate();
			signerPrincipal = signCert.getSubjectX500Principal();
			keyPair = generateKeyPair(); // To avoid key generation in HSM.
		} else {
			keyPair = generateKeyPair();
			signPrivateKey = keyPair.getPrivate();
		}
		X509Certificate x509Cert = CertificateUtility.generateX509Certificate(signPrivateKey, keyPair.getPublic(), certParams, 
									signerPrincipal, signAlgorithm, provider.getName());
		X509Certificate[] chain = new X509Certificate[] {x509Cert};
		storeCertificate(alias, chain, keyPair.getPrivate());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#storeSymmetricKey(javax.
	 * crypto.SecretKey, java.lang.String)
	 */
	@SuppressWarnings("findsecbugs:HARD_CODE_PASSWORD")
	@Override
	public void generateAndStoreSymmetricKey(String alias) {
		validatePKCS11KeyStore();
		SecretKey secretKey = generateSymmetricKey();
		SecretKeyEntry secret = new SecretKeyEntry(secretKey);
		ProtectionParameter password = getPasswordProtection();
		try {
			keyStore.setEntry(alias, secret, password);
			keyStore.store(null, keystorePwdCharArr);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
		}
	}

	private KeyPair generateKeyPair() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance(asymmetricKeyAlgorithm, provider);
			SecureRandom random = new SecureRandom();
			generator.initialize(asymmetricKeyLength, random);
			return generator.generateKeyPair();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new io.mosip.kernel.core.exception.NoSuchAlgorithmException(
					KeyGeneratorExceptionConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					KeyGeneratorExceptionConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
		}
	}

	private SecretKey generateSymmetricKey() {
		try {
			KeyGenerator generator = KeyGenerator.getInstance(symmetricKeyAlgorithm, provider);
			SecureRandom random = new SecureRandom();
			generator.init(symmetricKeyLength, random);
			return generator.generateKey();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new io.mosip.kernel.core.exception.NoSuchAlgorithmException(
					KeyGeneratorExceptionConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					KeyGeneratorExceptionConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage(), e);
		}
		
	}

	@Override
	public void storeCertificate(String alias, PrivateKey privateKey, Certificate certificate) {
		try {
			PrivateKeyEntry privateKeyEntry = new PrivateKeyEntry(privateKey, new Certificate[] {certificate});
			ProtectionParameter password = getPasswordProtection();
			keyStore.setEntry(alias, privateKeyEntry, password);
			keyStore.store(null, keystorePwdCharArr);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
		}
	}

	@Override
	public Certificate generateCertificate(PrivateKey signPrivateKey, PublicKey publicKey, CertificateParameters certParams, X500Principal signerPrincipal){
		// Added this method because provider is not exposed from this class.
		return CertificateUtility.generateX509Certificate(signPrivateKey, publicKey, certParams, signerPrincipal, signAlgorithm, provider.getName());
	}

	@Override
	public String getKeystoreProviderName() {
		if (Objects.nonNull(keyStore)) {
			return keyStore.getProvider().getName();
		}
		throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_NOT_INSTANTIATED.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_NOT_INSTANTIATED.getErrorMessage());
	}

	private PasswordProtection getPasswordProtection() {
		if (keystorePwdCharArr == null) {
			return null;
		}
		return new PasswordProtection(keystorePwdCharArr);
	}

	private void initKeyReferenceCache() {
		if(!enableKeyReferenceCache)
			return;
		this.privateKeyReferenceCache = new ConcurrentHashMap<>();
		this.secretKeyReferenceCache = new ConcurrentHashMap<>();
	}

	private void addPrivateKeyEntryToCache(String alias, PrivateKeyEntry privateKeyEntry) {
		if(!enableKeyReferenceCache)
			return;
		LOGGER.debug("sessionId", "KeyStoreImpl", "addPrivateKeyEntryToCache", 
			"Adding private key reference to map for alias " + alias);
		this.privateKeyReferenceCache.put(alias, privateKeyEntry);
	}

	private PrivateKeyEntry getPrivateKeyEntryFromCache(String alias) {
		if(!enableKeyReferenceCache)
			return null;
		return this.privateKeyReferenceCache.get(alias);
	}

	private void addSecretKeyToCache(String alias, SecretKey secretKey) {
		if(!enableKeyReferenceCache)
			return;
		LOGGER.debug("sessionId", "KeyStoreImpl", "addSecretKeyToCache", 
			"Adding secretKey reference to map for alias " + alias);
		this.secretKeyReferenceCache.put(alias, secretKey);
	}

	private SecretKey getSecretKeyFromCache(String alias) {
		if(!enableKeyReferenceCache)
			return null;
		return this.secretKeyReferenceCache.get(alias);
	}
}
