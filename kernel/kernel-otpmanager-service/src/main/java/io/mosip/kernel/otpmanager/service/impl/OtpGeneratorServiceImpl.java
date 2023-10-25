package io.mosip.kernel.otpmanager.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.otpmanager.spi.OtpGenerator;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.otpmanager.constant.OtpStatusConstants;
import io.mosip.kernel.otpmanager.dto.OtpGeneratorRequestDto;
import io.mosip.kernel.otpmanager.dto.OtpGeneratorResponseDto;
import io.mosip.kernel.otpmanager.entity.OtpEntity;
import io.mosip.kernel.otpmanager.repository.OtpRepository;
import io.mosip.kernel.otpmanager.util.OtpManagerUtils;
import io.mosip.kernel.otpmanager.util.OtpProvider;

/**
 * This class provides the implementation for the methods of OtpGeneratorService
 * interface.
 *
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@RefreshScope
@Service
public class OtpGeneratorServiceImpl implements OtpGenerator<OtpGeneratorRequestDto, OtpGeneratorResponseDto> {
	/**
	 * The reference that autowires OtpRepository class.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OtpGeneratorServiceImpl.class);
	@Autowired
	private OtpRepository otpRepository;

	@Autowired
	private OtpProvider otpProvider;

	@Value("${mosip.kernel.otp.key-freeze-time}")
	String keyFreezeTime;

	@Value("${mosip.kernel.otp.default-length}")
	int otpLength;

	@Value("${mosip.kernel.otp.mac-algorithm}")
	String macAlgorithm;

	@Value("${javax.persistence.jdbc.url}")
	String jdbcUrl;
	
	@Value("${spring.profiles.active}")
	String activeProfile;
	
	@Value("${mosip.kernel.auth.proxy-otp-value:111111}")
	String localOtp;
	
	@Value("${mosip.kernel.auth.proxy-otp}")
	private boolean isProxytrue;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.otpmanagerservice.service.OtpGeneratorService#getOtp(org.
	 * mosip.kernel.otpmanagerservice.dto.OtpGeneratorRequestDto)
	 */
	@Override
	public OtpGeneratorResponseDto getOtp(OtpGeneratorRequestDto otpDto) {

		String generatedOtp;

		/*
		 * Creating object to return the generation response.
		 */
		OtpGeneratorResponseDto response = new OtpGeneratorResponseDto();
		/*
		 * Skipping OTP creation for local profile 
		 */
        if(activeProfile.equalsIgnoreCase("local")) {
        	response.setOtp(localOtp);
			response.setStatus(OtpStatusConstants.GENERATION_SUCCESSFUL.getProperty());
		    return response;
        }
		
		/*
		 * Checking whether the key exists in the repository.
		 */
		String keyHash = OtpManagerUtils.getHash(otpDto.getKey());
		OtpEntity keyCheck = otpRepository.findById(OtpEntity.class, keyHash);
		if ((keyCheck != null) && (keyCheck.getStatusCode().equals(OtpStatusConstants.KEY_FREEZED.getProperty()))
				&& (OtpManagerUtils.timeDifferenceInSeconds(keyCheck.getUpdatedDtimes(),
						LocalDateTime.now(ZoneId.of("UTC"))) <= Integer.parseInt(keyFreezeTime))) {
			response.setOtp(OtpStatusConstants.SET_AS_NULL_IN_STRING.getProperty());
			response.setStatus(OtpStatusConstants.BLOCKED_USER.getProperty());
		} else {
			if (isProxytrue){
				generatedOtp = localOtp;
			} else {
				generatedOtp = otpProvider.computeOtp(otpDto.getKey(), otpLength, macAlgorithm);
			}
			
			OtpEntity otp = new OtpEntity();
			otp.setId(keyHash);
			otp.setValidationRetryCount(0);
			otp.setOtp(OtpManagerUtils.getHash(generatedOtp));
			otpRepository.save(otp);
			response.setOtp(generatedOtp);
			response.setStatus(OtpStatusConstants.GENERATION_SUCCESSFUL.getProperty());
		}
		return response;
	}
}
