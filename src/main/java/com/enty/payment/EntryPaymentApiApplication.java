package com.enty.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EntryPaymentApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(EntryPaymentApiApplication.class, args);
    }
}
