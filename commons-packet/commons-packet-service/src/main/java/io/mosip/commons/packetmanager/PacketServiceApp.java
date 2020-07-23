package io.mosip.commons.packetmanager;

import com.sun.org.apache.bcel.internal.generic.NEW;
import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * The Packet Service Application
 * 
 * @author Monobikash Das
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = { "io.mosip.*"})
@EnableCaching
public class PacketServiceApp {



    @Autowired
    private PacketReader packetReader;

    @Autowired
    private PacketWriter packetWriter;

    public static void main(String[] args) {
        SpringApplication.run(PacketServiceApp.class, args);
    }
}
