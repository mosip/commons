package io.mosip.kernel.partnercertservice.service.impl;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.security.auth.x500.X500Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.keymanager.model.CertificateParameters;
import io.mosip.kernel.core.keymanager.spi.KeyStore;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keymanager.hsm.util.CertificateUtility;
import io.mosip.kernel.keymanagerservice.dto.SignatureCertificate;
import io.mosip.kernel.keymanagerservice.entity.PartnerCertificateStore;
import io.mosip.kernel.keymanagerservice.helper.KeymanagerDBHelper;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.keymanagerservice.service.KeymanagerService;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;
import io.mosip.kernel.partnercertservice.constant.PartnerCertManagerConstants;
import io.mosip.kernel.partnercertservice.constant.PartnerCertManagerErrorConstants;
import io.mosip.kernel.partnercertservice.dto.CACertificateRequestDto;
import io.mosip.kernel.partnercertservice.dto.CACertificateResponseDto;
import io.mosip.kernel.partnercertservice.dto.CertificateTrustRequestDto;
import io.mosip.kernel.partnercertservice.dto.CertificateTrustResponeDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertDownloadRequestDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertDownloadResponeDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertificateRequestDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertificateResponseDto;
import io.mosip.kernel.partnercertservice.exception.PartnerCertManagerException;
import io.mosip.kernel.partnercertservice.helper.PartnerCertManagerDBHelper;
import io.mosip.kernel.partnercertservice.service.spi.PartnerCertificateManagerService;
import io.mosip.kernel.partnercertservice.util.PartnerCertificateManagerUtil;

/**
 * This class provides the implementation for the methods of
 * PartnerCertificateManagerService interface.
 *
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */
@Service
@Transactional
public class PartnerCertificateManagerServiceImpl implements PartnerCertificateManagerService {

    private static final Logger LOGGER = KeymanagerLogger.getLogger(PartnerCertificateManagerServiceImpl.class);

    @Value("${mosip.kernel.partner.sign.masterkey.application.id}")
    private String masterSignKeyAppId;

    @Value("${mosip.kernel.partner.allowed.domains}")
    private String partnerAllowedDomains;

    @Value("${mosip.kernel.certificate.sign.algorithm:SHA256withRSA}")
    private String signAlgorithm;

    @Value("${mosip.kernel.partner.issuer.certificate.duration.years:1}")
    private int issuerCertDuration;
        
    /**
     * Utility to generate Metadata
     */
    @Autowired
    KeymanagerUtil keymanagerUtil;

    /**
     * Utility to generate Metadata
     */
    @Autowired
    PartnerCertManagerDBHelper certDBHelper;

    /**
     * KeymanagerDBHelper instance to handle all DB operations
     */
    @Autowired
    private KeymanagerDBHelper dbHelper;

    /**
     * Keystore instance to handles and store cryptographic keys.
     */
    @Autowired
    private KeyStore keyStore;

    @Autowired
    private KeymanagerService keymanagerService;

    @Override
    public CACertificateResponseDto uploadCACertificate(CACertificateRequestDto caCertRequestDto) {
        LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                PartnerCertManagerConstants.EMPTY, "Uploading CA/Sub-CA Certificate.");

