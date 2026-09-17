package io.mosip.kernel.licensekeygenerator.misp.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.idgenerator.spi.MISPLicenseGenerator;
import io.mosip.kernel.licensekeygenerator.misp.util.MISPLicenseKeyGeneratorUtil;

/**
 * Implementation class for {@link MISPLicenseGenerator}.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@Component
public class MISPLicenseKeyGeneratorImpl implements MISPLicenseGenerator<String> {

	/**
	 * Autowired reference for {@link MISPLicenseKeyGeneratorUtil}.
	 */
	@Autowired
	MISPLicenseKeyGeneratorUtil mispLicenseGeneratorUtil;

	/**
	 * Delegates to {@link MISPLicenseKeyGeneratorUtil#generate()}.
	 *
	 * @return alphanumeric license key of configured length
	 * @throws io.mosip.kernel.licensekeygenerator.misp.exception.LengthNotSameException if the generated key length does not match configuration
	 */
	@Override
	public String generateLicense() {
		return mispLicenseGeneratorUtil.generate();
	}
}
