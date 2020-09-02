package io.mosip.kernel.clientcryptoservice.service.impl;

import io.mosip.kernel.clientcryptoservice.constant.ClientCryptoErrorConstants;
import io.mosip.kernel.clientcryptoservice.constant.ClientCryptoManagerConstant;
import io.mosip.kernel.clientcryptoservice.exception.ClientCryptoException;
import io.mosip.kernel.clientcryptoservice.exception.ClientCryptoReloadException;
import io.mosip.kernel.clientcryptoservice.service.spi.ClientCryptoService;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.crypto.jce.util.CryptoUtils;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.validation.constraints.NotNull;
import java.security.Security;
import java.util.Base64;

public class ClientCryptoFacade {

    private static final Logger LOGGER = KeymanagerLogger.getLogger(ClientCryptoFacade.class);

    private static ClientCryptoService clientCryptoService = null;
    private static BouncyCastleProvider provider;

    static {
        provider = new BouncyCastleProvider();
        Security.addProvider(provider);
        Security.setProperty("crypto.policy", "unlimited");
    }

    private static void initializeClientSecurity() throws ClientCryptoException, ClientCryptoReloadException {
        LOGGER.debug(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                ClientCryptoManagerConstant.EMPTY, "initializeClientSecurity >>> started");
        try {
            clientCryptoService = new TPMClientCryptoServiceImpl();

        } catch(Throwable e) {
            LOGGER.debug(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                    ClientCryptoManagerConstant.EMPTY, ExceptionUtils.getStackTrace(e));
            LOGGER.warn(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                    ClientCryptoManagerConstant.EMPTY, "SWITCHING TO LOCAL SECURITY IMPL");

            try {
                clientCryptoService = new LocalClientCryptoServiceImpl();
            } catch (ClientCryptoReloadException clientCryptoReloadException) {
                throw clientCryptoReloadException;
            } catch (Throwable ex) {
                LOGGER.error(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                        ClientCryptoManagerConstant.EMPTY, ExceptionUtils.getStackTrace(ex));
                LOGGER.error(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                        ClientCryptoManagerConstant.EMPTY,"Failed to load Client security instance");
            }
        }

        if(clientCryptoService == null) {
            LOGGER.error(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                    ClientCryptoManagerConstant.EMPTY, "Failed to get client security instance.");
            throw new ClientCryptoException(ClientCryptoErrorConstants.INITIALIZATION_ERROR.getErrorCode(),
                    ClientCryptoErrorConstants.INITIALIZATION_ERROR.getErrorMessage());
        }

        LOGGER.debug(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                ClientCryptoManagerConstant.EMPTY, "initializeClientSecurity >>> Done");
    }

    public static ClientCryptoService getClientSecurity() throws ClientCryptoException, ClientCryptoReloadException {
        if(clientCryptoService == null ) {
            initializeClientSecurity();
        }
        return clientCryptoService;
    }

    public static String getPublicKey(@NotNull String environment)
            throws ClientCryptoException, ClientCryptoReloadException {
        if(ClientCryptoManagerConstant.SERVER_PROD_PROFILE.equalsIgnoreCase(environment)
                && !clientCryptoService.isTPMInstance()) {
            LOGGER.error(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                    ClientCryptoManagerConstant.EMPTY, "TPM IS REQUIRED TO BE ENABLED.");

            throw new ClientCryptoException(ClientCryptoErrorConstants.TPM_REQUIRED.getErrorCode(),
                    ClientCryptoErrorConstants.TPM_REQUIRED.getErrorMessage());
        }

        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                ClientCryptoManagerConstant.EMPTY, "CURRENT PROFILE : " +
                environment != null ? environment : ClientCryptoManagerConstant.EMPTY);

        return CryptoUtil.encodeBase64(getClientSecurity().getSigningPublicPart());
    }
}
