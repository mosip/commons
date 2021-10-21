package io.mosip.kernel.keymanagerservice.util;

import static java.util.Arrays.copyOfRange;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.keymanager.exception.KeystoreProcessingException;
import io.mosip.kernel.core.keymanager.model.CertificateEntry;
import io.mosip.kernel.core.keymanager.model.CertificateParameters;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.keymanager.hsm.constant.KeymanagerErrorCode;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerConstant;
import io.mosip.kernel.keymanagerservice.constant.KeymanagerErrorConstant;
import io.mosip.kernel.keymanagerservice.dto.CSRGenerateRequestDto;
import io.mosip.kernel.keymanagerservice.dto.KeyPairGenerateRequestDto;
import io.mosip.kernel.keymanagerservice.entity.BaseEntity;
import io.mosip.kernel.keymanagerservice.entity.KeyAlias;
import io.mosip.kernel.keymanagerservice.exception.KeymanagerServiceException;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.core.logger.spi.Logger;
/**
 * Utility class for Keymanager
 * 
 * @author Dharmesh Khandelwal
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */

@Component
public class KeymanagerUtil {

	private static final Logger LOGGER = KeymanagerLogger.getLogger(KeymanagerUtil.class);

	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	@Value("${mosip.kernel.keygenerator.asymmetric-algorithm-name}")
	private String asymmetricAlgorithmName;

	/**
	 * KeySplitter for splitting key and data
	 */
	@Value("${mosip.kernel.data-key-splitter}")
	private String keySplitter;

	/**
	 * Common Name for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.common-name}")
	private String commonName;

	/**
	 * Organizational Unit for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.organizational-unit}")
	private String organizationUnit;

	/**
	 * Organization for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.organization}")
	private String organization;

	/**
	 * Location for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.location}")
	private String location;

	/**
	 * State for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.state}")
	private String state;

	/**
	 * Country for generating certificate
	 */
	@Value("${mosip.kernel.keymanager.certificate.default.country}")
	private String country;

	/**
	 * Field for symmetric Algorithm Name
	 */
	@Value("${mosip.kernel.crypto.symmetric-algorithm-name}")
	private String symmetricAlgorithmName;

	/**
	 * Certificate Signing Algorithm
	 * 
	 */
	@Value("${mosip.kernel.certificate.sign.algorithm:SHA256withRSA}")
	private String signAlgorithm;

	/**
	 * KeyGenerator instance to generate asymmetric key pairs
	 */
	@Autowired
	KeyGenerator keyGenerator;

	/**
	 * {@link CryptoCoreSpec} instance for cryptographic functionalities.
	 */
	@Autowired
	private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

	/**
	 * Function to check valid timestamp
	 * 
	 * @param timeStamp timeStamp
	 * @param keyAlias  keyAlias
	 * @return true if timestamp is valid, else false
	 */
	public boolean isValidTimestamp(LocalDateTime timeStamp, KeyAlias keyAlias) {
		return timeStamp.isEqual(keyAlias.getKeyGenerationTime()) || timeStamp.isEqual(keyAlias.getKeyExpiryTime())
				|| (timeStamp.isAfter(keyAlias.getKeyGenerationTime())
						&& timeStamp.isBefore(keyAlias.getKeyExpiryTime()));
	}

	/**
	 * Function to check if timestamp is overlapping
	 * 
	 * @param timeStamp         timeStamp
	 * @param policyExpiryTime  policyExpiryTime
	 * @param keyGenerationTime keyGenerationTime
	 * @param keyExpiryTime     keyExpiryTime
	 * @return true if timestamp is overlapping, else false
	 */
	public boolean isOverlapping(LocalDateTime timeStamp, LocalDateTime policyExpiryTime,
			LocalDateTime keyGenerationTime, LocalDateTime keyExpiryTime) {
		return !timeStamp.isAfter(keyExpiryTime) && !keyGenerationTime.isAfter(policyExpiryTime);
	}

	/**
	 * Function to check is reference id is valid
	 * 
	 * @param referenceId referenceId
	 * @return true if referenceId is valid, else false
	 */
	public boolean isValidReferenceId(String referenceId) {
		return referenceId != null && !referenceId.trim().isEmpty();
	}

