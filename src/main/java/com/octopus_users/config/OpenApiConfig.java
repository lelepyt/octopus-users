package com.octopus_users.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI usersOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Octopus Users API")
                        .description("A RESTful API for aggregating user data from multiple sources")
                        .version("1.0.0"));
    }
}