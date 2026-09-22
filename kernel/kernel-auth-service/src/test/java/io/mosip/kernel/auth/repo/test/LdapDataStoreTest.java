/**
 * Commented-out LDAP datastore tests for {@code LdapDataStore} authenticate,
 * user CRUD, roles, and password flows. The entire class is disabled as a
 * block comment and is not executed.
 */
/*
 * package io.mosip.kernel.auth.repo.test;
 * 
 * 
 * 
 * 
 * 
 * import static org.hamcrest.CoreMatchers.is; import static
 * org.junit.Assert.assertThat; import static org.mockito.Mockito.doNothing;
 * import static org.mockito.Mockito.doThrow; import static
 * org.mockito.Mockito.when;
 * 
 * import java.time.LocalDate; import java.util.ArrayList; import
 * java.util.List; import java.util.Map;
 * 
 * import javax.naming.NameAlreadyBoundException; import
 * javax.naming.NameNotFoundException; import javax.naming.NamingEnumeration;
 * import javax.naming.NamingException; import
 * javax.naming.directory.Attributes; import javax.naming.directory.DirContext;
 * import javax.naming.directory.ModificationItem; import
 * javax.naming.directory.SearchControls; import
 * javax.naming.directory.SearchResult; import javax.naming.ldap.LdapContext;
 * 
 * import org.apache.directory.api.ldap.model.cursor.EntryCursor; import
 * org.apache.directory.api.ldap.model.entry.Attribute; import
 * org.apache.directory.api.ldap.model.entry.Entry; import
 * org.apache.directory.api.ldap.model.entry.Modification; import
 * org.apache.directory.api.ldap.model.entry.Value; import
 * org.apache.directory.api.ldap.model.exception.LdapException; import
 * org.apache.directory.api.ldap.model.exception.LdapInvalidDnException; import
 * org.apache.directory.api.ldap.model.message.SearchScope; import
 * org.apache.directory.api.ldap.model.name.Dn; import
 * org.apache.directory.ldap.client.api.LdapConnection; import org.junit.Before;
 * import org.junit.Test; import org.junit.runner.RunWith; import
 * org.mockito.Mockito; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
 * import org.springframework.boot.test.context.SpringBootTest; import
 * org.springframework.test.context.junit4.SpringRunner;
 * 
 * import io.mosip.kernel.auth.defaultimpl.constant.LdapConstants; import
 * io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException; import
 * io.mosip.kernel.auth.defaultimpl.repository.impl.LdapDataStore; import
 * io.mosip.kernel.auth.test.AuthTestBootApplication; import
 * io.mosip.kernel.core.authmanager.model.AuthZResponseDto; import
 * io.mosip.kernel.core.authmanager.model.ClientSecret; import
 * io.mosip.kernel.core.authmanager.model.LoginUser; import
 * io.mosip.kernel.core.authmanager.model.MosipUserDto; import
 * io.mosip.kernel.core.authmanager.model.MosipUserListDto; import
 * io.mosip.kernel.core.authmanager.model.MosipUserSaltListDto; import
 * io.mosip.kernel.core.authmanager.model.OtpUser; import
 * io.mosip.kernel.core.authmanager.model.PasswordDto; import
 * io.mosip.kernel.core.authmanager.model.RIdDto; import
 * io.mosip.kernel.core.authmanager.model.RolesListDto; import
 * io.mosip.kernel.core.authmanager.model.UserDetailsDto; import
 * io.mosip.kernel.core.authmanager.model.UserDetailsResponseDto; import
 * io.mosip.kernel.core.authmanager.model.UserNameDto; import
 * io.mosip.kernel.core.authmanager.model.UserOtp; import
 * io.mosip.kernel.core.authmanager.model.UserRegistrationRequestDto; import
 * io.mosip.kernel.core.authmanager.model.ValidationResponseDto;
 * 
 * @SpringBootTest(classes = { AuthTestBootApplication.class })
 * 
 * @RunWith(SpringRunner.class)
 * 
 * @AutoConfigureMockMvc public class LdapDataStoreTest {
 * 
 * 
 * @Autowired private LdapDataStore ldapDataStore;
 * 
 * private LdapDataStore ldapDataStoreSpy;
 * 
 * @Before public void init() { ldapDataStoreSpy=Mockito.spy(ldapDataStore); }
 * 
 * @Test public void authenticateUserTest() throws Exception { LoginUser
 * loginUser = new LoginUser(); loginUser.setAppId("ida");
 * loginUser.setUserName("mock-user"); loginUser.setPassword("mock-pass");
 * LdapConnection connection = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * doNothing().when(connection).bind(Mockito.any(Dn.class),Mockito.anyString());
 * when(connection.isAuthenticated()).thenReturn(true);
 * 
 * // lookup mock Dn userDN=createUserDn(loginUser.getUserName()); Dn searchBase
 * = new Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(userLookup); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * MosipUserDto dto=ldapDataStoreSpy.authenticateUser(loginUser);
 * assertThat(dto.getName(),is(loginUser.getUserName())); }
 * 
 * 
 * 
 * @Test public void authenticateWithOtpTest() throws Exception { List<String>
 * channel = new ArrayList<>(); channel.add("phone"); OtpUser otpUser = new
 * OtpUser(); otpUser.setUserId("mock-user"); otpUser.setAppId("ida");
 * otpUser.setOtpChannel(channel); otpUser.setUseridtype("UIN");
 * otpUser.setContext("uin"); LdapConnection connection =
 * Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * // lookup mock Dn userDN=createUserDn(otpUser.getUserId()); Dn searchBase =
 * new Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(userLookup); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * when(connection.exists(Mockito.eq(userDN))).thenReturn(true); MosipUserDto
 * dto=ldapDataStoreSpy.authenticateWithOtp(otpUser);
 * assertThat(dto.getName(),is(otpUser.getUserId())); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * authenticateWithOtpAuthManagerExceptionTest() throws Exception { List<String>
 * channel = new ArrayList<>(); channel.add("phone"); OtpUser otpUser = new
 * OtpUser(); otpUser.setUserId("mock-user"); otpUser.setAppId("ida");
 * otpUser.setOtpChannel(channel); otpUser.setUseridtype("UIN");
 * otpUser.setContext("uin"); LdapConnection connection =
 * Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * // lookup mock Dn userDN=createUserDn(otpUser.getUserId()); Dn searchBase =
 * new Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(userLookup); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * when(connection.exists(Mockito.eq(userDN))).thenReturn(false); MosipUserDto
 * dto=ldapDataStoreSpy.authenticateWithOtp(otpUser);
 * assertThat(dto.getName(),is(otpUser.getUserId())); }
 * 
 * @Test public void authenticateUserWithOtpTest() throws Exception { UserOtp
 * uo= new UserOtp(); uo.setAppId("ida"); uo.setUserId("mock-user");
 * uo.setOtp("928374"); LdapConnection connection =
 * Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * // lookup mock Dn userDN=createUserDn(uo.getUserId()); Dn searchBase = new
 * Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(userLookup); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * MosipUserDto dto=ldapDataStoreSpy.authenticateUserWithOtp(uo);
 * assertThat(dto.getName(),is(uo.getUserId())); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * authenticateUserWithOtpAuthManagerExceptionTest() throws Exception { UserOtp
 * uo= new UserOtp(); uo.setAppId("ida"); uo.setUserId("mock-user");
 * uo.setOtp("928374"); LdapConnection connection =
 * Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * // lookup mock Dn userDN=createUserDn(uo.getUserId()); Dn searchBase = new
 * Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenThrow(new
 * LdapException("not found")); Attribute uidAttribute =
 * Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * MosipUserDto dto=ldapDataStoreSpy.authenticateUserWithOtp(uo);
 * assertThat(dto.getName(),is(uo.getUserId())); }
 * 
 * 
 * @Test public void authenticateWithSecretKeyTest() throws Exception {
 * ClientSecret clientSecret = new ClientSecret(); clientSecret.setAppId("ida");
 * clientSecret.setClientId("ida-client"); clientSecret.setSecretKey("abc");
 * LdapConnection connection = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * doNothing().when(connection).bind(Mockito.any(Dn.class),Mockito.anyString());
 * when(connection.isAuthenticated()).thenReturn(true);
 * 
 * // lookup mock Dn userDN=createUserDn(clientSecret.getClientId()); Dn
 * searchBase = new Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(userLookup); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("ida-client"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * MosipUserDto dto=ldapDataStoreSpy.authenticateWithSecretKey(clientSecret);
 * assertThat(dto.getName(),is(clientSecret.getClientId())); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * authenticateWithSecretKeyLdapExceptionTest() throws Exception { ClientSecret
 * clientSecret = new ClientSecret(); clientSecret.setAppId("ida");
 * clientSecret.setClientId("ida-client"); clientSecret.setSecretKey("abc");
 * LdapConnection connection = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * (); doThrow(new
 * LdapException("connection rejected")).when(connection).bind(Mockito.any(Dn.
 * class),Mockito.anyString());
 * when(connection.isAuthenticated()).thenReturn(true);
 * 
 * // lookup mock Dn userDN=createUserDn(clientSecret.getClientId()); Dn
 * searchBase = new Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(userLookup); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("ida-client"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * MosipUserDto dto=ldapDataStoreSpy.authenticateWithSecretKey(clientSecret);
 * assertThat(dto.getName(),is(clientSecret.getClientId())); }
 * 
 * 
 * 
 * @Test public void getAllRolesTest() throws Exception {
 * 
 * LdapConnection connection = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * doNothing().when(connection).bind(Mockito.any(Dn.class),Mockito.anyString());
 * when(connection.isAuthenticated()).thenReturn(true);
 * 
 * Dn searchBase = new Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(objectClass=organizationalRole)";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); Attribute descAttribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.get()).thenReturn(new Value("PROCESSOR"));
 * when(entry.get("description")).thenReturn(descAttribute);
 * when(descAttribute.get()).thenReturn(new Value("role for processor"));
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * 
 * RolesListDto dto=ldapDataStoreSpy.getAllRoles("ida");
 * assertThat(dto.getRoles().get(0).getRoleName(),is("PROCESSOR")); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * getAllRolesLdapExceptionTest() throws Exception {
 * 
 * LdapConnection connection = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * doNothing().when(connection).bind(Mockito.any(Dn.class),Mockito.anyString());
 * when(connection.isAuthenticated()).thenReturn(true);
 * 
 * Dn searchBase = new Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(objectClass=organizationalRole)";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); Attribute descAttribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.get()).thenReturn(new Value("PROCESSOR"));
 * when(entry.get("description")).thenReturn(descAttribute);
 * when(descAttribute.get()).thenReturn(new Value("role for processor"));
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenThrow(new
 * LdapException("connection rejected")); doNothing().when(entryCursor).close();
 * 
 * 
 * 
 * RolesListDto dto=ldapDataStoreSpy.getAllRoles("ida");
 * assertThat(dto.getRoles().get(0).getRoleName(),is("PROCESSOR")); }
 * 
 * 
 * @Test public void getListOfUsersDetailsTest() throws Exception { List<String>
 * users = new ArrayList<>(); users.add("mock-user"); LdapConnection connection
 * = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * // lookup mock Dn userDN=createUserDn("mock-user"); Dn searchBase = new
 * Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(userLookup); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * MosipUserListDto dto=ldapDataStoreSpy.getListOfUsersDetails(users,"ida");
 * assertThat(dto.getMosipUserDtoList().get(0).getName(),is(users.get(0))); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * getListOfUsersDetailsLdapExceptionTest() throws Exception { List<String>
 * users = new ArrayList<>(); users.add("mock-user"); LdapConnection connection
 * = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * // lookup mock Dn userDN=createUserDn("mock-user"); Dn searchBase = new
 * Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenThrow(new
 * LdapException("connection rejected")); doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(userLookup); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * MosipUserListDto dto=ldapDataStoreSpy.getListOfUsersDetails(users,"ida");
 * assertThat(dto.getMosipUserDtoList().get(0).getName(),is(users.get(0))); }
 * 
 * @Test public void getAllUserDetailsWithSaltTest() throws Exception {
 * List<String> users = new ArrayList<>(); users.add("mock-user");
 * LdapConnection connection = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * Dn searchBase = new Dn("ou=people,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute uidAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("uid")).thenReturn(uidAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("mock-user"));
 * when(entry.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userPasswordAttribute.get()).thenReturn(new
 * Value("{SSHA256}cJR+rbZqzDk3OQwLiN3PaPA+4YBynYpkSaisN+38E1BpbNpdGL4Erw=="));
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(connection).close();
 * 
 * 
 * 
 * MosipUserSaltListDto
 * dto=ldapDataStoreSpy.getAllUserDetailsWithSalt(users,"ida");
 * assertThat(dto.getMosipUserSaltList().get(0).getUserId(),is(users.get(0))); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * getAllUserDetailsWithSaltLdapExceptionTest() throws Exception { List<String>
 * users = new ArrayList<>(); users.add("mock-user"); LdapConnection connection
 * = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * Dn searchBase = new Dn("ou=people,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute uidAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("uid")).thenReturn(uidAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("mock-user"));
 * when(entry.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userPasswordAttribute.get()).thenReturn(new
 * Value("{SSHA256}cJR+rbZqzDk3OQwLiN3PaPA+4YBynYpkSaisN+38E1BpbNpdGL4Erw=="));
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenThrow(new
 * LdapException("connection rejected")); doNothing().when(connection).close();
 * 
 * 
 * 
 * MosipUserSaltListDto
 * dto=ldapDataStoreSpy.getAllUserDetailsWithSalt(users,"ida");
 * assertThat(dto.getMosipUserSaltList().get(0).getUserId(),is(users.get(0))); }
 * 
 * 
 * @Test public void getRidFromUserIdTest() throws Exception {
 * 
 * LdapConnection connection = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * // lookup mock Dn userDN=createUserDn("mock-user"); Dn searchBase = new
 * Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(userLookup); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * RIdDto dto=ldapDataStoreSpy.getRidFromUserId("mock-user","ida");
 * assertThat(dto.getRId(),is("829192012")); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * getRidFromUserIdLdapExceptionTest() throws Exception {
 * 
 * LdapConnection connection = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * // lookup mock Dn userDN=createUserDn("mock-user"); Dn searchBase = new
 * Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(null); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * RIdDto dto=ldapDataStoreSpy.getRidFromUserId("mock-user","ida");
 * assertThat(dto.getRId(),is("829192012")); }
 * 
 * @Test public void unBlockAccountTest() throws Exception {
 * 
 * LdapContext ldapContext = Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext(); String
 * userId="mock-user";
 * doNothing().when(ldapContext).modifyAttributes(Mockito.eq("uid=" + userId +
 * ",ou=people,c=mycountry"), Mockito.any());
 * 
 * AuthZResponseDto dto=ldapDataStoreSpy.unBlockAccount(userId);
 * assertThat(dto.getStatus(),is("Success")); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * unBlockAccountNamingExceptionTest() throws Exception {
 * 
 * LdapContext ldapContext = Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext(); String
 * userId="mock-user"; doThrow(new
 * NamingException("can not modify attributes")).when(ldapContext).
 * modifyAttributes(Mockito.eq("uid=" + userId + ",ou=people,c=mycountry"),
 * Mockito.any());
 * 
 * AuthZResponseDto dto=ldapDataStoreSpy.unBlockAccount(userId);
 * assertThat(dto.getStatus(),is("Success")); }
 * 
 * 
 * @Test public void changePasswordTest() throws Exception { PasswordDto
 * passwordDto = new PasswordDto(); passwordDto.setUserId("mock-user");
 * passwordDto.setNewPassword("Mosip#4123");
 * passwordDto.setOldPassword("Mosip#4331"); passwordDto.setHashAlgo("SHA256");
 * LdapContext ldapContext = Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getUserDetailSearchResult Dn searchBase = new Dn("uid=" +
 * passwordDto.getUserId() + ",ou=people,c=mycountry"); SearchControls
 * searchControls = new SearchControls();
 * searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE); Attributes
 * attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()), Mockito.eq(
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person))"
 * ), Mockito.any())).thenReturn(getChangePassNumingEnum(attributes));
 * 
 * 
 * // method javax.naming.directory.Attribute uidAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * javax.naming.directory.Attribute mailAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("uid")).thenReturn(uidAttribute);
 * when(attributes.get(LdapConstants.MAIL)).thenReturn(mailAttribute);
 * when(uidAttribute.get()).thenReturn(passwordDto.getUserId());
 * when(mailAttribute.get()).thenReturn("mock@moisp.io");
 * 
 * //getPassword javax.naming.directory.Attribute passwordAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("userPassword")).thenReturn(passwordAttribute);
 * when(passwordAttribute.get()).thenReturn(passwordDto.getOldPassword().
 * getBytes());
 * 
 * //method doNothing().when(ldapContext).modifyAttributes(Mockito.eq("uid=" +
 * passwordDto.getUserId() + ",ou=people,c=mycountry"), Mockito.any());
 * AuthZResponseDto dto=ldapDataStoreSpy.changePassword(passwordDto);
 * assertThat(dto.getStatus(),is("Success")); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * changePasswordLdapExceptionTest() throws Exception { PasswordDto passwordDto
 * = new PasswordDto(); passwordDto.setUserId("mock-user");
 * passwordDto.setNewPassword("Mosip#4331");
 * passwordDto.setOldPassword("Mosip#4331"); passwordDto.setHashAlgo("SHA256");
 * LdapContext ldapContext = Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getUserDetailSearchResult Dn searchBase = new Dn("uid=" +
 * passwordDto.getUserId() + ",ou=people,c=mycountry"); SearchControls
 * searchControls = new SearchControls();
 * searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE); Attributes
 * attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()), Mockito.eq(
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person))"
 * ), Mockito.any())).thenReturn(getChangePassNumingEnum(attributes));
 * 
 * 
 * // method javax.naming.directory.Attribute uidAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * javax.naming.directory.Attribute mailAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("uid")).thenReturn(uidAttribute);
 * when(attributes.get(LdapConstants.MAIL)).thenReturn(mailAttribute);
 * when(uidAttribute.get()).thenReturn(passwordDto.getUserId());
 * when(mailAttribute.get()).thenReturn("mock@moisp.io");
 * 
 * //getPassword javax.naming.directory.Attribute passwordAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("userPassword")).thenReturn(passwordAttribute);
 * when(passwordAttribute.get()).thenReturn(passwordDto.getOldPassword().
 * getBytes());
 * 
 * //method doNothing().when(ldapContext).modifyAttributes(Mockito.eq("uid=" +
 * passwordDto.getUserId() + ",ou=people,c=mycountry"), Mockito.any());
 * AuthZResponseDto dto=ldapDataStoreSpy.changePassword(passwordDto);
 * assertThat(dto.getStatus(),is("Success")); }
 * 
 * 
 * private NamingEnumeration<SearchResult> getChangePassNumingEnum(Attributes
 * attributes) { // TODO Auto-generated method stub return new
 * NamingEnumeration<SearchResult>() { int index=0;
 * 
 * SearchResult searchResult = new SearchResult("1", null, attributes);
 * 
 * @Override public SearchResult nextElement() { // TODO Auto-generated method
 * stub return null; }
 * 
 * @Override public boolean hasMoreElements() { // TODO Auto-generated method
 * stub return false; }
 * 
 * @Override public SearchResult next() throws NamingException { index++; return
 * searchResult; }
 * 
 * @Override public boolean hasMore() throws NamingException { boolean hasMore
 * =index+1<2; if(!hasMore) index=0; return hasMore; }
 * 
 * @Override public void close() throws NamingException { index=0;
 * 
 * } }; }
 * 
 * private NamingEnumeration<SearchResult> getEmptyNumingEnum(Attributes
 * attributes) { // TODO Auto-generated method stub return new
 * NamingEnumeration<SearchResult>() {
 * 
 * @Override public SearchResult nextElement() { // TODO Auto-generated method
 * stub return null; }
 * 
 * @Override public boolean hasMoreElements() { // TODO Auto-generated method
 * stub return false; }
 * 
 * @Override public SearchResult next() throws NamingException {
 * 
 * return null; }
 * 
 * @Override public boolean hasMore() throws NamingException { return false; }
 * 
 * @Override public void close() throws NamingException {
 * 
 * } }; }
 * 
 * 
 * @Test public void resetPasswordTest() throws Exception { PasswordDto
 * passwordDto = new PasswordDto(); passwordDto.setUserId("mock-user");
 * passwordDto.setNewPassword("Mosip#4123");
 * passwordDto.setOldPassword("Mosip#4331"); passwordDto.setHashAlgo("SHA256");
 * LdapContext ldapContext = Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getUserDetailSearchResult Dn searchBase = new Dn("uid=" +
 * passwordDto.getUserId() + ",ou=people,c=mycountry"); SearchControls
 * searchControls = new SearchControls();
 * searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE); Attributes
 * attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()), Mockito.eq(
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person))"
 * ), Mockito.any())).thenReturn(getChangePassNumingEnum(attributes));
 * 
 * 
 * // method javax.naming.directory.Attribute uidAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * javax.naming.directory.Attribute mailAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("uid")).thenReturn(uidAttribute);
 * when(attributes.get(LdapConstants.MAIL)).thenReturn(mailAttribute);
 * when(uidAttribute.get()).thenReturn(passwordDto.getUserId());
 * when(mailAttribute.get()).thenReturn("mock@moisp.io");
 * 
 * 
 * //method doNothing().when(ldapContext).modifyAttributes(Mockito.eq("uid=" +
 * passwordDto.getUserId() + ",ou=people,c=mycountry"), Mockito.any());
 * AuthZResponseDto dto=ldapDataStoreSpy.resetPassword(passwordDto);
 * assertThat(dto.getStatus(),is("Success")); }
 * 
 * 
 * @Test(expected = AuthManagerException.class) public void
 * resetPasswordNamingExceptionTest() throws Exception { PasswordDto passwordDto
 * = new PasswordDto(); passwordDto.setUserId("mock-user");
 * passwordDto.setNewPassword("Mosip#4123");
 * passwordDto.setOldPassword("Mosip#4331"); passwordDto.setHashAlgo("SHA256");
 * LdapContext ldapContext = Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getUserDetailSearchResult Dn searchBase = new Dn("uid=" +
 * passwordDto.getUserId() + ",ou=people,c=mycountry"); SearchControls
 * searchControls = new SearchControls();
 * searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE); Attributes
 * attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()), Mockito.eq(
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person))"
 * ), Mockito.any())).thenThrow(new NamingException("connection rejected"));
 * 
 * 
 * // method javax.naming.directory.Attribute uidAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * javax.naming.directory.Attribute mailAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("uid")).thenReturn(uidAttribute);
 * when(attributes.get(LdapConstants.MAIL)).thenReturn(mailAttribute);
 * when(uidAttribute.get()).thenReturn(passwordDto.getUserId());
 * when(mailAttribute.get()).thenReturn("mock@moisp.io");
 * 
 * 
 * //method doNothing().when(ldapContext).modifyAttributes(Mockito.eq("uid=" +
 * passwordDto.getUserId() + ",ou=people,c=mycountry"), Mockito.any());
 * AuthZResponseDto dto=ldapDataStoreSpy.resetPassword(passwordDto);
 * assertThat(dto.getStatus(),is("Success")); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * resetPasswordAuthManagerExceptionTest() throws Exception { PasswordDto
 * passwordDto = new PasswordDto(); passwordDto.setUserId("mock-user");
 * passwordDto.setNewPassword("mock-user#4123");
 * passwordDto.setOldPassword("Mosip#4331"); passwordDto.setHashAlgo("SHA256");
 * LdapContext ldapContext = Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getUserDetailSearchResult Dn searchBase = new Dn("uid=" +
 * passwordDto.getUserId() + ",ou=people,c=mycountry"); SearchControls
 * searchControls = new SearchControls();
 * searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE); Attributes
 * attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()), Mockito.eq(
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person))"
 * ), Mockito.any())).thenThrow(new NamingException("connection rejected"));
 * 
 * 
 * // method javax.naming.directory.Attribute uidAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * javax.naming.directory.Attribute mailAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("uid")).thenReturn(uidAttribute);
 * when(attributes.get(LdapConstants.MAIL)).thenReturn(mailAttribute);
 * when(uidAttribute.get()).thenReturn(passwordDto.getUserId());
 * when(mailAttribute.get()).thenReturn("mock@moisp.io");
 * 
 * 
 * //method doNothing().when(ldapContext).modifyAttributes(Mockito.eq("uid=" +
 * passwordDto.getUserId() + ",ou=people,c=mycountry"), Mockito.any());
 * AuthZResponseDto dto=ldapDataStoreSpy.resetPassword(passwordDto);
 * assertThat(dto.getStatus(),is("Success")); }
 * 
 * 
 * @Test public void getUserNameBasedOnMobileNumberAuthManagerExceptionTest()
 * throws Exception { String mobileNo="+919513582611"; LdapContext ldapContext =
 * Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getUserDetail Dn searchBase = new Dn("ou=people,c=mycountry"); String
 * searchFilter
 * ="(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person)(mobile={0}))";
 * Attributes attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()),
 * Mockito.eq(searchFilter), Mockito.eq(new
 * String[]{mobileNo}),Mockito.any())).thenReturn(getChangePassNumingEnum(
 * attributes));
 * 
 * 
 * // method javax.naming.directory.Attribute uidAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("uid")).thenReturn(uidAttribute);
 * when(uidAttribute.get()).thenReturn("mockuser");
 * 
 * 
 * 
 * UserNameDto dto=ldapDataStoreSpy.getUserNameBasedOnMobileNumber(mobileNo);
 * assertThat(dto.getUserName(),is("mockuser")); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * getUserNameBasedOnMobileNumberTest() throws Exception { String
 * mobileNo="+919513582611"; LdapContext ldapContext =
 * Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getUserDetail Dn searchBase = new Dn("ou=people,c=mycountry"); String
 * searchFilter
 * ="(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person)(mobile={0}))";
 * Attributes attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()),
 * Mockito.eq(searchFilter), Mockito.eq(new
 * String[]{mobileNo}),Mockito.any())).thenReturn(getEmptyNumingEnum(attributes)
 * );
 * 
 * 
 * // method javax.naming.directory.Attribute uidAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("uid")).thenReturn(uidAttribute);
 * when(uidAttribute.get()).thenReturn("mockuser");
 * 
 * 
 * 
 * UserNameDto dto=ldapDataStoreSpy.getUserNameBasedOnMobileNumber(mobileNo);
 * assertThat(dto.getUserName(),is("mockuser")); }
 * 
 * @Test public void registerUserTest() throws Exception {
 * UserRegistrationRequestDto userCreationRequestDto = new
 * UserRegistrationRequestDto(); userCreationRequestDto.setAppId("ida");
 * userCreationRequestDto.setUserName("110005");
 * userCreationRequestDto.setUserPassword("abc");
 * userCreationRequestDto.setRole("PROCESSOR");
 * userCreationRequestDto.setGender("MALE");
 * userCreationRequestDto.setContactNo("9281929201");
 * userCreationRequestDto.setDateOfBirth(LocalDate.now());
 * userCreationRequestDto.setEmailID("mock@mosip.io");
 * userCreationRequestDto.setFirstName("mock");
 * userCreationRequestDto.setLastName("user"); DirContext context =
 * Mockito.mock(DirContext.class);
 * Mockito.doReturn(context).when(ldapDataStoreSpy).getDirContext();
 * MosipUserDto dto=ldapDataStoreSpy.registerUser(userCreationRequestDto);
 * assertThat(dto.getUserId(),is(userCreationRequestDto.getUserName())); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * registerUserNameAlreadyBoundExceptionTest() throws Exception {
 * UserRegistrationRequestDto userCreationRequestDto = new
 * UserRegistrationRequestDto(); userCreationRequestDto.setAppId("ida");
 * userCreationRequestDto.setUserName("110005");
 * userCreationRequestDto.setUserPassword("abc");
 * userCreationRequestDto.setRole("PROCESSOR");
 * userCreationRequestDto.setGender("MALE");
 * userCreationRequestDto.setContactNo("9281929201");
 * userCreationRequestDto.setDateOfBirth(LocalDate.now());
 * userCreationRequestDto.setEmailID("mock@mosip.io");
 * userCreationRequestDto.setFirstName("mock");
 * userCreationRequestDto.setLastName("user"); DirContext context =
 * Mockito.mock(DirContext.class);
 * Mockito.doThrow(NameAlreadyBoundException.class).when(ldapDataStoreSpy).
 * getDirContext(); MosipUserDto
 * dto=ldapDataStoreSpy.registerUser(userCreationRequestDto);
 * assertThat(dto.getUserId(),is(userCreationRequestDto.getUserName())); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * registerUserNamingExceptionTest() throws Exception {
 * UserRegistrationRequestDto userCreationRequestDto = new
 * UserRegistrationRequestDto(); userCreationRequestDto.setAppId("ida");
 * userCreationRequestDto.setUserName("110005");
 * userCreationRequestDto.setUserPassword("abc");
 * userCreationRequestDto.setRole("PROCESSOR");
 * userCreationRequestDto.setGender("MALE");
 * userCreationRequestDto.setContactNo("9281929201");
 * userCreationRequestDto.setDateOfBirth(LocalDate.now());
 * userCreationRequestDto.setEmailID("mock@mosip.io");
 * userCreationRequestDto.setFirstName("mock");
 * userCreationRequestDto.setLastName("user"); DirContext context =
 * Mockito.mock(DirContext.class);
 * Mockito.doThrow(NamingException.class).when(ldapDataStoreSpy).getDirContext()
 * ; MosipUserDto dto=ldapDataStoreSpy.registerUser(userCreationRequestDto);
 * assertThat(dto.getUserId(),is(userCreationRequestDto.getUserName())); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * registerUserNameNotFoundExceptionTest() throws Exception {
 * UserRegistrationRequestDto userCreationRequestDto = new
 * UserRegistrationRequestDto(); userCreationRequestDto.setAppId("ida");
 * userCreationRequestDto.setUserName("110005");
 * userCreationRequestDto.setUserPassword("abc");
 * userCreationRequestDto.setRole("PROCESSOR");
 * userCreationRequestDto.setGender("MALE");
 * userCreationRequestDto.setContactNo("9281929201");
 * userCreationRequestDto.setDateOfBirth(LocalDate.now());
 * userCreationRequestDto.setEmailID("mock@mosip.io");
 * userCreationRequestDto.setFirstName("mock");
 * userCreationRequestDto.setLastName("user"); DirContext context =
 * Mockito.mock(DirContext.class);
 * Mockito.doReturn(context).when(ldapDataStoreSpy).getDirContext();
 * when(context.createSubcontext(Mockito.anyString(),Mockito.any(Attributes.
 * class))).thenThrow(NameNotFoundException.class); MosipUserDto
 * dto=ldapDataStoreSpy.registerUser(userCreationRequestDto);
 * assertThat(dto.getUserId(),is(userCreationRequestDto.getUserName())); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * registerUserModifyNameAlreadyBoundExceptionTest() throws Exception {
 * UserRegistrationRequestDto userCreationRequestDto = new
 * UserRegistrationRequestDto(); userCreationRequestDto.setAppId("ida");
 * userCreationRequestDto.setUserName("110005");
 * userCreationRequestDto.setUserPassword("abc");
 * userCreationRequestDto.setRole("PROCESSOR");
 * userCreationRequestDto.setGender("MALE");
 * userCreationRequestDto.setContactNo("9281929201");
 * userCreationRequestDto.setDateOfBirth(LocalDate.now());
 * userCreationRequestDto.setEmailID("mock@mosip.io");
 * userCreationRequestDto.setFirstName("mock");
 * userCreationRequestDto.setLastName("user"); DirContext context =
 * Mockito.mock(DirContext.class);
 * Mockito.doReturn(context).when(ldapDataStoreSpy).getDirContext();
 * doThrow(NameAlreadyBoundException.class).when(context).modifyAttributes(
 * Mockito.anyString(), Mockito.any(ModificationItem[].class)); MosipUserDto
 * dto=ldapDataStoreSpy.registerUser(userCreationRequestDto);
 * assertThat(dto.getUserId(),is(userCreationRequestDto.getUserName())); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * registerUserModifyNameNotFoundExceptionTest() throws Exception {
 * UserRegistrationRequestDto userCreationRequestDto = new
 * UserRegistrationRequestDto(); userCreationRequestDto.setAppId("ida");
 * userCreationRequestDto.setUserName("110005");
 * userCreationRequestDto.setUserPassword("abc");
 * userCreationRequestDto.setRole("PROCESSOR");
 * userCreationRequestDto.setGender("MALE");
 * userCreationRequestDto.setContactNo("9281929201");
 * userCreationRequestDto.setDateOfBirth(LocalDate.now());
 * userCreationRequestDto.setEmailID("mock@mosip.io");
 * userCreationRequestDto.setFirstName("mock");
 * userCreationRequestDto.setLastName("user"); DirContext context =
 * Mockito.mock(DirContext.class);
 * Mockito.doReturn(context).when(ldapDataStoreSpy).getDirContext();
 * doThrow(NameNotFoundException.class).when(context).modifyAttributes(Mockito.
 * anyString(), Mockito.any(ModificationItem[].class)); MosipUserDto
 * dto=ldapDataStoreSpy.registerUser(userCreationRequestDto);
 * assertThat(dto.getUserId(),is(userCreationRequestDto.getUserName())); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * registerUserModifyNamingExceptionTest() throws Exception {
 * UserRegistrationRequestDto userCreationRequestDto = new
 * UserRegistrationRequestDto(); userCreationRequestDto.setAppId("ida");
 * userCreationRequestDto.setUserName("110005");
 * userCreationRequestDto.setUserPassword("abc");
 * userCreationRequestDto.setRole("PROCESSOR");
 * userCreationRequestDto.setGender("MALE");
 * userCreationRequestDto.setContactNo("9281929201");
 * userCreationRequestDto.setDateOfBirth(LocalDate.now());
 * userCreationRequestDto.setEmailID("mock@mosip.io");
 * userCreationRequestDto.setFirstName("mock");
 * userCreationRequestDto.setLastName("user"); DirContext context =
 * Mockito.mock(DirContext.class);
 * Mockito.doReturn(context).when(ldapDataStoreSpy).getDirContext();
 * doThrow(NamingException.class).when(context).modifyAttributes(Mockito.
 * anyString(), Mockito.any(ModificationItem[].class)); MosipUserDto
 * dto=ldapDataStoreSpy.registerUser(userCreationRequestDto);
 * assertThat(dto.getUserId(),is(userCreationRequestDto.getUserName())); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * registerUserRollbackNamingExceptionTest() throws Exception {
 * UserRegistrationRequestDto userCreationRequestDto = new
 * UserRegistrationRequestDto(); userCreationRequestDto.setAppId("ida");
 * userCreationRequestDto.setUserName("110005");
 * userCreationRequestDto.setUserPassword("abc");
 * userCreationRequestDto.setRole("PROCESSOR");
 * userCreationRequestDto.setGender("MALE");
 * userCreationRequestDto.setContactNo("9281929201");
 * userCreationRequestDto.setDateOfBirth(LocalDate.now());
 * userCreationRequestDto.setEmailID("mock@mosip.io");
 * userCreationRequestDto.setFirstName("mock");
 * userCreationRequestDto.setLastName("user"); DirContext context =
 * Mockito.mock(DirContext.class);
 * Mockito.doReturn(context).when(ldapDataStoreSpy).getDirContext();
 * doThrow(NamingException.class).when(context).modifyAttributes(Mockito.
 * anyString(), Mockito.any(ModificationItem[].class));
 * doThrow(NamingException.class).when(context).destroySubcontext(Mockito.
 * anyString()); MosipUserDto
 * dto=ldapDataStoreSpy.registerUser(userCreationRequestDto);
 * assertThat(dto.getUserId(),is(userCreationRequestDto.getUserName())); }
 * 
 * 
 * @Test public void getUserRoleByUserIdTest() throws Exception { String
 * username ="mock-user"; LdapConnection connection =
 * Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * // lookup mock Dn userDN=createUserDn(username); Dn searchBase = new
 * Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(userLookup); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * MosipUserDto dto=ldapDataStoreSpy.getUserRoleByUserId(username);
 * assertThat(dto.getName(),is(username)); }
 * 
 * 
 * @Test(expected = AuthManagerException.class) public void
 * getUserRoleByUserIdNullDataTest() throws Exception { String username
 * ="mock-user"; LdapConnection connection = Mockito.mock(LdapConnection.class);
 * Mockito.doReturn(connection).when(ldapDataStoreSpy).createAnonymousConnection
 * ();
 * 
 * 
 * // lookup mock Dn userDN=createUserDn(username); Dn searchBase = new
 * Dn("ou=roles,c=mycountry"); String searchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=" + userDN + "))";
 * 
 * // get user roles EntryCursor entryCursor = Mockito.mock(EntryCursor.class);
 * Entry entry = Mockito.mock(Entry.class); Attribute attribute =
 * Mockito.mock(Attribute.class); List<Entry> entries = new ArrayList<Entry>();
 * entries.add(entry);
 * when(entryCursor.iterator()).thenReturn(entries.iterator());
 * when(entry.get("cn")).thenReturn(attribute);
 * when(attribute.toString()).thenReturn("PROCESSOR");
 * when(connection.search(Mockito.eq(searchBase), Mockito.eq(searchFilter),
 * Mockito.eq(SearchScope.ONELEVEL))).thenReturn(entryCursor);
 * doNothing().when(entryCursor).close();
 * 
 * 
 * //rest lookup Entry userLookup = Mockito.mock(Entry.class);
 * when(connection.lookup(Mockito.eq(userDN))).thenReturn(null); Attribute
 * uidAttribute = Mockito.mock(Attribute.class); Attribute mobileAttribute =
 * Mockito.mock(Attribute.class); Attribute mailAttribute =
 * Mockito.mock(Attribute.class); Attribute userPasswordAttribute =
 * Mockito.mock(Attribute.class); Attribute cnAttribute =
 * Mockito.mock(Attribute.class); Attribute ridAttribute =
 * Mockito.mock(Attribute.class);
 * when(userLookup.get("uid")).thenReturn(uidAttribute);
 * when(userLookup.get("mobile")).thenReturn(mobileAttribute);
 * when(userLookup.get("mail")).thenReturn(mailAttribute);
 * when(userLookup.get("userPassword")).thenReturn(userPasswordAttribute);
 * when(userLookup.get("cn")).thenReturn(cnAttribute);
 * when(userLookup.get("rid")).thenReturn(ridAttribute);
 * when(uidAttribute.get()).thenReturn(new Value("819219281"));
 * when(mobileAttribute.get()).thenReturn(new Value("9281929201"));
 * when(mailAttribute.get()).thenReturn(new Value("mock@mosip.io"));
 * when(userPasswordAttribute.get()).thenReturn(new Value("mock-pass"));
 * when(cnAttribute.get()).thenReturn(new Value("mock-user"));
 * when(ridAttribute.get()).thenReturn(new Value("829192012"));
 * 
 * 
 * 
 * MosipUserDto dto=ldapDataStoreSpy.getUserRoleByUserId(username);
 * assertThat(dto.getName(),is(username)); }
 * 
 * 
 * @Test public void getUserDetailBasedonMobileNumberTest() throws Exception {
 * String mobileNo="+919513582611"; LdapContext ldapContext =
 * Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getUserDetail Dn searchBase = new Dn("ou=people,c=mycountry"); String
 * searchFilter =
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person)(mobile={0}))";
 * Attributes attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()),
 * Mockito.eq(searchFilter),Mockito.eq(new String[]{mobileNo}),
 * Mockito.any())).thenReturn(getChangePassNumingEnum(attributes));
 * 
 * //get role Attributes roleAttributes = Mockito.mock(Attributes.class); Dn
 * roleSearchBase = new Dn("ou=roles,c=mycountry"); String roleSearchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=uid=" + "mockuser" +
 * ",ou=people,c=mycountry))";
 * when(ldapContext.search(Mockito.eq(roleSearchBase.getName()),
 * Mockito.eq(roleSearchFilter),
 * Mockito.any())).thenReturn(getChangePassNumingEnum(roleAttributes));
 * javax.naming.directory.Attribute roleAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(roleAttributes.get("cn")).thenReturn(roleAttribute);
 * when(roleAttribute.get()).thenReturn("PROCESSOR");
 * 
 * // method javax.naming.directory.Attribute uidAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("uid")).thenReturn(uidAttribute);
 * when(uidAttribute.get()).thenReturn("mockuser");
 * javax.naming.directory.Attribute mailAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("mail")).thenReturn(mailAttribute);
 * when(mailAttribute.get()).thenReturn("mock@mosip.io");
 * javax.naming.directory.Attribute mobileAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("mobile")).thenReturn(mobileAttribute);
 * when(mobileAttribute.get()).thenReturn(mobileNo);
 * javax.naming.directory.Attribute nameAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("cn")).thenReturn(nameAttribute);
 * when(nameAttribute.get()).thenReturn("mock-user");
 * 
 * 
 * 
 * 
 * MosipUserDto dto=ldapDataStoreSpy.getUserDetailBasedonMobileNumber(mobileNo);
 * assertThat(dto.getUserId(),is("mockuser")); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * getUserDetailBasedonMobileNumberLdapInvalidDnExceptionTest() throws Exception
 * { String mobileNo="9513582611"; LdapContext ldapContext =
 * Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getUserDetail Dn searchBase = new Dn("ou=people,c=mycountry"); String
 * searchFilter =
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person)(mobile="
 * + mobileNo + "))";; Attributes attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()),
 * Mockito.eq(searchFilter),
 * Mockito.any())).thenReturn(getChangePassNumingEnum(attributes));
 * 
 * //get role Attributes roleAttributes = Mockito.mock(Attributes.class); Dn
 * roleSearchBase = new Dn("ou=roles,c=mycountry"); String roleSearchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=uid=" + "mockuser" +
 * ",ou=people,c=mycountry))";
 * when(ldapContext.search(Mockito.eq(roleSearchBase.getName()),
 * Mockito.eq(roleSearchFilter),
 * Mockito.any())).thenReturn(getChangePassNumingEnum(roleAttributes));
 * javax.naming.directory.Attribute roleAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(roleAttributes.get("cn")).thenReturn(roleAttribute);
 * when(roleAttribute.get()).thenReturn("PROCESSOR");
 * 
 * // method javax.naming.directory.Attribute uidAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("uid")).thenReturn(uidAttribute);
 * when(uidAttribute.get()).thenReturn("mockuser");
 * javax.naming.directory.Attribute mailAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("mail")).thenReturn(mailAttribute);
 * when(mailAttribute.get()).thenReturn("mock@mosip.io");
 * javax.naming.directory.Attribute mobileAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("mobile")).thenReturn(mobileAttribute);
 * when(mobileAttribute.get()).thenReturn(mobileNo);
 * javax.naming.directory.Attribute nameAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("cn")).thenReturn(nameAttribute);
 * when(nameAttribute.get()).thenReturn("mock-user");
 * 
 * 
 * 
 * 
 * MosipUserDto dto=ldapDataStoreSpy.getUserDetailBasedonMobileNumber(mobileNo);
 * assertThat(dto.getUserId(),is("mockuser")); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * getUserDetailBasedonMobileNumberNamingExceptionTest() throws Exception {
 * String mobileNo="+919513582611"; LdapContext ldapContext =
 * Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getUserDetail Dn searchBase = new Dn("ou=people,c=mycountry");
 * 
 * String searchFilter
 * ="(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person)(mobile={0}))";
 * Attributes attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()),
 * Mockito.eq(searchFilter),Mockito.eq(new String[]{mobileNo}),
 * Mockito.any())).thenThrow(new NamingException("connection rejected"));
 * 
 * //get role Attributes roleAttributes = Mockito.mock(Attributes.class); Dn
 * roleSearchBase = new Dn("ou=roles,c=mycountry"); String roleSearchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=uid=" + "mockuser" +
 * ",ou=people,c=mycountry))";
 * when(ldapContext.search(Mockito.eq(roleSearchBase.getName()),
 * Mockito.eq(roleSearchFilter),Mockito.eq(new
 * String[]{mobileNo}),Mockito.any())).thenReturn(getChangePassNumingEnum(
 * roleAttributes)); javax.naming.directory.Attribute roleAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(roleAttributes.get("cn")).thenReturn(roleAttribute);
 * when(roleAttribute.get()).thenReturn("PROCESSOR");
 * 
 * // method javax.naming.directory.Attribute uidAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("uid")).thenReturn(uidAttribute);
 * when(uidAttribute.get()).thenReturn("mockuser");
 * javax.naming.directory.Attribute mailAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("mail")).thenReturn(mailAttribute);
 * when(mailAttribute.get()).thenReturn("mock@mosip.io");
 * javax.naming.directory.Attribute mobileAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("mobile")).thenReturn(mobileAttribute);
 * when(mobileAttribute.get()).thenReturn(mobileNo);
 * javax.naming.directory.Attribute nameAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("cn")).thenReturn(nameAttribute);
 * when(nameAttribute.get()).thenReturn("mock-user");
 * 
 * 
 * 
 * 
 * MosipUserDto dto=ldapDataStoreSpy.getUserDetailBasedonMobileNumber(mobileNo);
 * assertThat(dto.getUserId(),is("mockuser")); }
 * 
 * 
 * @Test public void validateUserNameTest() throws Exception { String
 * userName="mockuser"; LdapContext ldapContext =
 * Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * 
 * //getUserDetailSearchResult Dn searchBase = new Dn("uid=" +userName +
 * ",ou=people,c=mycountry"); SearchControls searchControls = new
 * SearchControls();
 * searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE); Attributes
 * attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()), Mockito.eq(
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person))"
 * ), Mockito.any())).thenReturn(getChangePassNumingEnum(attributes));
 * 
 * 
 * // method javax.naming.directory.Attribute isActiveAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("isActive")).thenReturn(isActiveAttribute);
 * when(isActiveAttribute.get()).thenReturn("true");
 * 
 * 
 * 
 * 
 * ValidationResponseDto dto=ldapDataStoreSpy.validateUserName(userName);
 * assertThat(dto.getStatus(),is("VALID")); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * validateUserNameNamingExceptionTest() throws Exception { String
 * userName="mockuser"; LdapContext ldapContext =
 * Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * 
 * //getUserDetailSearchResult Dn searchBase = new Dn("uid=" +userName +
 * ",ou=people,c=mycountry"); SearchControls searchControls = new
 * SearchControls();
 * searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE); Attributes
 * attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()), Mockito.eq(
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person))"
 * ), Mockito.any())).thenThrow(new NamingException("connection rejected"));
 * 
 * 
 * // method javax.naming.directory.Attribute isActiveAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("isActive")).thenReturn(isActiveAttribute);
 * when(isActiveAttribute.get()).thenReturn("true");
 * 
 * 
 * 
 * 
 * ValidationResponseDto dto=ldapDataStoreSpy.validateUserName(userName);
 * assertThat(dto.getStatus(),is("VALID")); }
 * 
 * @Test(expected = AuthManagerException.class) public void
 * validateUserNameAuthManagerExceptionTest() throws Exception { String
 * userName="mockuser"; LdapContext ldapContext =
 * Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * 
 * //getUserDetailSearchResult Dn searchBase = new Dn("uid=" +userName +
 * ",ou=people,c=mycountry"); SearchControls searchControls = new
 * SearchControls();
 * searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE); Attributes
 * attributes = Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()), Mockito.eq(
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person))"
 * ), Mockito.any())).thenReturn(getChangePassNumingEnum(attributes));
 * 
 * 
 * // method javax.naming.directory.Attribute isActiveAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("isActive")).thenReturn(null);
 * when(isActiveAttribute.get()).thenReturn("true");
 * 
 * 
 * 
 * 
 * ValidationResponseDto dto=ldapDataStoreSpy.validateUserName(userName);
 * assertThat(dto.getStatus(),is("VALID")); }
 * 
 * 
 * @Test public void getUserDetailBasedOnUidTest() throws Exception {
 * List<String> uids=new ArrayList<String>(); uids.add("mockuser"); LdapContext
 * ldapContext = Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getSearchResultBasedOnId Dn searchBase = new Dn("uid=" + uids.get(0) +
 * ",ou=people,c=mycountry"); Attributes attributes =
 * Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()), Mockito.eq(
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person))"
 * ), Mockito.any())).thenReturn(getChangePassNumingEnum(attributes));
 * 
 * //get role based on uid Attributes roleAttributes =
 * Mockito.mock(Attributes.class); Dn roleSearchBase = new
 * Dn("ou=roles,c=mycountry"); String roleSearchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=uid=" + "mockuser" +
 * ",ou=people,c=mycountry))";
 * when(ldapContext.search(Mockito.eq(roleSearchBase.getName()),
 * Mockito.eq(roleSearchFilter),
 * Mockito.any())).thenReturn(getChangePassNumingEnum(roleAttributes));
 * javax.naming.directory.Attribute roleAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(roleAttributes.get("cn")).thenReturn(roleAttribute);
 * when(roleAttribute.get()).thenReturn("PROCESSOR");
 * 
 * // method
 * 
 * javax.naming.directory.Attribute mailAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("mail")).thenReturn(mailAttribute);
 * when(mailAttribute.get()).thenReturn("mock@mosip.io");
 * javax.naming.directory.Attribute mobileAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("mobile")).thenReturn(mobileAttribute);
 * when(mobileAttribute.get()).thenReturn("+919283928392");
 * javax.naming.directory.Attribute nameAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("cn")).thenReturn(nameAttribute);
 * when(nameAttribute.get()).thenReturn("mock-user");
 * javax.naming.directory.Attribute passwordAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.USER_PASSWORD)).thenReturn(
 * passwordAttribute);
 * when(passwordAttribute.get()).thenReturn("mockpass".getBytes());
 * javax.naming.directory.Attribute firstNameAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.FIRST_NAME)).thenReturn(firstNameAttribute)
 * ; when(firstNameAttribute.get()).thenReturn("fname");
 * javax.naming.directory.Attribute lNameAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.LAST_NAME)).thenReturn(lNameAttribute);
 * when(lNameAttribute.get()).thenReturn("lname");
 * javax.naming.directory.Attribute genderCodeAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.GENDER_CODE)).thenReturn(
 * genderCodeAttribute); when(genderCodeAttribute.get()).thenReturn("MLE");
 * javax.naming.directory.Attribute isActiveAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.IS_ACTIVE)).thenReturn(isActiveAttribute);
 * when(isActiveAttribute.get()).thenReturn("true");
 * javax.naming.directory.Attribute dobAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.DOB)).thenReturn(dobAttribute);
 * when(dobAttribute.get()).thenReturn("2007-12-03");
 * javax.naming.directory.Attribute ridAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.RID)).thenReturn(ridAttribute);
 * when(ridAttribute.get()).thenReturn("83292019283");
 * 
 * 
 * 
 * 
 * UserDetailsResponseDto dto=ldapDataStoreSpy.getUserDetailBasedOnUid(uids);
 * assertThat(dto.getUserDetails().get(0).getName(),is("mock-user")); }
 * 
 * 
 * @Test(expected = AuthManagerException.class) public void
 * getUserDetailBasedOnUidNamingExceptionTest() throws Exception { List<String>
 * uids=new ArrayList<String>(); uids.add("mockuser"); LdapContext ldapContext =
 * Mockito.mock(LdapContext.class);
 * Mockito.doReturn(ldapContext).when(ldapDataStoreSpy).getContext();
 * 
 * //getSearchResultBasedOnId Dn searchBase = new Dn("uid=" + uids.get(0) +
 * ",ou=people,c=mycountry"); Attributes attributes =
 * Mockito.mock(Attributes.class);
 * when(ldapContext.search(Mockito.eq(searchBase.getName()), Mockito.eq(
 * "(&(objectClass=organizationalPerson)(objectClass=inetOrgPerson)(objectClass=person))"
 * ), Mockito.any())).thenThrow(new NamingException("connection rejected"));
 * 
 * //get role based on uid Attributes roleAttributes =
 * Mockito.mock(Attributes.class); Dn roleSearchBase = new
 * Dn("ou=roles,c=mycountry"); String roleSearchFilter =
 * "(&(objectClass=organizationalRole)(roleOccupant=uid=" + "mockuser" +
 * ",ou=people,c=mycountry))";
 * when(ldapContext.search(Mockito.eq(roleSearchBase.getName()),
 * Mockito.eq(roleSearchFilter),
 * Mockito.any())).thenReturn(getChangePassNumingEnum(roleAttributes));
 * javax.naming.directory.Attribute roleAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(roleAttributes.get("cn")).thenReturn(roleAttribute);
 * when(roleAttribute.get()).thenReturn("PROCESSOR");
 * 
 * // method
 * 
 * javax.naming.directory.Attribute mailAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("mail")).thenReturn(mailAttribute);
 * when(mailAttribute.get()).thenReturn("mock@mosip.io");
 * javax.naming.directory.Attribute mobileAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("mobile")).thenReturn(mobileAttribute);
 * when(mobileAttribute.get()).thenReturn("+919283928392");
 * javax.naming.directory.Attribute nameAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get("cn")).thenReturn(nameAttribute);
 * when(nameAttribute.get()).thenReturn("mock-user");
 * javax.naming.directory.Attribute passwordAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.USER_PASSWORD)).thenReturn(
 * passwordAttribute);
 * when(passwordAttribute.get()).thenReturn("mockpass".getBytes());
 * javax.naming.directory.Attribute firstNameAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.FIRST_NAME)).thenReturn(firstNameAttribute)
 * ; when(firstNameAttribute.get()).thenReturn("fname");
 * javax.naming.directory.Attribute lNameAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.LAST_NAME)).thenReturn(lNameAttribute);
 * when(lNameAttribute.get()).thenReturn("lname");
 * javax.naming.directory.Attribute genderCodeAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.GENDER_CODE)).thenReturn(
 * genderCodeAttribute); when(genderCodeAttribute.get()).thenReturn("MLE");
 * javax.naming.directory.Attribute isActiveAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.IS_ACTIVE)).thenReturn(isActiveAttribute);
 * when(isActiveAttribute.get()).thenReturn("true");
 * javax.naming.directory.Attribute dobAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.DOB)).thenReturn(dobAttribute);
 * when(dobAttribute.get()).thenReturn("2007-12-03");
 * javax.naming.directory.Attribute ridAttribute =
 * Mockito.mock(javax.naming.directory.Attribute.class);
 * when(attributes.get(LdapConstants.RID)).thenReturn(ridAttribute);
 * when(ridAttribute.get()).thenReturn("83292019283");
 * 
 * 
 * 
 * 
 * UserDetailsResponseDto dto=ldapDataStoreSpy.getUserDetailBasedOnUid(uids);
 * assertThat(dto.getUserDetails().get(0).getName(),is("mock-user")); }
 * 
 * @Test public void getPagenatedMapTest() throws Exception { UserDetailsDto
 * userDetailsDto = new UserDetailsDto(); userDetailsDto.setUserId("mock-user");
 * List<UserDetailsDto> userDetailsDtos = new ArrayList<UserDetailsDto>();
 * userDetailsDtos.add(userDetailsDto);
 * 
 * Map<Object, Object> dto=ldapDataStoreSpy.getPagenatedMap(userDetailsDtos,1);
 * assertThat(((List<UserDetailsDto>)(dto.get(0))).get(0).getUserId(),is(
 * "mock-user")); }
 * 
 * 
 * 
 * 
 * private Dn createUserDn(String userName) throws LdapInvalidDnException {
 * userName = escapeLDAPValue(userName); return new Dn("uid=" + userName +
 * ",ou=people,c=mycountry"); }
 * 
 * private Dn createRoleDn(String role) throws LdapInvalidDnException { role =
 * escapeLDAPValue(role); return new Dn("cn=" + role + ",ou=roles,c=mycountry");
 * } private String escapeLDAPValue(String ldapString) { if (null == ldapString)
 * return ""; try { // Fix as per //
 * https://stackoverflow.com/questions/31309673/parse-ldap-filter-to-escape-
 * special-characters StringBuilder finalLdapString = new
 * StringBuilder(ldapString.length()); for (byte ldapCharacter :
 * ldapString.getBytes("UTF-8")) { if (ldapCharacter == '\\') {
 * finalLdapString.append("\\5c"); } else if (ldapCharacter == '*') {
 * finalLdapString.append("\\2a"); } else if (ldapCharacter == '(') {
 * finalLdapString.append("\\28"); } else if (ldapCharacter == ')') {
 * finalLdapString.append("\\29"); } else if (ldapCharacter == 0) {
 * finalLdapString.append("\\00"); } else if ((ldapCharacter & 0xff) > 127) {
 * finalLdapString.append("\\").append(to2CharHexString((ldapCharacter &
 * 0xff))); } else { finalLdapString.append((char) ldapCharacter); } } return
 * finalLdapString.toString(); } catch (Exception ex) { return ""; }
 * 
 * }
 * 
 * private String to2CharHexString(int hexValue) { String hexCharacter =
 * Integer.toHexString(hexValue & 0xff); if (hexCharacter.length() == 1) return
 * "0" + hexCharacter; else return hexCharacter; }
 * 
 * 
 * 
 * 
 * 
 * }
 */