package io.mosip.kernel.partnercertservice.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keymanagerservice.exception.KeymanagerServiceException;
import io.mosip.kernel.keymanager.hsm.util.CertificateUtility;
import io.mosip.kernel.keymanagerservice.dto.SignatureCertificate;
import io.mosip.kernel.keymanagerservice.entity.PartnerCertificateStore;
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

    @Value("${mosip.kernel.partner.issuer.certificate.allowed.grace.duration:30}")
    private int gracePeriod;

    @Value("${mosip.kernel.partner.resign.ftm.domain.certs:false}")
    private boolean resignFTMDomainCerts;

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

        List<Certificate> certList = parseCertificateData(certificateData);
        int certsCount = certList.size();
        LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                    PartnerCertManagerConstants.EMPTY, "Number of Certificates inputed: " + certsCount);
        
        String partnerDomain = validateAllowedDomains(caCertRequestDto.getPartnerDomain());
        boolean foundError = false;
        boolean uploadedCert = false;
        for(Certificate cert : certList) {
            X509Certificate reqX509Cert = (X509Certificate) cert;

            String certThumbprint = PartnerCertificateManagerUtil.getCertificateThumbprint(reqX509Cert);
            boolean certExist = certDBHelper.isCertificateExist(certThumbprint, partnerDomain);
            if (certExist) {
                LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                        PartnerCertManagerConstants.EMPTY, "CA/sub-CA certificate already exists in Store.");
                if (certsCount == 1) {
                     throw new PartnerCertManagerException(
                           PartnerCertManagerErrorConstants.CERTIFICATE_EXIST_ERROR.getErrorCode(),
                           PartnerCertManagerErrorConstants.CERTIFICATE_EXIST_ERROR.getErrorMessage());
                }
                foundError = true;
                continue;
            }

            boolean validDates = PartnerCertificateManagerUtil.isCertificateDatesValid(reqX509Cert);
            if (!validDates) {
                LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                        PartnerCertManagerConstants.EMPTY, "Certificate Dates are not valid.");
                if(certsCount == 1) {
                    throw new PartnerCertManagerException(
                            PartnerCertManagerErrorConstants.CERTIFICATE_DATES_NOT_VALID.getErrorCode(),
                            PartnerCertManagerErrorConstants.CERTIFICATE_DATES_NOT_VALID.getErrorMessage());
                }
                foundError = true;
                continue;
            }
            
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
                uploadedCert = true;
            } else {
                LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                        PartnerCertManagerConstants.EMPTY, "Adding Intermediate Certificates in store.");

                boolean certValid = validateCertificatePath(reqX509Cert, partnerDomain);
                if (!certValid) {
                     LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                           PartnerCertManagerConstants.EMPTY,
                           "Sub-CA Certificate not allowed to upload as root CA is not available.");
                     if (certsCount == 1) {
                        throw new PartnerCertManagerException(PartnerCertManagerErrorConstants.ROOT_CA_NOT_FOUND.getErrorCode(),
                            PartnerCertManagerErrorConstants.ROOT_CA_NOT_FOUND.getErrorMessage());
                     }
                     foundError = true;
                     continue;
                }
                String issuerId = certDBHelper.getIssuerCertId(certIssuer);
                String certId = UUID.randomUUID().toString();
                certDBHelper.storeCACertificate(certId, certSubject, certIssuer, issuerId, reqX509Cert, certThumbprint,
                        partnerDomain);
                uploadedCert = true;
            }
        }
        CACertificateResponseDto responseDto = new CACertificateResponseDto();
        if (uploadedCert && (certsCount == 1 || !foundError))
            responseDto.setStatus(PartnerCertManagerConstants.SUCCESS_UPLOAD);
        else if (uploadedCert && foundError)
            responseDto.setStatus(PartnerCertManagerConstants.PARTIAL_SUCCESS_UPLOAD);
        else 
            responseDto.setStatus(PartnerCertManagerConstants.UPLOAD_FAILED);
        responseDto.setTimestamp(DateUtils.getUTCCurrentDateTime());
        return responseDto;
    }

    private List<Certificate> parseCertificateData(String certificateData) {
        List<Certificate> certList = new ArrayList<>();
        try {
            X509Certificate reqX509Cert = (X509Certificate) keymanagerUtil.convertToCertificate(certificateData);
            certList.add(reqX509Cert);
            return certList;
        } catch(KeymanagerServiceException kse) {
            LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                PartnerCertManagerConstants.EMPTY, "Ignore this exception, the exception thrown when certificate is not" 
                                        + " able to parse, may be p7b certificate data inputed.");
        }
        // Try to Parse as P7B file.
        byte[] p7bBytes = CryptoUtil.decodeBase64(certificateData);
        try (ByteArrayInputStream certStream = new ByteArrayInputStream(p7bBytes)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection<?> p7bCertList = cf.generateCertificates(certStream);
            p7bCertList.forEach(cert -> {
                certList.add((Certificate)cert);
            });
            Collections.reverse(certList);
            return certList;
        } catch(CertificateException | IOException  exp) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                PartnerCertManagerConstants.EMPTY, "Error Parsing P7B Certificate data.", exp);
        }
        throw new PartnerCertManagerException(
                PartnerCertManagerErrorConstants.INVALID_CERTIFICATE.getErrorCode(),
                PartnerCertManagerErrorConstants.INVALID_CERTIFICATE.getErrorMessage());
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
    private List<? extends Certificate> getCertificateTrustPath(X509Certificate reqX509Cert, String partnerDomain) {

        try {
            Map<String, Set<?>> trustStoreMap = certDBHelper.getTrustAnchors(partnerDomain);
            Set<TrustAnchor> rootTrustAnchors = (Set<TrustAnchor>) trustStoreMap
                    .get(PartnerCertManagerConstants.TRUST_ROOT);
            Set<X509Certificate> interCerts = (Set<X509Certificate>) trustStoreMap
                    .get(PartnerCertManagerConstants.TRUST_INTER);
            
            LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.CERT_TRUST_VALIDATION,
                    PartnerCertManagerConstants.EMPTY, "Certificate Trust Path Validation for domain: " + partnerDomain);
            LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.CERT_TRUST_VALIDATION,
                    PartnerCertManagerConstants.EMPTY, "Total Number of ROOT Trust Found: " + rootTrustAnchors.size());
            LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.CERT_TRUST_VALIDATION,
                    PartnerCertManagerConstants.EMPTY, "Total Number of INTERMEDIATE Trust Found: " + interCerts.size());

            X509CertSelector certToVerify = new X509CertSelector();
            certToVerify.setCertificate(reqX509Cert);

            PKIXBuilderParameters pkixBuilderParams = new PKIXBuilderParameters(rootTrustAnchors, certToVerify);
            pkixBuilderParams.setRevocationEnabled(false);

            CertStore interCertStore = CertStore.getInstance("Collection",
                    new CollectionCertStoreParameters(interCerts));
            pkixBuilderParams.addCertStore(interCertStore);

            // Building the cert path and verifying the certification chain
            CertPathBuilder certPathBuilder = CertPathBuilder.getInstance("PKIX");
            //certPathBuilder.build(pkixBuilderParams);
            PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) certPathBuilder.build(pkixBuilderParams);

            X509Certificate rootCert = result.getTrustAnchor().getTrustedCert();
            List<? extends Certificate> certList = result.getCertPath().getCertificates();
            List<Certificate> trustCertList = new ArrayList<>();
            certList.stream().forEach(cert -> {
                trustCertList.add(cert);
            }); 
            trustCertList.add(rootCert);
            return trustCertList;
        } catch (CertPathBuilderException | InvalidAlgorithmParameterException | NoSuchAlgorithmException exp) {
            LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_CA_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Ignore this exception, the exception thrown when trust validation failed.");
        }
        return null;
    }

    private boolean validateCertificatePath(X509Certificate reqX509Cert, String partnerDomain) {
        List<? extends Certificate> certList = getCertificateTrustPath(reqX509Cert, partnerDomain);
        return Objects.nonNull(certList);
    }

    @Override
    public PartnerCertificateResponseDto uploadPartnerCertificate(PartnerCertificateRequestDto partnerCertRequesteDto) {
        LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                PartnerCertManagerConstants.EMPTY, "Uploading Partner Certificate.");

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

        LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                PartnerCertManagerConstants.EMPTY, "Partner certificate upload for domain: " + partnerDomain);

        validateBasicPartnerCertParams(reqX509Cert, certThumbprint, reqOrgName, partnerDomain);

        List<? extends Certificate> certList = getCertificateTrustPath(reqX509Cert, partnerDomain);
        //boolean certValid = validateCertificatePath(reqX509Cert, partnerDomain);
        if (Objects.isNull(certList)) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Partner Certificate not allowed to upload as root CA/Intermediate CAs are not found in trust cert path.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.ROOT_INTER_CA_NOT_FOUND.getErrorCode(),
                    PartnerCertManagerErrorConstants.ROOT_INTER_CA_NOT_FOUND.getErrorMessage());
        }
        validateOtherPartnerCertParams(reqX509Cert, reqOrgName);

        String certSubject = PartnerCertificateManagerUtil
                .formatCertificateDN(reqX509Cert.getSubjectX500Principal().getName());
        String certIssuer = PartnerCertificateManagerUtil
                .formatCertificateDN(reqX509Cert.getIssuerX500Principal().getName());
        String issuerId = certDBHelper.getIssuerCertId(certIssuer);
        String certId = UUID.randomUUID().toString();

        X509Certificate rootCert = (X509Certificate) keymanagerUtil.convertToCertificate(
                                        keymanagerService.getCertificate(PartnerCertManagerConstants.ROOT_APP_ID, 
                                                        Optional.of(PartnerCertManagerConstants.EMPTY)).getCertificate());
        String timestamp = DateUtils.getUTCCurrentDateTimeString();
        SignatureCertificate certificateResponse = keymanagerService.getSignatureCertificate(masterSignKeyAppId,
                                                        Optional.of(PartnerCertManagerConstants.EMPTY), timestamp);
        X509Certificate pmsCert = certificateResponse.getCertificateEntry().getChain()[0];

        X509Certificate resignedCert = reSignPartnerKey(reqX509Cert, certificateResponse, partnerDomain);
        String signedCertData = keymanagerUtil.getPEMFormatedData(resignedCert);
        certDBHelper.storePartnerCertificate(certId, certSubject, certIssuer, issuerId, reqX509Cert, certThumbprint,
                reqOrgName, partnerDomain, signedCertData);
        
        String p7bCertChain = PartnerCertificateManagerUtil.buildP7BCertificateChain(certList, resignedCert, partnerDomain, 
                        resignFTMDomainCerts, rootCert, pmsCert);
        PartnerCertificateResponseDto responseDto = new PartnerCertificateResponseDto();
        responseDto.setCertificateId(certId);
        responseDto.setSignedCertificateData(p7bCertChain);
        responseDto.setTimestamp(DateUtils.getUTCCurrentDateTime());
        return responseDto;
    }

    private void validateBasicPartnerCertParams(X509Certificate reqX509Cert, String certThumbprint, String reqOrgName,
            String partnerDomain) {
        boolean certExist = certDBHelper.isPartnerCertificateExist(certThumbprint, partnerDomain);
        if (certExist) {
            LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY, "Partner certificate already exists in Store.");
            // Commented below throw clause because renewal of certificate should be allowed for existing certificates.
            // Added one more condition to check certificate validity is in allowed date range.
            /* throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.CERTIFICATE_EXIST_ERROR.getErrorCode(),
                    PartnerCertManagerErrorConstants.CERTIFICATE_EXIST_ERROR.getErrorMessage()); */
        }

        boolean validDates = PartnerCertificateManagerUtil.isCertificateDatesValid(reqX509Cert);
        if (!validDates) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY, "Certificate Dates are not valid.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.CERTIFICATE_DATES_NOT_VALID.getErrorCode(),
                    PartnerCertManagerErrorConstants.CERTIFICATE_DATES_NOT_VALID.getErrorMessage());
        }

        boolean validDuration = PartnerCertificateManagerUtil.isCertificateValidForDuration(reqX509Cert, issuerCertDuration, gracePeriod);
        if (!validDuration) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY, "Certificate Dates are not in allowed range.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.CERTIFICATE_DATES_NOT_VALID.getErrorCode(),
                    PartnerCertManagerErrorConstants.CERTIFICATE_DATES_NOT_VALID.getErrorMessage());
        }

        boolean selfSigned = PartnerCertificateManagerUtil.isSelfSignedCertificate(reqX509Cert);
        if (selfSigned) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                        PartnerCertManagerConstants.EMPTY, "Self Signed Certificate are not in allowed as Partner.");
            throw new PartnerCertManagerException(
                        PartnerCertManagerErrorConstants.SELF_SIGNED_CERT_NOT_ALLOWED.getErrorCode(),
                        PartnerCertManagerErrorConstants.SELF_SIGNED_CERT_NOT_ALLOWED.getErrorMessage());
        }
    }

    private void validateOtherPartnerCertParams(X509Certificate reqX509Cert, String reqOrgName) {
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

    private X509Certificate reSignPartnerKey(X509Certificate reqX509Cert, SignatureCertificate certificateResponse, 
                        String partnerDomain) {

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
        boolean encKeyUsage = partnerDomain.equalsIgnoreCase(PartnerCertManagerConstants.AUTH_DOMAIN);
        return (X509Certificate) CertificateUtility.generateX509Certificate(signPrivateKey, partnerPublicKey, certParams,
                signerPrincipal, signAlgorithm, keyStore.getKeystoreProviderName(), encKeyUsage);
    }

    @Override
    public PartnerCertDownloadResponeDto getPartnerCertificate(PartnerCertDownloadRequestDto certDownloadRequestDto) {

        LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.GET_PARTNER_CERT,
                PartnerCertManagerConstants.EMPTY, "Get Partner Certificate Request.");

        String partnetCertId = certDownloadRequestDto.getPartnerCertId();

        if (!PartnerCertificateManagerUtil.isValidCertificateID(partnetCertId)) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY,
                    "Invalid Certificate ID provided to get the partner certificate.");
            throw new PartnerCertManagerException(
                    PartnerCertManagerErrorConstants.INVALID_CERTIFICATE_ID.getErrorCode(),
                    PartnerCertManagerErrorConstants.INVALID_CERTIFICATE_ID.getErrorMessage());
        }
        PartnerCertificateStore partnerCertStore = certDBHelper.getPartnerCert(partnetCertId);
        if (Objects.isNull(partnerCertStore)) {
            LOGGER.error(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.UPLOAD_PARTNER_CERT,
                    PartnerCertManagerConstants.EMPTY, "Partner Certificate not found for the provided ID.");
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
        LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.CERT_TRUST_VALIDATION,
                PartnerCertManagerConstants.EMPTY, "Certificate Trust Path Validation.");

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

        LOGGER.info(PartnerCertManagerConstants.SESSIONID, PartnerCertManagerConstants.CERT_TRUST_VALIDATION,
                PartnerCertManagerConstants.EMPTY, "Certificate Trust Path Validation for domain: " + partnerDomain);

        boolean certValid = validateCertificatePath(reqX509Cert, partnerDomain);
        CertificateTrustResponeDto responseDto = new CertificateTrustResponeDto();
        responseDto.setStatus(certValid);     
        return responseDto;
    }
    
}