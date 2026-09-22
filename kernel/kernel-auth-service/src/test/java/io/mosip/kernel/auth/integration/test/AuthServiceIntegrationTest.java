package io.mosip.kernel.auth.integration.test;

import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.springframework.test.web.servlet.MockMvc;

/**
 * Placeholder LDAP/MockMvc integration test for {@code kernel-auth-service}.
 * Spring Boot, Mockito, and {@code @Before} annotations are commented out so
 * this class currently does not run.
 */
//@SpringBootTest(classes = AuthTestBootApplication.class)
//@RunWith(SpringRunner.class)
//@AutoConfigureMockMvc
public class AuthServiceIntegrationTest {

	/** MockMvc for HTTP calls (injection commented out). */
	// @Autowired
	MockMvc mockMvc;

	/** LDAP context collaborator (Mockito bean commented out). */
	// @MockitoBean
	LdapContext ldapContext;

	/** LDAP search results (Mockito bean commented out). */
	// @MockitoBean
	NamingEnumeration<SearchResult> searchresult;

	/** Placeholder setup; currently a no-op. */
	// @Before
	public void setUp() {

	}

}
