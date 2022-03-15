/*
 * package io.mosip.kernel.pridgenerator.test.router;
 * 
 * import static org.mockito.Mockito.when;
 * 
 * import java.io.IOException; import java.net.ServerSocket; import
 * java.util.stream.Stream;
 * 
 * import javax.annotation.PostConstruct;
 * 
 * import org.junit.AfterClass; import org.junit.BeforeClass; import
 * org.junit.Test; import org.junit.runner.RunWith; import org.mockito.Mockito;
 * import
 * org.springframework.context.annotation.AnnotationConfigApplicationContext;
 * import org.springframework.context.support.AbstractApplicationContext;
 * 
 * import com.fasterxml.jackson.databind.ObjectMapper; import
 * com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
 * 
 * import io.mosip.kernel.core.exception.ServiceError; import
 * io.mosip.kernel.core.http.ResponseWrapper; import
 * io.mosip.kernel.pridgenerator.constant.EventType; import
 * io.mosip.kernel.pridgenerator.constant.PRIDGeneratorErrorCode; import
 * io.mosip.kernel.pridgenerator.dto.PridFetchResponseDto; import
 * io.mosip.kernel.pridgenerator.entity.PridEntity; import
 * io.mosip.kernel.pridgenerator.exception.PridGeneratorServiceException; import
 * io.mosip.kernel.pridgenerator.service.PridService; import
 * io.mosip.kernel.pridgenerator.test.config.HibernateExceptionDaoConfig; import
 * io.mosip.kernel.pridgenerator.verticle.PridPoolCheckerVerticle; import
 * io.mosip.kernel.pridgenerator.verticle.PridPopulatorVerticle; import
 * io.vertx.core.DeploymentOptions; import io.vertx.core.Verticle; import
 * io.vertx.core.Vertx; import io.vertx.core.VertxOptions; import
 * io.vertx.core.buffer.Buffer; import io.vertx.core.eventbus.EventBus; import
 * io.vertx.core.logging.Logger; import io.vertx.core.logging.LoggerFactory;
 * import io.vertx.core.logging.SLF4JLogDelegateFactory; import
 * io.vertx.ext.unit.Async; import io.vertx.ext.unit.TestContext; import
 * io.vertx.ext.unit.junit.VertxUnitRunner; import
 * io.vertx.ext.web.client.HttpResponse; import
 * io.vertx.ext.web.client.WebClient;
 * 
 * @RunWith(VertxUnitRunner.class) public class IntegrationExceptionTest {
 * 
 * private static Vertx vertx; private static int port; private static
 * AbstractApplicationContext context; private static ObjectMapper objectMapper
 * = new ObjectMapper(); private static Logger LOGGER;
 * 
 * 
 * @BeforeClass public static void setup(TestContext testContext) throws
 * IOException { System.setProperty("vertx.logger-delegate-factory-class-name",
 * SLF4JLogDelegateFactory.class.getName()); objectMapper.registerModule(new
 * JavaTimeModule()); LOGGER =
 * LoggerFactory.getLogger(IntegrationExceptionTest.class); ServerSocket socket
 * = new ServerSocket(0); vertx = Vertx.vertx(); port = socket.getLocalPort();
 * System.setProperty("server.port", String.valueOf(port)); socket.close();
 * VertxOptions options = new VertxOptions(); context = new
 * AnnotationConfigApplicationContext(HibernateExceptionDaoConfig.class);
 * DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);
 * vertx = Vertx.vertx(options); Verticle[] workerVerticles = { new
 * PridPoolCheckerVerticle(context), new PridPopulatorVerticle(context) };
 * Stream.of(workerVerticles).forEach(verticle -> deploy(verticle,
 * workerOptions, vertx)); AnnotationConfigApplicationContext c =
 * (AnnotationConfigApplicationContext)context; PridService pridService =
 * c.getBean(PridService.class);
 * when(pridService.savePRID(Mockito.any(PridEntity.class))).thenReturn(true);
 * when(pridService.fetchPridCount(Mockito.anyString())).thenReturn(6L);
 * vertx.setTimer(1000, handler -> initPool()); }
 * 
 * private static void deploy(Verticle verticle, DeploymentOptions opts, Vertx
 * vertx) { vertx.deployVerticle(verticle, opts, res -> { if (res.failed()) {
 * LOGGER.info("Failed to deploy verticle " +
 * verticle.getClass().getSimpleName() + " " + res.cause()); } else if
 * (res.succeeded()) { LOGGER.info("Deployed verticle " +
 * verticle.getClass().getSimpleName());
 * 
 * } }); }
 * 
 * @PostConstruct private static void initPool() {
 * LOGGER.info("Service will be started after pooling prids.."); EventBus
 * eventBus = vertx.eventBus(); LOGGER.info("eventBus deployer {}", eventBus);
 * eventBus.publish(EventType.INITPOOL, EventType.INITPOOL); }
 * 
 * @AfterClass public static void cleanup(TestContext testContext) { if (vertx
 * != null && testContext != null)
 * vertx.close(testContext.asyncAssertSuccess()); if (context != null)
 * context.close(); }
 * 
 * @Test public void getPRIDExceptionTest(TestContext context) {
 * LOGGER.info("getPRIDSuccessTest execution..."); Async async =
 * context.async(); AnnotationConfigApplicationContext c =
 * (AnnotationConfigApplicationContext)this.context; PridService pridService =
 * c.getBean(PridService.class); WebClient client = WebClient.create(vertx);
 * when(pridService.fetchPrid()).thenThrow(new
 * PridGeneratorServiceException(PRIDGeneratorErrorCode.PRID_NOT_AVAILABLE.
 * getErrorCode(),"mock-message")); client.get(port, "localhost",
 * "/v1/pridgenerator/prid").send(ar -> { if (ar.succeeded()) {
 * HttpResponse<Buffer> httpResponse = ar.result();
 * LOGGER.info(httpResponse.bodyAsString()); context.assertEquals(200,
 * httpResponse.statusCode()); //TODO assert error code but response is null
 * change to empty client.close(); async.complete(); } else {
 * LOGGER.error(ar.cause().getMessage()); } }); }
 * 
 * @Test public void getViDExpiryEmptyTest(TestContext context) {
 * LOGGER.info("getViDExpiryEmptyTest execution..."); Async async =
 * context.async(); WebClient client = WebClient.create(vertx); client.get(port,
 * "localhost", "/v1/idgenerator/vid?videxpiry=").send(ar -> { if
 * (ar.succeeded()) { HttpResponse<Buffer> httpResponse = ar.result();
 * LOGGER.info(httpResponse.bodyAsString()); context.assertEquals(200,
 * httpResponse.statusCode()); try { ResponseWrapper<?> uinResp =
 * objectMapper.readValue(httpResponse.bodyAsString(), ResponseWrapper.class);
 * ServiceError dto = objectMapper.convertValue(uinResp.getErrors().get(0),
 * ServiceError.class); context.assertEquals(dto.getErrorCode(), "KER-VID-002");
 * } catch (IOException e) { e.printStackTrace(); } client.close();
 * async.complete(); } else { LOGGER.error(ar.cause().getMessage()); } }); }
 * 
 * @Test public void getViDExpiryInvalidPatternTest(TestContext context) {
 * LOGGER.info("getViDExpiryInvalidPatternTest execution..."); Async async =
 * context.async(); WebClient client = WebClient.create(vertx); client.get(port,
 * "localhost", "/v1/idgenerator/vid?videxpiry=" +
 * (DateUtils.getCurrentDateTimeString().replace("Z", ""))).send(ar -> {
 * 
 * if (ar.succeeded()) { HttpResponse<Buffer> httpResponse = ar.result();
 * LOGGER.info(httpResponse.bodyAsString()); context.assertEquals(200,
 * httpResponse.statusCode()); try { ResponseWrapper<?> uinResp =
 * objectMapper.readValue(httpResponse.bodyAsString(), ResponseWrapper.class);
 * ServiceError dto = objectMapper.convertValue(uinResp.getErrors().get(0),
 * ServiceError.class); context.assertEquals(dto.getErrorCode(), "KER-VID-004");
 * } catch (IOException e) { e.printStackTrace(); } client.close();
 * async.complete(); } else { LOGGER.error(ar.cause().getMessage()); } }); }
 * 
 * @Test public void getViDExpiryInvalidExpiryDateTest(TestContext context) {
 * LOGGER.info("getViDExpiryInvalidExpiryDateTest execution..."); Async async =
 * context.async(); WebClient client = WebClient.create(vertx); client.get(port,
 * "localhost",
 * "/v1/idgenerator/vid?videxpiry=2018-12-10T06:12:52.994Z").send(ar -> {
 * 
 * if (ar.succeeded()) { HttpResponse<Buffer> httpResponse = ar.result();
 * LOGGER.info(httpResponse.bodyAsString()); context.assertEquals(200,
 * httpResponse.statusCode()); try { ResponseWrapper<?> uinResp =
 * objectMapper.readValue(httpResponse.bodyAsString(), ResponseWrapper.class);
 * ServiceError dto = objectMapper.convertValue(uinResp.getErrors().get(0),
 * ServiceError.class); context.assertEquals(dto.getErrorCode(), "KER-VID-003");
 * } catch (IOException e) { e.printStackTrace(); } client.close();
 * async.complete(); } else { LOGGER.error(ar.cause().getMessage()); } }); }
 * 
 * @Test public void getUinSuccessTest(TestContext context) {
 * LOGGER.info("getUinSuccessTest execution..."); Async async = context.async();
 * WebClient client = WebClient.create(vertx); client.get(port, "localhost",
 * "/v1/idgenerator/uin").send(ar -> { if (ar.succeeded()) {
 * HttpResponse<Buffer> httpResponse = ar.result();
 * LOGGER.info(httpResponse.bodyAsString()); context.assertEquals(200,
 * httpResponse.statusCode()); try { ResponseWrapper<?> uinResp =
 * objectMapper.readValue(httpResponse.bodyAsString(), ResponseWrapper.class);
 * UinResponseDto dto = objectMapper.convertValue(uinResp.getResponse(),
 * UinResponseDto.class); context.assertNotNull(dto.getUin()); } catch
 * (IOException e) { e.printStackTrace(); } client.close(); async.complete(); }
 * else { LOGGER.error(ar.cause().getMessage()); } }); }
 * 
 * @Test public void uinStatusUpdateSuccessTest(TestContext context) throws
 * JsonProcessingException {
 * LOGGER.info("uinStatusUpdateSuccessTest execution..."); Async async =
 * context.async();
 * 
 * MappingJackson2HttpMessageConverter converter = new
 * MappingJackson2HttpMessageConverter(); converter.setSupportedMediaTypes(
 * Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON,
 * MediaType.APPLICATION_OCTET_STREAM }));
 * 
 * RestTemplate restTemplate = new
 * RestTemplateBuilder().defaultMessageConverters()
 * .additionalMessageConverters(converter).build();
 * 
 * ResponseWrapper<?> uinResp = restTemplate.getForObject("http://localhost:" +
 * port + "/v1/idgenerator/uin", ResponseWrapper.class); UinResponseDto dto =
 * objectMapper.convertValue(uinResp.getResponse(), UinResponseDto.class);
 * 
 * UinStatusUpdateReponseDto requestDto = new UinStatusUpdateReponseDto();
 * requestDto.setUin(dto.getUin()); requestDto.setStatus("ASSIGNED");
 * 
 * RequestWrapper<UinStatusUpdateReponseDto> requestWrp = new
 * RequestWrapper<>(); requestWrp.setId("mosip.kernel.uinservice");
 * requestWrp.setVersion("1.0"); requestWrp.setRequest(requestDto);
 * 
 * String reqJson = objectMapper.writeValueAsString(requestWrp);
 * 
 * final String length = Integer.toString(reqJson.length()); WebClient client =
 * WebClient.create(vertx); client.put(port, "localhost",
 * "/v1/idgenerator/uin").putHeader("content-type", "application/json")
 * .putHeader("content-length", length).sendJson(requestWrp, response -> {
 * UinStatusUpdateReponseDto uinStatusUpdateReponseDto = null; if
 * (response.succeeded()) { HttpResponse<Buffer> httpResponse =
 * response.result(); LOGGER.info(httpResponse.bodyAsString());
 * context.assertEquals(httpResponse.statusCode(), 200); try {
 * uinStatusUpdateReponseDto = objectMapper.readValue(
 * httpResponse.bodyAsJsonObject().getValue("response").toString(),
 * UinStatusUpdateReponseDto.class); } catch (IOException exception) {
 * exception.printStackTrace(); }
 * context.assertEquals(uinStatusUpdateReponseDto.getStatus(),
 * UinGeneratorConstant.ASSIGNED); client.close(); async.complete(); } else {
 * LOGGER.error(response.cause().getMessage()); } });
 * 
 * }
 * 
 * @Test public void uinStausUpdateUinNotFoundExpTest(TestContext context)
 * throws IOException {
 * LOGGER.info("uinStausUpdateUinNotFoundExpTest execution..."); Async async =
 * context.async(); MappingJackson2HttpMessageConverter converter = new
 * MappingJackson2HttpMessageConverter(); converter.setSupportedMediaTypes(
 * Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON,
 * MediaType.APPLICATION_OCTET_STREAM }));
 * 
 * UinStatusUpdateReponseDto requestDto = new UinStatusUpdateReponseDto();
 * requestDto.setUin("7676676"); requestDto.setStatus("ASSIGNED");
 * 
 * RequestWrapper<UinStatusUpdateReponseDto> requestWrp = new
 * RequestWrapper<>(); requestWrp.setId("mosip.kernel.uinservice");
 * requestWrp.setVersion("1.0"); requestWrp.setRequest(requestDto);
 * 
 * String reqJson = objectMapper.writeValueAsString(requestWrp);
 * 
 * final String length = Integer.toString(reqJson.length()); WebClient client =
 * WebClient.create(vertx); client.put(port, "localhost",
 * "/v1/idgenerator/uin").putHeader("content-type", "application/json")
 * .putHeader("content-length", length).sendJson(requestWrp, response -> { if
 * (response.succeeded()) { HttpResponse<Buffer> httpResponse =
 * response.result(); LOGGER.info(httpResponse.bodyAsString());
 * context.assertEquals(httpResponse.statusCode(), 200); List<ServiceError>
 * validationErrorsList = ExceptionUtils
 * .getServiceErrorList(httpResponse.bodyAsString());
 * assertTrue(validationErrorsList.size() > 0); boolean errorFound = false; for
 * (ServiceError sr : validationErrorsList) { if
 * (sr.getErrorCode().equals(UinGeneratorErrorCode.UIN_NOT_FOUND.getErrorCode())
 * ) { errorFound = true; break; } } context.assertTrue(errorFound);
 * client.close(); async.complete(); } else {
 * LOGGER.error(response.cause().getMessage()); } });
 * 
 * }
 * 
 * @Test public void uinStatusUpdateStatusNotFoundExpTest(TestContext context)
 * throws IOException {
 * LOGGER.info("uinStatusUpdateStatusNotFoundExpTest execution..."); Async async
 * = context.async(); MappingJackson2HttpMessageConverter converter = new
 * MappingJackson2HttpMessageConverter(); converter.setSupportedMediaTypes(
 * Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON,
 * MediaType.APPLICATION_OCTET_STREAM }));
 * 
 * RestTemplate restTemplate = new
 * RestTemplateBuilder().defaultMessageConverters()
 * .additionalMessageConverters(converter).build();
 * 
 * ResponseWrapper<?> uinResp = restTemplate.getForObject("http://localhost:" +
 * port + "/v1/idgenerator/uin", ResponseWrapper.class); UinResponseDto dto =
 * objectMapper.convertValue(uinResp.getResponse(), UinResponseDto.class);
 * 
 * UinStatusUpdateReponseDto requestDto = new UinStatusUpdateReponseDto();
 * requestDto.setUin(dto.getUin()); requestDto.setStatus("FailASSIGNED");
 * 
 * RequestWrapper<UinStatusUpdateReponseDto> requestWrp = new
 * RequestWrapper<>(); requestWrp.setId("mosip.kernel.uinservice");
 * requestWrp.setVersion("1.0"); requestWrp.setRequest(requestDto);
 * 
 * String reqJson = objectMapper.writeValueAsString(requestWrp);
 * 
 * final String length = Integer.toString(reqJson.length());
 * 
 * WebClient client = WebClient.create(vertx); client.put(port, "localhost",
 * "/v1/idgenerator/uin").putHeader("content-type", "application/json")
 * .putHeader("content-length", length).sendJson(requestWrp, response -> { if
 * (response.succeeded()) { HttpResponse<Buffer> httpResponse =
 * response.result(); LOGGER.info(httpResponse.bodyAsString());
 * context.assertEquals(httpResponse.statusCode(), 200); List<ServiceError>
 * validationErrorsList = ExceptionUtils
 * .getServiceErrorList(httpResponse.bodyAsString());
 * context.assertTrue(validationErrorsList.size() > 0); boolean errorFound =
 * false; for (ServiceError sr : validationErrorsList) { if
 * (sr.getErrorCode().equals(UinGeneratorErrorCode.UIN_STATUS_NOT_FOUND.
 * getErrorCode())) { errorFound = true; break; } }
 * context.assertTrue(errorFound); client.close(); async.complete(); } else {
 * LOGGER.error(response.cause().getMessage()); } }); }
 * 
 * 
 * }
 */