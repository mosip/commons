/*
 * package io.mosip.kernel.pridgenerator.test.router;
 * 
 * import java.io.IOException; import java.net.ServerSocket; import
 * java.util.stream.Stream;
 * 
 * import jakarta.annotation.PostConstruct;
 * 
 * import org.junit.AfterClass; import org.junit.BeforeClass; import
 * org.junit.Test; import org.junit.runner.RunWith; import
 * org.springframework.context.annotation.AnnotationConfigApplicationContext;
 * import org.springframework.context.support.AbstractApplicationContext;
 * 
 * import com.fasterxml.jackson.databind.ObjectMapper; import
 * com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
 * 
 * import io.mosip.kernel.core.http.ResponseWrapper; import
 * io.mosip.kernel.pridgenerator.constant.EventType; import
 * io.mosip.kernel.pridgenerator.dto.PridFetchResponseDto; import
 * io.mosip.kernel.pridgenerator.test.config.HibernateDaoConfig; import
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
 * @RunWith(VertxUnitRunner.class) public class IntegrationTest {
 * 
 * private static Vertx vertx; private static int port; private static
 * AbstractApplicationContext context; private static ObjectMapper objectMapper
 * = new ObjectMapper(); private static Logger LOGGER;
 * 
 * 
 * @BeforeClass public static void setup(TestContext testContext) throws
 * IOException { System.setProperty("vertx.logger-delegate-factory-class-name",
 * SLF4JLogDelegateFactory.class.getName()); objectMapper.registerModule(new
 * JavaTimeModule()); LOGGER = LoggerFactory.getLogger(IntegrationTest.class);
 * ServerSocket socket = new ServerSocket(0); vertx = Vertx.vertx(); port =
 * socket.getLocalPort(); System.setProperty("server.port",
 * String.valueOf(port)); socket.close(); VertxOptions options = new
 * VertxOptions(); context = new
 * AnnotationConfigApplicationContext(HibernateDaoConfig.class);
 * DeploymentOptions workerOptions = new DeploymentOptions().setWorker(true);
 * vertx = Vertx.vertx(options); Verticle[] workerVerticles = { new
 * PridPoolCheckerVerticle(context), new PridPopulatorVerticle(context) };
 * Stream.of(workerVerticles).forEach(verticle -> deploy(verticle,
 * workerOptions, vertx)); vertx.setTimer(1000, handler -> initPool()); }
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
 * @Test public void getPRIDSuccessTest(TestContext context) {
 * LOGGER.info("getPRIDSuccessTest execution..."); Async async =
 * context.async(); WebClient client = WebClient.create(vertx); client.get(port,
 * "localhost", "/v1/pridgenerator/prid").send(ar -> { if (ar.succeeded()) {
 * HttpResponse<Buffer> httpResponse = ar.result();
 * LOGGER.info(httpResponse.bodyAsString()); context.assertEquals(200,
 * httpResponse.statusCode()); try { ResponseWrapper<?> uinResp =
 * objectMapper.readValue(httpResponse.bodyAsString(), ResponseWrapper.class);
 * PridFetchResponseDto dto = objectMapper.convertValue(uinResp.getResponse(),
 * PridFetchResponseDto.class); context.assertNotNull(dto.getPrid()); } catch
 * (IOException e) { e.printStackTrace(); } client.close(); async.complete(); }
 * else { LOGGER.error(ar.cause().getMessage()); } }); } }
 */