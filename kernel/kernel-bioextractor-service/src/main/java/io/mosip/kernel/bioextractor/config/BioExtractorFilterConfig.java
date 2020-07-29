package io.mosip.kernel.bioextractor.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.kernel.bioextractor.filter.BiometricExtractorFilter;

/**
 * The configuration for adding filters.
 *
 * @author Manoj SP
 */
@Configuration
public class BioExtractorFilterConfig {

	/**
	 * Gets the otp filter.
	 *
	 * @return the otp filter
	 */
	@Bean
	public FilterRegistrationBean<BiometricExtractorFilter> getOtpFilter() {
		FilterRegistrationBean<BiometricExtractorFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new BiometricExtractorFilter());
		registrationBean.addUrlPatterns("/extracttemplates/*");
		return registrationBean;
	}

}