	/**
	 * Function to set metadata
	 * 
	 * @param <T>    is a type parameter
	 * @param entity entity of T type
	 * @return Entity with metadata
	 */
	public <T extends BaseEntity> T setMetaData(T entity) {
		String contextUser = "SYSTEM";
		LocalDateTime time = LocalDateTime.now(ZoneId.of("UTC"));
		entity.setCreatedBy(contextUser);
		entity.setCreatedtimes(time);
		entity.setIsDeleted(false);
		return entity;
	}

	/**
	 * Function to encrypt key
	 * 
	 * @param privateKey privateKey
	 * @param masterKey  masterKey
	 * @return encrypted key
	 */
	public byte[] encryptKey(PrivateKey privateKey, PublicKey masterKey) {
		SecretKey symmetricKey = keyGenerator.getSymmetricKey();
		byte[] encryptedPrivateKey = cryptoCore.symmetricEncrypt(symmetricKey, privateKey.getEncoded(), null);
		byte[] encryptedSymmetricKey = cryptoCore.asymmetricEncrypt(masterKey, symmetricKey.getEncoded());
		return CryptoUtil.combineByteArray(encryptedPrivateKey, encryptedSymmetricKey, keySplitter);
	}

	/**
	 * Function to decrypt key
	 * 
	 * @param key        key
	 * @param privateKey privateKey
	 * @return decrypted key
	 */
	public byte[] decryptKey(byte[] key, PrivateKey privateKey, PublicKey publicKey) {

		int keyDemiliterIndex = 0;
		final int cipherKeyandDataLength = key.length;
		final int keySplitterLength = keySplitter.length();
		keyDemiliterIndex = CryptoUtil.getSplitterIndex(key, keyDemiliterIndex, keySplitter);
		byte[] encryptedKey = copyOfRange(key, 0, keyDemiliterIndex);
		byte[] encryptedData = copyOfRange(key, keyDemiliterIndex + keySplitterLength, cipherKeyandDataLength);
		byte[] decryptedSymmetricKey = cryptoCore.asymmetricDecrypt(privateKey, publicKey, encryptedKey);
		SecretKey symmetricKey = new SecretKeySpec(decryptedSymmetricKey, 0, decryptedSymmetricKey.length,
				symmetricAlgorithmName);
		return cryptoCore.symmetricDecrypt(symmetricKey, encryptedData, null);
	}

