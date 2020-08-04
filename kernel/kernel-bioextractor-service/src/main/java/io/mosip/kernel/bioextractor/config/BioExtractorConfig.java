package io.mosip.kernel.bioextractor.config;

import static io.mosip.kernel.bioextractor.config.constant.BioExtractorConfigKeyConstants.CONCURRENT_JOB_LIMIT_KEY;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import io.mosip.kernel.bioextractor.config.constant.BioExtractorConstants;

@Configuration
public class BioExtractorConfig {
	@Value("${" + CONCURRENT_JOB_LIMIT_KEY  + ":" + BioExtractorConstants.CONCURRENT_JOB_LIMIT_VALUE_DEFAULT+ "}")
	private int concurrentJobLimit;
	
	@Bean
	Executor getExecutor() {
		SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
		simpleAsyncTaskExecutor.setThreadNamePrefix("BioExtractor-");
		simpleAsyncTaskExecutor.setConcurrencyLimit(concurrentJobLimit);
		return simpleAsyncTaskExecutor;
	}
	
}
