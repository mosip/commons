package io.mosip.kernel.keymanager.softhsm.util;

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
import io.mosip.kernel.keymanager.softhsm.constant.KeymanagerConstant;
import io.mosip.kernel.keymanager.softhsm.constant.KeymanagerErrorCode;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Certificate utility to generate and sign X509 Certificate
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 *@author Nagarjuna
 */
public class CertificateUtility {
	
	/**
	 * Private constructor for CertificateUtility
	 */
	private CertificateUtility() {
	}
	
	/**
	 * Generate X509 Certificate
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
	public static X509Certificate generateX509Certificate(KeyPair keyPair, String commonName, String organizationalUnit,
			String organization, String country, LocalDateTime validityFrom, LocalDateTime validityTo, String providerName) {
    	X509Certificate rootCert;    	
		try {
			BigInteger rootSerialNum = new BigInteger(Long.toString(new SecureRandom().nextLong()));
	    	X500Name rootCertIssuer = new X500Name(getCertificateAttributes(commonName, organizationalUnit, organization, country));
	        X500Name rootCertSubject = rootCertIssuer;
	        
			ContentSigner rootCertContentSigner = new JcaContentSignerBuilder(KeymanagerConstant.SIGNATURE_ALGORITHM).setProvider(providerName).build(keyPair.getPrivate());
			X509v3CertificateBuilder rootCertBuilder = new JcaX509v3CertificateBuilder(rootCertIssuer, rootSerialNum, getDateFromLocalDateTime(validityFrom), getDateFromLocalDateTime(validityTo), rootCertSubject, keyPair.getPublic());
	        JcaX509ExtensionUtils rootCertExtUtils = new JcaX509ExtensionUtils();
	        rootCertBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
			rootCertBuilder.addExtension(Extension.subjectKeyIdentifier, false, rootCertExtUtils.createSubjectKeyIdentifier(keyPair.getPublic()));
			X509CertificateHolder rootCertHolder = rootCertBuilder.build(rootCertContentSigner);	        
			rootCert = new JcaX509CertificateConverter().setProvider(providerName).getCertificate(rootCertHolder);
		} catch (OperatorCreationException|NoSuchAlgorithmException | CertIOException| CertificateException e) {
			throw new KeystoreProcessingException(KeymanagerErrorCode.CERTIFICATE_PROCESSING_ERROR.getErrorCode(),
					KeymanagerErrorCode.CERTIFICATE_PROCESSING_ERROR.getErrorMessage() + e.getMessage(), e);
		}

    	return rootCert;
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
}