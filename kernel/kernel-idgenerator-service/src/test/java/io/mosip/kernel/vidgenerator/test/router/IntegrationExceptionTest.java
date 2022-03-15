package io.mosip.kernel.vidgenerator.test.router;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilClientException;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilException;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.idgenerator.test.config.ExceptionDaoConfig;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.uingenerator.constant.UinGeneratorErrorCode;
import io.mosip.kernel.uingenerator.verticle.UinGeneratorVerticle;
import io.mosip.kernel.uingenerator.verticle.UinTransferVerticle;
import io.mosip.kernel.vidgenerator.constant.EventType;
import io.mosip.kernel.vidgenerator.exception.VidGeneratorServiceException;
import io.mosip.kernel.vidgenerator.service.VidService;
import io.mosip.kernel.vidgenerator.verticle.VidExpiryVerticle;
import io.mosip.kernel.vidgenerator.verticle.VidPoolCheckerVerticle;
import io.mosip.kernel.vidgenerator.verticle.VidPopulatorVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

@RunWith(VertxUnitRunner.class)
public class IntegrationExceptionTest {

	private  Vertx vertx;
	private  int port;
	private  AbstractApplicationContext context;
	private  ObjectMapper objectMapper = new ObjectMapper();
	private static Logger LOGGER;
	

	@Before
	public void setup(TestContext testContext) throws IOException {
		System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
		objectMapper.registerModule(new JavaTimeModule());
		LOGGER = LoggerFactory.getLogger(IntegrationExceptionTest.class);
		ServerSocket socket = new ServerSocket(0);
		vertx = Vertx.vertx();
		port = socket.getLocalPort();
		System.setProperty("server.port", String.valueOf(port));
		socket.close();
		VertxOptions options = new VertxOptions();
		context = new AnnotationConfigApplicationContext(ExceptionDaoConfig.class);
		DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);

		Verticle[] workerVerticles = { new VidPoolCheckerVerticle(context), new VidPopulatorVerticle(context),
				new VidExpiryVerticle(context) };
		Stream.of(workerVerticles).forEach(verticle -> deploy(verticle, workerOptions, vertx));
		vertx.setTimer(1000, handler -> initVIDPool());
		
		Verticle[] uinVerticles = { new UinGeneratorVerticle(context),new UinTransferVerticle(context)};
		Stream.of(uinVerticles).forEach(verticle -> vertx.deployVerticle(verticle, stringAsyncResult -> {
			if (stringAsyncResult.succeeded()) {
				LOGGER.info("Successfully deployed: " + verticle.getClass().getSimpleName());
			} else {
				LOGGER.info("Failed to deploy:" + verticle.getClass().getSimpleName() + "\nCause: "
						+ stringAsyncResult.cause());
			}
		}));
		vertx.setTimer(1000, handler -> initUINPool());
	}
	
	private  void initUINPool() {
		LOGGER.info("Service will be started after pooling vids..");
		EventBus eventBus = vertx.eventBus();
		LOGGER.info("eventBus deployer {}", eventBus);
		eventBus.publish(UinGeneratorConstant.UIN_GENERATOR_ADDRESS, UinGeneratorConstant.GENERATE_UIN);
	}

	private  void deploy(Verticle verticle, DeploymentOptions opts, Vertx vertx) {
		vertx.deployVerticle(verticle, opts, res -> {
			if (res.failed()) {
				LOGGER.info("Failed to deploy verticle " + verticle.getClass().getSimpleName() + " " + res.cause());
			} else if (res.succeeded()) {
				LOGGER.info("Deployed verticle " + verticle.getClass().getSimpleName());

			}
		});
	}

	private void initVIDPool() {
		LOGGER.info("Service will be started after pooling vids..");
		EventBus eventBus = vertx.eventBus();
		LOGGER.info("eventBus deployer {}", eventBus);
		eventBus.publish(EventType.INITPOOL, EventType.INITPOOL);
	}

	@After
	public void cleanup(TestContext testContext) {
		if (vertx != null && testContext != null)
			vertx.close(testContext.asyncAssertSuccess());
		if (context != null)
			context.close();
	}

	
	@Test
	public void getUinSignatureUtilExceptionTest(TestContext context) {
		LOGGER.info("getUinSignatureUtilExceptionTest execution...");
		Async async = context.async();
		AnnotationConfigApplicationContext c = (AnnotationConfigApplicationContext)this.context;
		SignatureUtil signatureUtil =c.getBean(SignatureUtil.class);
		SignatureUtilException signatureUtilException = new SignatureUtilException(UinGeneratorErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(), "mock exception");
		when(signatureUtil.sign(Mockito.anyString())).thenThrow(signatureUtilException);
		WebClient client = WebClient.create(vertx);
		client.get(port, "localhost", "/v1/idgenerator/uin").send(ar -> {
			if (ar.succeeded()) {
				HttpResponse<Buffer> httpResponse = ar.result();
				LOGGER.info(httpResponse.bodyAsString());
				context.assertEquals(200, httpResponse.statusCode());
				try {
					ResponseWrapper<?> uinResp = objectMapper.readValue(httpResponse.bodyAsString(),
							ResponseWrapper.class);
					context.assertEquals(uinResp.getErrors().get(0).getErrorCode(), UinGeneratorErrorCode.INTERNAL_SERVER_ERROR.getErrorCode());
				} catch (IOException e) {
					e.printStackTrace();
				}
				client.close();
				async.complete();
			} else {
				LOGGER.error(ar.cause().getMessage());
			}
		});	
	}
	
	@Test
	public void getUinSignatureUtilClientExceptionTest(TestContext context) {
		LOGGER.info("getUinSignatureUtilClientExceptionTest execution...");
		Async async = context.async();
		AnnotationConfigApplicationContext c = (AnnotationConfigApplicationContext)this.context;
		SignatureUtil signatureUtil =c.getBean(SignatureUtil.class);
		ServiceError serviceError = new ServiceError("KER-SGN-001", "mock signature exception");
		List<ServiceError> errors = new ArrayList<>();
		errors.add(serviceError);
		SignatureUtilClientException signatureUtilClientException = new SignatureUtilClientException(errors);
		when(signatureUtil.sign(Mockito.anyString())).thenThrow(signatureUtilClientException);
		WebClient client = WebClient.create(vertx);
		client.get(port, "localhost", "/v1/idgenerator/uin").send(ar -> {
			if (ar.succeeded()) {
				HttpResponse<Buffer> httpResponse = ar.result();
				LOGGER.info(httpResponse.bodyAsString());
				context.assertEquals(200, httpResponse.statusCode());
				try {
					ResponseWrapper<?> uinResp = objectMapper.readValue(httpResponse.bodyAsString(),
							ResponseWrapper.class);
					context.assertEquals(uinResp.getErrors().get(0).getErrorCode(), "KER-SGN-001");
				} catch (IOException e) {
					e.printStackTrace();
				}
				client.close();
				async.complete();
			} else {
				LOGGER.error(ar.cause().getMessage());
			}
		});	
	}

}
