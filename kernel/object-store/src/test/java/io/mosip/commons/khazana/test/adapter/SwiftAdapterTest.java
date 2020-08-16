package io.mosip.commons.khazana.test.adapter;

import io.mosip.commons.khazana.impl.SwiftAdapter;
import org.javaswift.joss.client.factory.AccountConfig;
import org.javaswift.joss.client.factory.AccountFactory;
import org.javaswift.joss.model.Account;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.PropertySource;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AccountFactory.class})
@PropertySource("classpath:application-test.properties")
public class SwiftAdapterTest {

    private static final String account = "acc";
    private static final String container = "reg123";
    private static final String objectName = "id";

    @InjectMocks
    private SwiftAdapter swiftAdapter = new SwiftAdapter();

    private Account mockAccount;

    @Mock
    private AccountFactory accountFactory;

    @Before
    public void setup() throws Exception {
        /*PowerMockito.whenNew(AccountFactory.class).withAnyArguments().thenReturn(accountFactory);
        when(accountFactory.createAccount()).thenReturn(mockAccount);*/

        AccountConfig config = new AccountConfig();
        config.setUsername("");
        config.setPassword("");
        config.setAuthUrl("");
        config.setTenantName("");
        config.setMock(true);
        mockAccount = new AccountFactory(config).createAccount();

        PowerMockito.whenNew(AccountFactory.class).withAnyArguments().thenReturn(accountFactory);
        when(accountFactory.createAccount()).thenReturn(mockAccount);
    }

    @Test
    @Ignore
    public void testGetObject() {
        InputStream is = swiftAdapter.getObject(account, container, objectName);
        assertNotNull("Get object should not be null", is);
    }
}
