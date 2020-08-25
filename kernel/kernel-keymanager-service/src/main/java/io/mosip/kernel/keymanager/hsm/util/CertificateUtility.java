package io.mosip.kernel.keymanager.hsm.util;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import io.mosip.kernel.core.keymanager.exception.KeystoreProcessingException;
import io.mosip.kernel.core.keymanager.model.CertificateParameters;
import io.mosip.kernel.keymanager.hsm.constant.KeymanagerErrorCode;

/**
 * Certificate utility to generate and sign X509 Certificate
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public class CertificateUtility {

	
	/**
	 * Private constructor for CertificateUtility
	 */
	private CertificateUtility() {
	}

	/**
	 * Generate and sign X509 Certificate
	 * 
	 * @param keyPair            the keypair
	 * @param commonName         commonName
	 * @param organizationalUnit organizationalUnit
	 * @param organization       organization
	 * @param country            country
	 * @param validityFrom       validityFrom
	 * @param validityTo         validityTo
	 * @return The certificate
	 */
	public static X509Certificate generateX509Certificate(PrivateKey signPrivateKey, PublicKey publicKey, String commonName, String organizationalUnit,
			String organization, String country, LocalDateTime validityFrom, LocalDateTime validityTo, String signAlgorithm, String providerName) { 

		X500Name rootCertIssuer = new X500Name(getCertificateAttributes(commonName, organizationalUnit, organization, country));
		X500Name rootCertSubject = rootCertIssuer;
		return generateX509Certificate(signPrivateKey, publicKey, rootCertIssuer, rootCertSubject, signAlgorithm, providerName, validityFrom, validityTo);
	}

	/**
	 * Generate and sign X509 Certificate
	 * 
	 * @param signPrivateKey  the private key for signing certificate
	 * @param publicKey  the public key for generating certificate
	 * @param certParams   the certificate parameters
	 * 
	 * @return The certificate
	 */
	public static X509Certificate generateX509Certificate(PrivateKey signPrivateKey, PublicKey publicKey, CertificateParameters certParams, 
						X500Principal signerPrincipal, String signAlgorithm, String providerName) { 

		X500Name certSubject = new X500Name(getCertificateAttributes(certParams));
		X500Name certIssuer = Objects.nonNull(signerPrincipal)? new X500Name(signerPrincipal.getName()) : certSubject;
		
		return generateX509Certificate(signPrivateKey, publicKey, certIssuer, certSubject, signAlgorithm, providerName, certParams.getNotBefore(), certParams.getNotAfter());
	}

	private static X509Certificate generateX509Certificate(PrivateKey signPrivateKey, PublicKey publicKey, X500Name certIssuer, X500Name certSubject, 
						String signAlgorithm, String providerName, LocalDateTime notBefore, LocalDateTime notAfter) {
		try {
			BigInteger rootSerialNum = new BigInteger(Long.toString(new SecureRandom().nextLong()));
			
			ContentSigner rootCertContentSigner = new JcaContentSignerBuilder(signAlgorithm).setProvider(providerName).build(signPrivateKey);
			X509v3CertificateBuilder rootCertBuilder = new JcaX509v3CertificateBuilder(certIssuer, rootSerialNum, getDateFromLocalDateTime(notBefore), 
													getDateFromLocalDateTime(notAfter), certSubject, publicKey);
			JcaX509ExtensionUtils rootCertExtUtils = new JcaX509ExtensionUtils();
			rootCertBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
			rootCertBuilder.addExtension(Extension.subjectKeyIdentifier, false, rootCertExtUtils.createSubjectKeyIdentifier(publicKey));
			X509CertificateHolder rootCertHolder = rootCertBuilder.build(rootCertContentSigner);	        
			return new JcaX509CertificateConverter().getCertificate(rootCertHolder);
		} catch (OperatorCreationException|NoSuchAlgorithmException | CertIOException| CertificateException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.CERTIFICATE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.CERTIFICATE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
		}
	}

	/**
	 * Converts the local date time to Date
	 * @param localDateTime
	 * @return
	 */
    private static Date getDateFromLocalDateTime(LocalDateTime localDateTime) {    	
    	return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Concatenates the cert attributes
     * @param commonName
     * @param organizationalUnit
     * @param organization
     * @param country
     * @return
     */
    private static String getCertificateAttributes(String commonName, String organizationalUnit,
			String organization, String country ) {
    	return "CN=" + commonName + ", OU =" + organizationalUnit + ",O=" + organization + ", C=" + country;
	}
	
	
	private static String getCertificateAttributes(CertificateParameters certParams) {
		return "CN=" + certParams.getCommonName() + ", OU =" + certParams.getOrganizationUnit() + ",O=" + certParams.getOrganization() 
					+ ", L=" + certParams.getLocation() + ", ST=" + certParams.getState() + ", C=" + certParams.getCountry();
    }
}