package io.mosip.commons.packetmanager;

import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packet.facade.PacketWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

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
