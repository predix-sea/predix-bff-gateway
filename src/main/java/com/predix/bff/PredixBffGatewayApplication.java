package com.predix.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PredixBffGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PredixBffGatewayApplication.class, args);
    }
}
