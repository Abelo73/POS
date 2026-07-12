package com.novapos.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI novaPosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NovaPOS API")
                        .description("Enterprise Point of Sale platform — modular monolith backend API")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("NovaPOS Team"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://novapos.com")));
    }
}
