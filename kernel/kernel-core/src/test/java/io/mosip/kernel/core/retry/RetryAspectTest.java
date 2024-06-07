package io.mosip.kernel.core.retry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.SocketTimeoutException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.kernel.core.util.RetryUtil;

/**
 * @author Loganathan Sekar
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class, RetryConfig.class,
		RetryListenerImpl.class, RetryUtil.class, RetryAspect.class })
@EnableConfigurationProperties
@PropertySource("classpath:application-test.properties")
public class RetryAspectTest {

	@Autowired
	Environment env;

	@Autowired
	RetryAspect retryAspect;

	@Test
	public void testRetryPolicy_Testsuccess() throws Exception {
		FailingMockOperationWithRetry<?> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(() -> new SocketTimeoutException()));
		Object result = FailingMockOperationWithRetry.get();
		assertNotNull(result);
	}

	@Test(expected = SocketTimeoutException.class)
	public void testRetryPolicy_TestFailureWithRetryableException() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperationWithRetry<SocketTimeoutException> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(retryLimit + 10, () -> new SocketTimeoutException()));
		Object result;
		try {
			result = FailingMockOperationWithRetry.get();
		} catch (SocketTimeoutException e) {
			assertEquals(FailingMockOperationWithRetry.getExecutedTimes(), retryLimit + 1);
			throw e;
		}
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRetryPolicy_TestFailureWithNonRetryableException() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperationWithRetry<IllegalArgumentException> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(retryLimit + 10, () -> new IllegalArgumentException()));
		Object result;
		try {
			result = FailingMockOperationWithRetry.get();
		} catch (IllegalArgumentException e) {
			assertEquals(FailingMockOperationWithRetry.getExecutedTimes(), 1);
			throw e;
		}
		fail();
	}

	@Test
	public void testRetryPolicy_TestSuccessAfterfewFailures() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		int retryCount = retryLimit / 2;
		FailingMockOperationWithRetry<?> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(retryCount, () -> new SocketTimeoutException()));
		Object result = FailingMockOperationWithRetry.get();
		assertEquals(FailingMockOperationWithRetry.getExecutedTimes(), retryCount + 1);
		assertNotNull(result);

	}

	@Test
	public void testRetryPolicy_TestSuccessAfterFailuresTillMaxRetries() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperationWithRetry<?> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(retryLimit, () -> new SocketTimeoutException()));
		Object result = FailingMockOperationWithRetry.get();
		assertEquals(FailingMockOperationWithRetry.getExecutedTimes(), retryLimit + 1);
		assertNotNull(result);

	}

	@Test
	public void testRetryPolicy_TestsuccessWithRunnable() throws Exception {
		FailingMockOperationWithRetry<?> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(() -> new SocketTimeoutException()));
		FailingMockOperationWithRetry.run();
	}

	@Test(expected = SocketTimeoutException.class)
	public void testRetryPolicy_TestFailureWithRunnable() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperationWithRetry<SocketTimeoutException> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(retryLimit + 10, () -> new SocketTimeoutException()));
		try {
			FailingMockOperationWithRetry.run();
		} catch (SocketTimeoutException e) {
			assertEquals(FailingMockOperationWithRetry.getExecutedTimes(), retryLimit + 1);
			throw e;
		}
		fail();
	}

	@Test
	public void testRetryPolicy_TestsuccessWithConsumer() throws Exception {
		FailingMockOperationWithRetry<?> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(() -> new SocketTimeoutException()));
		FailingMockOperationWithRetry.accept("Hello");
	}

	@Test(expected = SocketTimeoutException.class)
	public void testRetryPolicy_TestFailureWithConsumer() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperationWithRetry<SocketTimeoutException> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(retryLimit + 10, () -> new SocketTimeoutException()));
		Object result;
		try {
			FailingMockOperationWithRetry.accept("Hello");
		} catch (SocketTimeoutException e) {
			assertEquals(FailingMockOperationWithRetry.getExecutedTimes(), retryLimit + 1);
			throw e;
		}
		fail();
	}

	@Test
	public void testRetryPolicy_TestsuccessWithFunction() throws Exception {
		FailingMockOperationWithRetry<?> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(() -> new SocketTimeoutException()));
		Object result = FailingMockOperationWithRetry.apply("Hello");
		assertEquals(result, "Hello");
	}

	@Test(expected = SocketTimeoutException.class)
	public void testRetryPolicy_TestFailureWithFunction() throws Exception {
		Integer retryLimit = env.getProperty(RetryConfigKeyConstants.KERNEL_RETRY_ATTEMPTS_LIMIT, Integer.class);
		FailingMockOperationWithRetry<SocketTimeoutException> FailingMockOperationWithRetry = getAspectProxy(
				new FailingMockOperationWithRetry<>(retryLimit + 10, () -> new SocketTimeoutException()));
		Object result;
		try {
			result = FailingMockOperationWithRetry.apply("Hello");
		} catch (SocketTimeoutException e) {
			assertEquals(FailingMockOperationWithRetry.getExecutedTimes(), retryLimit + 1);
			throw e;
		}
		fail();
	}

	private <T> T getAspectProxy(T target) {
		AspectJProxyFactory factory = new AspectJProxyFactory(target);
		factory.addAspect(retryAspect);
		T proxy = factory.getProxy();
		return proxy;
	}

}