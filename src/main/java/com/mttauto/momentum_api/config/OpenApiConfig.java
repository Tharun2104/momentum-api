package com.mttauto.momentum_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI momentumOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Momentum API")
                        .version("0.1.0")
                        .description("Backend APIs for Momentum run tracking."));
    }
}
