package io.mosip.kernel.vidgenerator.test.router;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.idgenerator.test.config.HibernateDaoConfig;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.uingenerator.constant.UinGeneratorErrorCode;
import io.mosip.kernel.uingenerator.dto.UinResponseDto;
import io.mosip.kernel.uingenerator.dto.UinStatusUpdateReponseDto;
import io.mosip.kernel.uingenerator.verticle.UinGeneratorVerticle;
import io.mosip.kernel.uingenerator.verticle.UinTransferVerticle;
import io.mosip.kernel.vidgenerator.constant.EventType;
import io.mosip.kernel.vidgenerator.dto.VidFetchResponseDto;
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
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

@RunWith(VertxUnitRunner.class)
public class IntegrationTest {

	private static Vertx vertx;
	private static int port;
	private static AbstractApplicationContext context;
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static Logger LOGGER;
	

	@BeforeClass
	public static void setup(TestContext testContext) throws IOException {
		System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
		objectMapper.registerModule(new JavaTimeModule());
		LOGGER = LoggerFactory.getLogger(IntegrationTest.class);
		ServerSocket socket = new ServerSocket(0);
		vertx = Vertx.vertx();
		port = 8080;
		LOGGER.info("IntegrationTest server starting on port "+port);
		System.setProperty("server.port", String.valueOf(port));
		socket.close();
		VertxOptions options = new VertxOptions();
		context = new AnnotationConfigApplicationContext(HibernateDaoConfig.class);
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
	
	private static void initUINPool() {
		LOGGER.info("Service will be started after pooling vids..");
		EventBus eventBus = vertx.eventBus();
		LOGGER.info("eventBus deployer {}", eventBus);
		eventBus.publish(UinGeneratorConstant.UIN_GENERATOR_ADDRESS, UinGeneratorConstant.GENERATE_UIN);
	}

	private static void deploy(Verticle verticle, DeploymentOptions opts, Vertx vertx) {
		vertx.deployVerticle(verticle, opts, res -> {
			if (res.failed()) {
				LOGGER.info("Failed to deploy verticle " + verticle.getClass().getSimpleName() + " " + res.cause());
			} else if (res.succeeded()) {
				LOGGER.info("Deployed verticle " + verticle.getClass().getSimpleName());

			}
		});
	}

	private static void initVIDPool() {
		LOGGER.info("Service will be started after pooling vids..");
		EventBus eventBus = vertx.eventBus();
		LOGGER.info("eventBus deployer {}", eventBus);
		eventBus.publish(EventType.INITPOOL, EventType.INITPOOL);
	}

	@AfterClass
	public static void cleanup(TestContext testContext) {
		if (vertx != null && testContext != null)
			vertx.close(testContext.asyncAssertSuccess());
		if (context != null)
			context.close();
	}

	@Test
	public void getVIDSuccessTest(TestContext context) {
		LOGGER.info("getVidSuccessTest execution...");
		Async async = context.async();
		WebClient client = WebClient.create(vertx);
		client.get(port, "localhost", "/v1/idgenerator/vid").send(ar -> {
			if (ar.succeeded()) {
				HttpResponse<Buffer> httpResponse = ar.result();
				LOGGER.info(httpResponse.bodyAsString());
				context.assertEquals(200, httpResponse.statusCode());
				try {
					ResponseWrapper<?> uinResp = objectMapper.readValue(httpResponse.bodyAsString(),
							ResponseWrapper.class);
					VidFetchResponseDto dto = objectMapper.convertValue(uinResp.getResponse(),
							VidFetchResponseDto.class);
					context.assertNotNull(dto.getVid());
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
	public void getViDExpiryEmptyTest(TestContext context) {
		LOGGER.info("getViDExpiryEmptyTest execution...");
		Async async = context.async();
		WebClient client = WebClient.create(vertx);
		client.get(port, "localhost", "/v1/idgenerator/vid?videxpiry=").send(ar -> {
			if (ar.succeeded()) {
				HttpResponse<Buffer> httpResponse = ar.result();
				LOGGER.info(httpResponse.bodyAsString());
				context.assertEquals(200, httpResponse.statusCode());
				try {
					ResponseWrapper<?> uinResp = objectMapper.readValue(httpResponse.bodyAsString(),
							ResponseWrapper.class);
					ServiceError dto = objectMapper.convertValue(uinResp.getErrors().get(0), ServiceError.class);
					context.assertEquals(dto.getErrorCode(), "KER-VID-002");
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
	public void getViDExpiryInvalidPatternTest(TestContext context) {
		LOGGER.info("getViDExpiryInvalidPatternTest execution...");
		Async async = context.async();
		WebClient client = WebClient.create(vertx);
		client.get(port, "localhost",
				"/v1/idgenerator/vid?videxpiry=" + (DateUtils.getCurrentDateTimeString().replace("Z", ""))).send(ar -> {

					if (ar.succeeded()) {
						HttpResponse<Buffer> httpResponse = ar.result();
						LOGGER.info(httpResponse.bodyAsString());
						context.assertEquals(200, httpResponse.statusCode());
						try {
							ResponseWrapper<?> uinResp = objectMapper.readValue(httpResponse.bodyAsString(),
									ResponseWrapper.class);
							ServiceError dto = objectMapper.convertValue(uinResp.getErrors().get(0),
									ServiceError.class);
							context.assertEquals(dto.getErrorCode(), "KER-VID-004");
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
	public void getViDExpiryInvalidExpiryDateTest(TestContext context) {
		LOGGER.info("getViDExpiryInvalidExpiryDateTest execution...");
		Async async = context.async();
		WebClient client = WebClient.create(vertx);
		client.get(port, "localhost", "/v1/idgenerator/vid?videxpiry=2018-12-10T06:12:52.994Z").send(ar -> {

			if (ar.succeeded()) {
				HttpResponse<Buffer> httpResponse = ar.result();
				LOGGER.info(httpResponse.bodyAsString());
				context.assertEquals(200, httpResponse.statusCode());
				try {
					ResponseWrapper<?> uinResp = objectMapper.readValue(httpResponse.bodyAsString(),
							ResponseWrapper.class);
					ServiceError dto = objectMapper.convertValue(uinResp.getErrors().get(0), ServiceError.class);
					context.assertEquals(dto.getErrorCode(), "KER-VID-003");
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
	public void getUinSuccessTest(TestContext context) {
		LOGGER.info("getUinSuccessTest execution...");
		Async async = context.async();
		WebClient client = WebClient.create(vertx);
		client.get(port, "localhost", "/v1/idgenerator/uin").send(ar -> {
			if (ar.succeeded()) {
				HttpResponse<Buffer> httpResponse = ar.result();
				LOGGER.info(httpResponse.bodyAsString());
				context.assertEquals(200, httpResponse.statusCode());
				try {
					ResponseWrapper<?> uinResp = objectMapper.readValue(httpResponse.bodyAsString(),
							ResponseWrapper.class);
					UinResponseDto dto = objectMapper.convertValue(uinResp.getResponse(), UinResponseDto.class);
					context.assertNotNull(dto.getUin());
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
	public void uinStatusUpdateSuccessTest(TestContext context) throws JsonProcessingException {
		LOGGER.info("uinStatusUpdateSuccessTest execution...");
		Async async = context.async();
		
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(
				Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM }));

		RestTemplate restTemplate = new RestTemplateBuilder().defaultMessageConverters()
				.additionalMessageConverters(converter).build();

		ResponseWrapper<?> uinResp = restTemplate.getForObject("http://localhost:" + port + "/v1/idgenerator/uin",
				ResponseWrapper.class);
		UinResponseDto dto = objectMapper.convertValue(uinResp.getResponse(), UinResponseDto.class);

		UinStatusUpdateReponseDto requestDto = new UinStatusUpdateReponseDto();
		requestDto.setUin(dto.getUin());
		requestDto.setStatus("ASSIGNED");

		RequestWrapper<UinStatusUpdateReponseDto> requestWrp = new RequestWrapper<>();
		requestWrp.setId("mosip.kernel.uinservice");
		requestWrp.setVersion("1.0");
		requestWrp.setRequest(requestDto);

		String reqJson = objectMapper.writeValueAsString(requestWrp);

		final String length = Integer.toString(reqJson.length());
		WebClient client = WebClient.create(vertx);
		client.put(port, "localhost", "/v1/idgenerator/uin").putHeader("content-type", "application/json")
				.putHeader("content-length", length).sendJson(requestWrp, response -> {
					UinStatusUpdateReponseDto uinStatusUpdateReponseDto = null;
					if (response.succeeded()) {
						HttpResponse<Buffer> httpResponse = response.result();
						LOGGER.info(httpResponse.bodyAsString());
						context.assertEquals(httpResponse.statusCode(), 200);
						try {
							uinStatusUpdateReponseDto = objectMapper.readValue(
									httpResponse.bodyAsJsonObject().getValue("response").toString(),
									UinStatusUpdateReponseDto.class);
						} catch (IOException exception) {
							exception.printStackTrace();
						}
						context.assertEquals(uinStatusUpdateReponseDto.getStatus(), UinGeneratorConstant.ASSIGNED);
						client.close();
						async.complete();
					} else {
						LOGGER.error(response.cause().getMessage());
					}
				});

	}

	@Test
	public void uinStausUpdateUinNotFoundExpTest(TestContext context) throws IOException {
		LOGGER.info("uinStausUpdateUinNotFoundExpTest execution...");
		Async async = context.async();
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(
				Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM }));

		UinStatusUpdateReponseDto requestDto = new UinStatusUpdateReponseDto();
		requestDto.setUin("7676676");
		requestDto.setStatus("ASSIGNED");

		RequestWrapper<UinStatusUpdateReponseDto> requestWrp = new RequestWrapper<>();
		requestWrp.setId("mosip.kernel.uinservice");
		requestWrp.setVersion("1.0");
		requestWrp.setRequest(requestDto);

		String reqJson = objectMapper.writeValueAsString(requestWrp);

		final String length = Integer.toString(reqJson.length());
		WebClient client = WebClient.create(vertx);
		client.put(port, "localhost", "/v1/idgenerator/uin").putHeader("content-type", "application/json")
				.putHeader("content-length", length).sendJson(requestWrp, response -> {
					if (response.succeeded()) {
						HttpResponse<Buffer> httpResponse = response.result();
						LOGGER.info(httpResponse.bodyAsString());
						context.assertEquals(httpResponse.statusCode(), 200);
						List<ServiceError> validationErrorsList = ExceptionUtils
								.getServiceErrorList(httpResponse.bodyAsString());
						assertTrue(validationErrorsList.size() > 0);
						boolean errorFound = false;
						for (ServiceError sr : validationErrorsList) {
							if (sr.getErrorCode().equals(UinGeneratorErrorCode.UIN_NOT_FOUND.getErrorCode())) {
								errorFound = true;
								break;
							}
						}
						context.assertTrue(errorFound);
						client.close();
						async.complete();
					} else {
						LOGGER.error(response.cause().getMessage());
					}
				});

	}

	@Test
	public void uinStatusUpdateStatusNotFoundExpTest(TestContext context) throws IOException {
		LOGGER.info("uinStatusUpdateStatusNotFoundExpTest execution...");
		Async async = context.async();
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(
				Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM }));

		RestTemplate restTemplate = new RestTemplateBuilder().defaultMessageConverters()
				.additionalMessageConverters(converter).build();

		ResponseWrapper<?> uinResp = restTemplate.getForObject("http://localhost:" + port + "/v1/idgenerator/uin",
				ResponseWrapper.class);
		UinResponseDto dto = objectMapper.convertValue(uinResp.getResponse(), UinResponseDto.class);

		UinStatusUpdateReponseDto requestDto = new UinStatusUpdateReponseDto();
		requestDto.setUin(dto.getUin());
		requestDto.setStatus("FailASSIGNED");

		RequestWrapper<UinStatusUpdateReponseDto> requestWrp = new RequestWrapper<>();
		requestWrp.setId("mosip.kernel.uinservice");
		requestWrp.setVersion("1.0");
		requestWrp.setRequest(requestDto);

		String reqJson = objectMapper.writeValueAsString(requestWrp);

		final String length = Integer.toString(reqJson.length());

		WebClient client = WebClient.create(vertx);
		client.put(port, "localhost", "/v1/idgenerator/uin").putHeader("content-type", "application/json")
				.putHeader("content-length", length).sendJson(requestWrp, response -> {
					if (response.succeeded()) {
						HttpResponse<Buffer> httpResponse = response.result();
						LOGGER.info(httpResponse.bodyAsString());
						context.assertEquals(httpResponse.statusCode(), 200);
						List<ServiceError> validationErrorsList = ExceptionUtils
								.getServiceErrorList(httpResponse.bodyAsString());
						context.assertTrue(validationErrorsList.size() > 0);
						boolean errorFound = false;
						for (ServiceError sr : validationErrorsList) {
							if (sr.getErrorCode().equals(UinGeneratorErrorCode.UIN_STATUS_NOT_FOUND.getErrorCode())) {
								errorFound = true;
								break;
							}
						}
						context.assertTrue(errorFound);
						client.close();
						async.complete();
					} else {
						LOGGER.error(response.cause().getMessage());
					}
				});
	}

}
