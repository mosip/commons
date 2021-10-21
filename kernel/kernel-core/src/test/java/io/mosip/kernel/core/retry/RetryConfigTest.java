package io.mosip.kernel.core.retry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.sql.SQLTimeoutException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Loganathan Sekar
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class, RetryConfig.class,
		RetryListenerImpl.class })
@EnableConfigurationProperties
@PropertySource("classpath:application-test.properties")
public class RetryConfigTest {

	@Autowired
	RetryTemplate retryTemplate;

	@Autowired
	Environment env;

	@Test
	public void testRetryPolicy_Testsuccess() throws Exception {
		FailingMockOperation<?> failingMockOperation = new FailingMockOperation<>(() -> new SocketTimeoutException());
		Object result = retryTemplate.execute(c -> failingMockOperation.get());
		assertNotNull(result);
	}

	@Test(expected = SocketTimeoutException.class)
	public void testRetryPolicy_TestFailureWithRetryableException() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperation<SocketTimeoutException> failingMockOperation = new FailingMockOperation<>(retryLimit + 10, () -> new SocketTimeoutException());
		Object result;
		try {
			result = retryTemplate.execute(c -> failingMockOperation.get());
		} catch (SocketTimeoutException e) {
			assertEquals(failingMockOperation.getExecutedTimes(), retryLimit + 1);
			throw e;
		}
		fail();
	}
	
	@Test(expected = FileNotFoundException.class)
	public void testRetryPolicy_TestFailureWithNoRetryWithNotIncludedForRetryableException() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperation<FileNotFoundException> failingMockOperation = new FailingMockOperation<>(retryLimit + 10, () -> new FileNotFoundException());
		Object result;
		try {
			result = retryTemplate.execute(c -> failingMockOperation.get());
		} catch (FileNotFoundException e) {
			assertEquals(failingMockOperation.getExecutedTimes(), 1);
			throw e;
		}
		fail();
	}
	
	@Test(expected = SQLTimeoutException.class)
	public void testRetryPolicy_TestFailureWithNoRetryWithSubclassExceptionNotRetryable() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperation<SQLTimeoutException> failingMockOperation = new FailingMockOperation<>(retryLimit + 10, () -> new SQLTimeoutException());
		Object result;
		try {
			result = retryTemplate.execute(c -> failingMockOperation.get());
		} catch (SQLTimeoutException e) {
			assertEquals(failingMockOperation.getExecutedTimes(), 1);
			throw e;
		}
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRetryPolicy_TestFailureWithNonRetryableException() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperation<IllegalArgumentException> failingMockOperation = new FailingMockOperation<>(retryLimit + 10,
				() -> new IllegalArgumentException());
		Object result;
		try {
			result = retryTemplate.execute(c -> failingMockOperation.get());
		} catch (IllegalArgumentException e) {
			assertEquals(failingMockOperation.getExecutedTimes(), 1);
			throw e;
		}
		fail();
	}

	@Test
	public void testRetryPolicy_TestSuccessAfterfewFailures() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		int retryCount = retryLimit / 2;
		FailingMockOperation<?> failingMockOperation = new FailingMockOperation<>(retryCount,
				() -> new SocketTimeoutException());
		Object result = retryTemplate.execute(c -> failingMockOperation.get());
		assertEquals(failingMockOperation.getExecutedTimes(), retryCount + 1);
		assertNotNull(result);

	}
	
	@Test
	public void testRetryPolicy_TestSuccessAfterFailuresTillMaxRetries() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperation<?> failingMockOperation = new FailingMockOperation<>(retryLimit,
				() -> new SocketTimeoutException());
		Object result = retryTemplate.execute(c -> failingMockOperation.get());
		assertEquals(failingMockOperation.getExecutedTimes(), retryLimit + 1);
		assertNotNull(result);

	}

}