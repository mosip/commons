package io.mosip.kernel.cryptosignature.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "${mosip.auth.adapter.impl.basepackage}" })
public class ResponseSignatureConfig {
	
}
