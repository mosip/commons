package io.mosip.commons.packetmanager;

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
@ComponentScan(basePackages = {"io.mosip.commons.packet.*" ,
         "io.mosip.commons.packetmanager.*", "${mosip.auth.adapter.impl.basepackage}"})
@EnableCaching
public class PacketServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(PacketServiceApp.class, args);
    }
}
