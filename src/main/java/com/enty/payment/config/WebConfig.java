package com.enty.payment.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String logoDirectory;
    private final String receiptDirectory;

    public WebConfig(@Value("${app.storage.customer-logo-directory}") String logoDirectory,
            @Value("${app.storage.payment-receipt-directory}") String receiptDirectory) {
        this.logoDirectory = logoDirectory;
        this.receiptDirectory = receiptDirectory;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(logoDirectory).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/files/customer-logos/**").addResourceLocations(location);
        registry.addResourceHandler("/files/payment-receipts/**")
                .addResourceLocations(Path.of(receiptDirectory).toAbsolutePath().normalize().toUri().toString());
    }
}
