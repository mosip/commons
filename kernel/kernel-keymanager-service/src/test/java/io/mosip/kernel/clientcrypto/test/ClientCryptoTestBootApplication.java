package io.mosip.kernel.clientcrypto.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "io.mosip.kernel.clientcrypto.*", "io.mosip.kernel.crypto.jce.*"})
public class ClientCryptoTestBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientCryptoTestBootApplication.class, args);
    }
}
