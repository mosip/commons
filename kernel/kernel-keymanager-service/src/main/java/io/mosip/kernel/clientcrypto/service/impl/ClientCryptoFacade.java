package io.mosip.kernel.clientcrypto.service.impl;

import io.mosip.kernel.clientcrypto.constant.ClientCryptoErrorConstants;
import io.mosip.kernel.clientcrypto.constant.ClientCryptoManagerConstant;
import io.mosip.kernel.clientcrypto.exception.ClientCryptoException;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoService;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
public class ClientCryptoFacade {

    private static final Logger LOGGER = KeymanagerLogger.getLogger(ClientCryptoFacade.class);
    private static ClientCryptoService clientCryptoService = null;

    @Autowired
    private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;


    private void initializeClientSecurity() {
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
                clientCryptoService = new LocalClientCryptoServiceImpl(cryptoCore);
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

    public ClientCryptoService getClientSecurity() {
        if(clientCryptoService == null) {
            initializeClientSecurity();
        }
        return clientCryptoService;
    }
}