        String certificateData = caCertRequestDto.getCertificateData();
        if (!keymanagerUtil.isValidCertificateData(certificateData)) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Invalid Certificate Data provided to upload the ca/sub-ca certificate.");
            throw new PartnerCertManagerException(PartnerCertManagerErrorConstants.INVALID_CERTIFICATE.getErrorCode(),
                    PartnerCertManagerErrorConstants.INVALID_CERTIFICATE.getErrorMessage());
        }
        X509Certificate reqX509Cert = (X509Certificate) keymanagerUtil.convertToCertificate(certificateData);
        String certThumbprint = PartnerCertificateManagerUtil.getCertificateThumbprint(reqX509Cert);
        String partnerDomain = validateAllowedDomains(caCertRequestDto.getPartnerDomain());

        validateBasicCACertParams(reqX509Cert, certThumbprint, partnerDomain);

        String certSubject = PartnerCertificateManagerUtil
                .formatCertificateDN(reqX509Cert.getSubjectX500Principal().getName());
        String certIssuer = PartnerCertificateManagerUtil
                .formatCertificateDN(reqX509Cert.getIssuerX500Principal().getName());
        boolean selfSigned = PartnerCertificateManagerUtil.isSelfSignedCertificate(reqX509Cert);

        if (selfSigned) {
            LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                    PartnerCertManagerConstants.EMPTY, "Adding Self-signed Certificate in store.");
            String certId = UUID.randomUUID().toString();
            certDBHelper.storeCACertificate(certId, certSubject, certIssuer, certId, reqX509Cert, certThumbprint,
                    partnerDomain);
        } else {
            LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                    PartnerCertManagerConstants.EMPTY, "Adding Intermediate Certificates in store.");

            boolean certValid = validateCertificatePath(reqX509Cert, partnerDomain);
            if (!certValid) {
                LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                        PartnerCertManagerConstants.EMPTY,
                        "Sub-CA Certificate not allowed to upload as root CA is not available.");
                throw new PartnerCertManagerException(PartnerCertManagerErrorConstants.ROOT_CA_NOT_FOUND.getErrorCode(),
                        PartnerCertManagerErrorConstants.ROOT_CA_NOT_FOUND.getErrorMessage());
            }
            String issuerId = certDBHelper.getIssuerCertId(certIssuer);
            String certId = UUID.randomUUID().toString();
            certDBHelper.storeCACertificate(certId, certSubject, certIssuer, issuerId, reqX509Cert, certThumbprint,
                    partnerDomain);
        }
        CACertificateResponseDto responseDto = new CACertificateResponseDto();
        responseDto.setStatus(PartnerCertManagerConstants.SUCCESS_UPLOAD);
        responseDto.setTimestamp(DateUtils.getUTCCurrentDateTime());
        return responseDto;
    }

    private void validateBasicCACertParams(X509Certificate reqX509Cert, String certThumbprint, String partnerDomain) {
        boolean certExist = certDBHelper.isCertificateExist(certThumbprint, partnerDomain);
        if (certExist) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                    PartnerCertManagerConstants.EMPTY, "CA/sub-CA certificate already exists in Store.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.CERTIFICATE_EXIST_ERROR.getErrorCode(),
                    PartnerCertManagerErrorConstants.CERTIFICATE_EXIST_ERROR.getErrorMessage());
        }

        boolean validDates = PartnerCertificateManagerUtil.isCertificateDatesValid(reqX509Cert);
        if (!validDates) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                    PartnerCertManagerConstants.EMPTY, "Certificate Dates are not valid.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.CERTIFICATE_DATES_NOT_VALID.getErrorCode(),
                    PartnerCertManagerErrorConstants.CERTIFICATE_DATES_NOT_VALID.getErrorMessage());
        }
    }

    private String validateAllowedDomains(String partnerDomain) {
        String validPartnerDomain = Stream.of(partnerAllowedDomains.split(",")).map(String::trim)
                .filter(allowedDomain -> allowedDomain.equalsIgnoreCase(partnerDomain)).findFirst()
                .orElseThrow(() -> new PartnerCertManagerException(
                        PartnerCertManagerErrorConstants.INVALID_PARTNER_DOMAIN.getErrorCode(),
                        PartnerCertManagerErrorConstants.INVALID_PARTNER_DOMAIN.getErrorMessage()));
        return validPartnerDomain.toUpperCase();
    }

    @SuppressWarnings("unchecked")
    private boolean validateCertificatePath(X509Certificate reqX509Cert, String partnerDomain) {

        try {
            Map<String, Set<?>> trustStoreMap = certDBHelper.getTrustAnchors(partnerDomain);
            Set<TrustAnchor> rootTrustAnchors = (Set<TrustAnchor>) trustStoreMap
                    .get(PartnerCertManagerConstants.TRUST_ROOT);
            Set<X509Certificate> interCerts = (Set<X509Certificate>) trustStoreMap
                    .get(PartnerCertManagerConstants.TRUST_INTER);

            X509CertSelector certToVerify = new X509CertSelector();
            certToVerify.setCertificate(reqX509Cert);

            PKIXBuilderParameters pkixBuilderParams = new PKIXBuilderParameters(rootTrustAnchors, certToVerify);
            pkixBuilderParams.setRevocationEnabled(false);

            CertStore interCertStore = CertStore.getInstance("Collection",
                    new CollectionCertStoreParameters(interCerts));
            pkixBuilderParams.addCertStore(interCertStore);

            // Building the cert path and verifying the certification chain
            CertPathBuilder certPathBuilder = CertPathBuilder.getInstance("PKIX");
            certPathBuilder.build(pkixBuilderParams);
            /* PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) */
            /*
             * List<? extends Certificate> certList =
             * result.getCertPath().getCertificates();
             */
            return true;
        } catch (CertPathBuilderException | InvalidAlgorithmParameterException | NoSuchAlgorithmException exp) {
            LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Ignore this exception, the exception thrown when trust validation failed.");
        }
        return false;
    }

    @Override
    public PartnerCertificateResponseDto uploadPartnerCertificate(PartnerCertificateRequestDto partnerCertRequesteDto) {

        String certificateData = partnerCertRequesteDto.getCertificateData();
        if (!keymanagerUtil.isValidCertificateData(certificateData)) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Invalid Certificate Data provided to upload the partner certificate.");
            throw new PartnerCertManagerException(PartnerCertManagerErrorConstants.INVALID_CERTIFICATE.getErrorCode(),
                    PartnerCertManagerErrorConstants.INVALID_CERTIFICATE.getErrorMessage());
        }
        X509Certificate reqX509Cert = (X509Certificate) keymanagerUtil.convertToCertificate(certificateData);
        String certThumbprint = PartnerCertificateManagerUtil.getCertificateThumbprint(reqX509Cert);
        String reqOrgName = partnerCertRequesteDto.getOrganizationName();
        String partnerDomain = validateAllowedDomains(partnerCertRequesteDto.getPartnerDomain());

        validateBasicPartnerCertParams(reqX509Cert, certThumbprint, reqOrgName, partnerDomain);

        String certSubject = PartnerCertificateManagerUtil
                .formatCertificateDN(reqX509Cert.getSubjectX500Principal().getName());
        String certIssuer = PartnerCertificateManagerUtil
                .formatCertificateDN(reqX509Cert.getIssuerX500Principal().getName());
        String issuerId = certDBHelper.getIssuerCertId(certIssuer);
        String certId = UUID.randomUUID().toString();

        X509Certificate resignedCert = reSignPartnerKey(reqX509Cert);
        String signedCertData = keymanagerUtil.getPEMFormatedData(resignedCert);
        certDBHelper.storePartnerCertificate(certId, certSubject, certIssuer, issuerId, reqX509Cert, certThumbprint,
                reqOrgName, partnerDomain, signedCertData);
        PartnerCertificateResponseDto responseDto = new PartnerCertificateResponseDto();
        responseDto.setCertificateId(certId);
        responseDto.setSignedCertificateData(signedCertData);
        responseDto.setTimestamp(DateUtils.getUTCCurrentDateTime());
        return responseDto;
    }

    private void validateBasicPartnerCertParams(X509Certificate reqX509Cert, String certThumbprint, String reqOrgName,
            String partnerDomain) {
        boolean certExist = certDBHelper.isPartnerCertificateExist(certThumbprint, partnerDomain);
        if (certExist) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY, "Partner certificate already exists in Store.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.CERTIFICATE_EXIST_ERROR.getErrorCode(),
                    PartnerCertManagerErrorConstants.CERTIFICATE_EXIST_ERROR.getErrorMessage());
        }

        boolean validDates = PartnerCertificateManagerUtil.isCertificateDatesValid(reqX509Cert);
        if (!validDates) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY, "Certificate Dates are not valid.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.CERTIFICATE_DATES_NOT_VALID.getErrorCode(),
                    PartnerCertManagerErrorConstants.CERTIFICATE_DATES_NOT_VALID.getErrorMessage());
        }

        boolean certValid = validateCertificatePath(reqX509Cert, partnerDomain);
        if (!certValid) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Partner Certificate not allowed to upload as root CA/Intermediate CAs are not available.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.ROOT_INTER_CA_NOT_FOUND.getErrorCode(),
                    PartnerCertManagerErrorConstants.ROOT_INTER_CA_NOT_FOUND.getErrorMessage());
        }

        int certVersion = reqX509Cert.getVersion();
        if (certVersion != 3) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Partner Certificate version not valid, the version has to be V3");
            throw new PartnerCertManagerException(PartnerCertManagerErrorConstants.INVALID_CERT_VERSION.getErrorCode(),
                    PartnerCertManagerErrorConstants.INVALID_CERT_VERSION.getErrorMessage());
        }

        String certOrgName = PartnerCertificateManagerUtil.getCertificateOrgName(reqX509Cert.getSubjectX500Principal());
        if (!certOrgName.equals(reqOrgName)) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Partner Certificate Organization and Partner Organization Name not matching.");
            throw new PartnerCertManagerException(PartnerCertManagerErrorConstants.PARTNER_ORG_NOT_MATCH.getErrorCode(),
                    PartnerCertManagerErrorConstants.PARTNER_ORG_NOT_MATCH.getErrorMessage());
        }

        String keyAlgorithm = reqX509Cert.getPublicKey().getAlgorithm();
        if (keyAlgorithm.equalsIgnoreCase(PartnerCertManagerConstants.RSA_ALGORITHM)) {
            int keySize = ((java.security.interfaces.RSAPublicKey) reqX509Cert.getPublicKey()).getModulus().bitLength();
            if (keySize < PartnerCertManagerConstants.RSA_MIN_KEY_SIZE) {
                LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                        PartnerCertManagerConstants.EMPTY, "Partner Certificate key is less than allowed size.");
                throw new PartnerCertManagerException(
                        PartnerCertManagerErrorConstants.CERT_KEY_NOT_ALLOWED.getErrorCode(),
                        PartnerCertManagerErrorConstants.CERT_KEY_NOT_ALLOWED.getErrorMessage());
            }
        }

        String signatureAlgorithm = reqX509Cert.getSigAlgName();
        if (!signatureAlgorithm.toUpperCase().startsWith(PartnerCertManagerConstants.HASH_SHA2)) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY, "Signature Algorithm not supported.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.CERT_SIGNATURE_ALGO_NOT_ALLOWED.getErrorCode(),
                    PartnerCertManagerErrorConstants.CERT_SIGNATURE_ALGO_NOT_ALLOWED.getErrorMessage());
        }
    }

    private X509Certificate reSignPartnerKey(X509Certificate reqX509Cert) {

        String timestamp = DateUtils.getUTCCurrentDateTimeString();
	SignatureCertificate certificateResponse = keymanagerService.getSignatureCertificate(masterSignKeyAppId,
                                        Optional.of(PartnerCertManagerConstants.EMPTY), timestamp);
        LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT, "KeyAlias",
                "Found Master Key Alias: " + certificateResponse.getAlias());
                
        PrivateKey signPrivateKey = certificateResponse.getCertificateEntry().getPrivateKey();
        X509Certificate signCert = certificateResponse.getCertificateEntry().getChain()[0];
        X500Principal signerPrincipal = signCert.getSubjectX500Principal();

        X500Principal subjectPrincipal = reqX509Cert.getSubjectX500Principal();
        PublicKey partnerPublicKey = reqX509Cert.getPublicKey();
        
        int noOfDays = PartnerCertManagerConstants.YEAR_DAYS * issuerCertDuration;
        LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT, "Cert Duration",
                "Calculated Signed Certficiate Number of Days for expire: " + noOfDays);
        LocalDateTime notBeforeDate = DateUtils.getUTCCurrentDateTime(); 
        LocalDateTime notAfterDate = notBeforeDate.plus(noOfDays, ChronoUnit.DAYS);
        CertificateParameters certParams = PartnerCertificateManagerUtil.getCertificateParameters(subjectPrincipal,
                notBeforeDate, notAfterDate);
        return (X509Certificate) CertificateUtility.generateX509Certificate(signPrivateKey, partnerPublicKey, certParams,
                signerPrincipal, signAlgorithm, keyStore.getKeystoreProviderName());
    }

    @Override
    public PartnerCertDownloadResponeDto getPartnerCertificate(PartnerCertDownloadRequestDto certDownloadRequestDto) {

        String partnetCertId = certDownloadRequestDto.getPartnerCertId();

        if (!PartnerCertificateManagerUtil.isValidCertificateID(partnetCertId)) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Invalid Certificate ID provided to get the partner certificate.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.INVALID_CERTIFICATE_ID.getErrorCode(),
                    PartnerCertManagerErrorConstants.INVALID_CERTIFICATE_ID.getErrorMessage());
        }
        PartnerCertificateStore partnerCertStore = certDBHelper.getPartnetCert(partnetCertId);
        if (Objects.isNull(partnerCertStore)) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY, "Partner Certificate ID not found.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.PARTNER_CERT_ID_NOT_FOUND.getErrorCode(),
                    PartnerCertManagerErrorConstants.PARTNER_CERT_ID_NOT_FOUND.getErrorMessage());
        }

        PartnerCertDownloadResponeDto responseDto = new PartnerCertDownloadResponeDto();
        responseDto.setCertificateData(partnerCertStore.getSignedCertData());
        responseDto.setTimestamp(DateUtils.getUTCCurrentDateTime());
        return responseDto;
    }

    @Override
    public CertificateTrustResponeDto verifyCertificateTrust(CertificateTrustRequestDto certificateTrustRequestDto) {
        
        String certificateData = certificateTrustRequestDto.getCertificateData();
        if (!keymanagerUtil.isValidCertificateData(certificateData)) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Invalid Certificate Data provided to verify partner certificate trust.");
            throw new PartnerCertManagerException(PartnerCertManagerErrorConstants.INVALID_CERTIFICATE.getErrorCode(),
                    PartnerCertManagerErrorConstants.INVALID_CERTIFICATE.getErrorMessage());
        }
        X509Certificate reqX509Cert = (X509Certificate) keymanagerUtil.convertToCertificate(certificateData);
        String partnerDomain = validateAllowedDomains(certificateTrustRequestDto.getPartnerDomain());

        boolean certValid = validateCertificatePath(reqX509Cert, partnerDomain);
        CertificateTrustResponeDto responseDto = new CertificateTrustResponeDto();
        responseDto.setStatus(certValid);     
        return responseDto;
    }
    
}