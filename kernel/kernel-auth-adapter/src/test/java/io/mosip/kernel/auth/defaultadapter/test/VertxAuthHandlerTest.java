package io.mosip.kernel.auth.defaultadapter.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.auth.defaultadapter.config.RestTemplateInterceptor;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.handler.AuthHandler;
import io.mosip.kernel.auth.defaultadapter.handler.VertxAuthHandler;
import io.mosip.kernel.auth.defaultadapter.helper.VertxTokenValidationHelper;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import io.vertx.ext.web.RoutingContext;

/**
 * Tests {@link VertxAuthHandler} token validation against a Vert.x
 * {@link RoutingContext} and lookup of the user stored on that context.
 * <p>
 * Uses {@link MockitoBean} (Boot 4) for {@link VertxTokenValidationHelper}.
 */
@SpringBootTest(classes = { AuthTestBootApplication.class })
@RunWith(SpringRunner.class)
public class VertxAuthHandlerTest extends AuthHandler {

	
	/** Vert.x auth handler from the adapter test context. */
	@Autowired
	private VertxAuthHandler vertxAuthHandler;
	
	/** Rest interceptor from the adapter test context. */
	@Autowired
	private RestTemplateInterceptor restInterceptor;
	
	/** Vert.x token validation collaborator replaced with a Mockito bean. */
	@MockitoBean
	private VertxTokenValidationHelper validationHelper;
	
	/** Whether SSL verification is bypassed in the adapter (test property). */
	@Value("${mosip.kernel.auth.adapter.ssl-bypass:true}")
	private boolean sslBypass;
	

	
	
	/**
	 * Asserts private {@code validateToken} returns the token from a validated
	 * {@link MosipUserDto} placed on the routing context.
	 *
	 * @throws Exception if reflection or invocation fails
	 */
	@Test
	public void validateTokenTest() throws Exception {
		Method method=   vertxAuthHandler.getClass().getDeclaredMethod("validateToken", RoutingContext.class,String[].class);
		method.setAccessible(true);
		RoutingContext routingContext = Mockito.mock(RoutingContext.class);
		String[] roles= {"REGISTRATION_PROCESSOR"};
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setRole("PROCESSOR");
		mosipUserDto.setUserId("110005");
		mosipUserDto.setToken("mock-token");
		
		when(validationHelper.getTokenValidatedVertxUserResponse(Mockito.any(RestTemplate.class),Mockito.eq(routingContext),Mockito.any(String[].class))).thenReturn(mosipUserDto);
		when(routingContext.put(Mockito.eq(AuthAdapterConstant.ROUTING_CONTEXT_USER), Mockito.eq(mosipUserDto))).thenReturn(null);
		String token=  (String) method.invoke(vertxAuthHandler,routingContext,roles);
		assertThat(token,is(mosipUserDto.getToken()));
	}
	
	/**
	 * Asserts {@code getContextUser} returns the user id stored on the routing
	 * context.
	 *
	 * @throws Exception if the handler call fails
	 */
	@Test
	public void getContextUserTest() throws Exception {
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setRole("PROCESSOR");
		mosipUserDto.setUserId("110005");
		mosipUserDto.setToken("mock-token");
		RoutingContext routingContext = Mockito.mock(RoutingContext.class);
		when(routingContext.get(AuthAdapterConstant.ROUTING_CONTEXT_USER)).thenReturn(mosipUserDto);
		assertThat(vertxAuthHandler.getContextUser(routingContext),is(mosipUserDto.getUserId()));
		
	}
	
	
	
	
	
}
