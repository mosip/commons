package io.mosip.kernel.saltgenerator.config;

import static io.mosip.kernel.saltgenerator.constant.SaltGeneratorConstant.CHUNK_SIZE;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

import io.mosip.kernel.saltgenerator.entity.SaltEntity;

/**
 * The Class SaltGeneratorJobConfig - provides configuration for Salt generator Job.
 *
 * @author Manoj SP
 */
@Configuration
@DependsOn("saltGeneratorConfig")
public class SaltGeneratorJobConfig {
	
	@Autowired
	private Environment env;

	
	/** The listener. */
	@Autowired
	private JobExecutionListener listener;
	
	/** The reader. */
	@Autowired
	private ItemReader<SaltEntity> reader;
	
	/** The writer. */
	@Autowired
	private ItemWriter<SaltEntity> writer;
	
	/**
	 * Job.
	 *
	 * @param step the step
	 * @return the job
	 */
	@Bean
	public Job job(JobRepository jobRepository,Step step) {
		return new JobBuilder("job", jobRepository)
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(step)
				.end()
				.build();
	}
	
	
	/**
	 * Step.
	 *
	 * @return the step
	 */
	@Bean
	public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new StepBuilder("step", jobRepository)
				.<SaltEntity, SaltEntity>chunk(env.getProperty(CHUNK_SIZE.getValue(), Integer.class),
						transactionManager)
				.reader(reader)
				.writer(writer)
				.build();
	}
}