	/**
	 * Parse a date string of pattern UTC_DATETIME_PATTERN into
	 * {@link LocalDateTime}
	 * 
	 * @param dateTime of type {@link String} of pattern UTC_DATETIME_PATTERN
	 * @return a {@link LocalDateTime} of given pattern
	 */
	public LocalDateTime parseToLocalDateTime(String dateTime) {
		return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));
	}

	public void isCertificateValid(CertificateEntry<X509Certificate, PrivateKey> certificateEntry, Date inputDate) {
		try {
			certificateEntry.getChain()[0].checkValidity(inputDate);
		} catch (CertificateExpiredException | CertificateNotYetValidException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.CERTIFICATE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.CERTIFICATE_PROCESSING_ERROR.getErrorMessage() + e.getMessage());
		}
	}

	public PrivateKey privateKeyExtractor(InputStream privateKeyInputStream) {

		KeyFactory kf = null;
		PKCS8EncodedKeySpec keySpec = null;
		PrivateKey privateKey = null;
		try {
			StringWriter stringWriter = new StringWriter();
			IOUtils.copy(privateKeyInputStream, stringWriter, StandardCharsets.UTF_8);
			String privateKeyPEMString = stringWriter.toString();
			byte[] decodedKey = Base64.decodeBase64(privateKeyPEMString);
			kf = KeyFactory.getInstance(asymmetricAlgorithmName);
			keySpec = new PKCS8EncodedKeySpec(decodedKey);
			privateKey = kf.generatePrivate(keySpec);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.KEYSTORE_PROCESSING_ERROR.getErrorMessage() + e.getMessage());
		}

		return privateKey;
	}

	public boolean isValidResponseType(String responseType) {
		return responseType != null && !responseType.trim().isEmpty();
	}

	public boolean isValidApplicationId(String appId) {
		return appId != null && !appId.trim().isEmpty();
	}

	public boolean isValidCertificateData(String certData) {
		return certData != null && !certData.trim().isEmpty();
	}

	public Certificate convertToCertificate(String certData) {
		try {
			StringReader strReader = new StringReader(certData);
			PemReader pemReader = new PemReader(strReader);
			PemObject pemObject = pemReader.readPemObject();
			if (Objects.isNull(pemObject)) {
				LOGGER.error(KeymanagerConstant.SESSIONID, KeymanagerConstant.CERTIFICATE_PARSE, 
								KeymanagerConstant.CERTIFICATE_PARSE, "Error Parsing Certificate.");
				throw new KeymanagerServiceException(io.mosip.kernel.keymanagerservice.constant.KeymanagerErrorConstant.CERTIFICATE_PARSING_ERROR.getErrorCode(),
								KeymanagerErrorConstant.CERTIFICATE_PARSING_ERROR.getErrorMessage());				
			}
			byte[] certBytes = pemObject.getContent();
			CertificateFactory certFactory = CertificateFactory.getInstance(KeymanagerConstant.CERTIFICATE_TYPE);
			return certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
		} catch(IOException | CertificateException e) {
			throw new KeymanagerServiceException(KeymanagerErrorConstant.CERTIFICATE_PARSING_ERROR.getErrorCode(),
					KeymanagerErrorConstant.CERTIFICATE_PARSING_ERROR.getErrorMessage() + e.getMessage());
		}
	}

	public Certificate convertToCertificate(byte[] certDataBytes) {
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance(KeymanagerConstant.CERTIFICATE_TYPE);
			return certFactory.generateCertificate(new ByteArrayInputStream(certDataBytes));
		} catch(CertificateException e) {
			throw new KeymanagerServiceException(KeymanagerErrorConstant.CERTIFICATE_PARSING_ERROR.getErrorCode(),
					KeymanagerErrorConstant.CERTIFICATE_PARSING_ERROR.getErrorMessage() + e.getMessage());
		}
	}

	public String getPEMFormatedData(Object anyObject){
		
		StringWriter stringWriter = new StringWriter();
		try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
			pemWriter.writeObject(anyObject);
			pemWriter.flush();
			return stringWriter.toString();
		} catch (IOException ioExp) {
			throw new KeymanagerServiceException(KeymanagerErrorConstant.INTERNAL_SERVER_ERROR.getErrorCode(),
						KeymanagerErrorConstant.INTERNAL_SERVER_ERROR.getErrorMessage(), ioExp);
		}
	}

	public CertificateParameters getCertificateParameters(X500Principal latestCertPrincipal, LocalDateTime notBefore, LocalDateTime notAfter) {

		CertificateParameters certParams = new CertificateParameters();
		X500Name x500Name = new X500Name(latestCertPrincipal.getName());

		certParams.setCommonName(IETFUtils.valueToString((x500Name.getRDNs(BCStyle.CN)[0]).getFirst().getValue()));
		certParams.setOrganizationUnit(getParamValue(getAttributeIfExist(x500Name, BCStyle.OU), organizationUnit));
		certParams.setOrganization(getParamValue(getAttributeIfExist(x500Name, BCStyle.O), organization));
		certParams.setLocation(getParamValue(getAttributeIfExist(x500Name, BCStyle.L), location));
		certParams.setState(getParamValue(getAttributeIfExist(x500Name, BCStyle.ST), state));
		certParams.setCountry(getParamValue(getAttributeIfExist(x500Name, BCStyle.C), country));
		certParams.setNotBefore(notBefore);
		certParams.setNotAfter(notAfter);
		return certParams;
	}

	private static String getAttributeIfExist(X500Name x500Name, ASN1ObjectIdentifier identifier) {
        RDN[] rdns = x500Name.getRDNs(identifier);
        if (rdns.length == 0) {
            return KeymanagerConstant.EMPTY;
        }
        return IETFUtils.valueToString((rdns[0]).getFirst().getValue());
    }

	public CertificateParameters getCertificateParameters(KeyPairGenerateRequestDto request, LocalDateTime notBefore, LocalDateTime notAfter) {

		CertificateParameters certParams = new CertificateParameters();
		certParams.setCommonName(getParamValue(request.getCommonName(), commonName));
		certParams.setOrganizationUnit(getParamValue(request.getOrganizationUnit(), organizationUnit));
		certParams.setOrganization(getParamValue(request.getOrganization(), organization));
		certParams.setLocation(getParamValue(request.getLocation(), location));
		certParams.setState(getParamValue(request.getState(), state));
		certParams.setCountry(getParamValue(request.getCountry(), country));
		certParams.setNotBefore(notBefore);
		certParams.setNotAfter(notAfter);
		return certParams;
	}

	public CertificateParameters getCertificateParameters(CSRGenerateRequestDto request, LocalDateTime notBefore, LocalDateTime notAfter) {

		CertificateParameters certParams = new CertificateParameters();
		certParams.setCommonName(getParamValue(request.getCommonName(), commonName));
		certParams.setOrganizationUnit(getParamValue(request.getOrganizationUnit(), organizationUnit));
		certParams.setOrganization(getParamValue(request.getOrganization(), organization));
		certParams.setLocation(getParamValue(request.getLocation(), location));
		certParams.setState(getParamValue(request.getState(), state));
		certParams.setCountry(getParamValue(request.getCountry(), country));
		certParams.setNotBefore(notBefore);
		certParams.setNotAfter(notAfter);
		return certParams;
	}
	
	private String getParamValue(String value, String defaultValue){
		if (Objects.nonNull(value) && !value.trim().isEmpty())
			return value;
			
		return defaultValue;
	}
	
	public String getCSR(PrivateKey privateKey, PublicKey publicKey, CertificateParameters certParams) {

		try {
			X500Principal csrSubject = new X500Principal("CN=" + certParams.getCommonName() + ", OU=" + certParams.getOrganizationUnit() +
												", O=" + certParams.getOrganization() + ", L=" + certParams.getLocation() + 
												", S=" + certParams.getState() + ", C=" + certParams.getCountry());
			ContentSigner contentSigner = new JcaContentSignerBuilder(signAlgorithm).build(privateKey);
			PKCS10CertificationRequestBuilder pcks10Builder = new JcaPKCS10CertificationRequestBuilder(csrSubject, publicKey);
			PKCS10CertificationRequest csrObject = pcks10Builder.build(contentSigner);
			return getPEMFormatedData(csrObject);
		} catch (OperatorCreationException exp) {
			throw new KeymanagerServiceException(KeymanagerErrorConstant.INTERNAL_SERVER_ERROR.getErrorCode(),
						KeymanagerErrorConstant.INTERNAL_SERVER_ERROR.getErrorMessage(), exp);
		}
	}

	public void destoryKey(PrivateKey privateKey) {
		try {
			privateKey.destroy();
		} catch (DestroyFailedException e) {
			LOGGER.warn(KeymanagerConstant.SESSIONID, KeymanagerConstant.EMPTY, KeymanagerConstant.EMPTY,
					"Warning - while destorying Private Key Object.");
		}
		privateKey = null;
	}

	public void destoryKey(SecretKey secretKey) {
		try {
			secretKey.destroy();
		} catch (DestroyFailedException e) {
			LOGGER.warn(KeymanagerConstant.SESSIONID, KeymanagerConstant.EMPTY, KeymanagerConstant.EMPTY,
					"Warning - while destorying Secret Key Object.");
		}
		secretKey = null;
	}

	public LocalDateTime convertToUTC(Date anyDate) {
		LocalDateTime ldTime = DateUtils.parseDateToLocalDateTime(anyDate);
		ZonedDateTime zonedtime = ldTime.atZone(ZoneId.systemDefault());
        ZonedDateTime converted = zonedtime.withZoneSameInstant(ZoneOffset.UTC);
        return converted.toLocalDateTime();
	}
}
