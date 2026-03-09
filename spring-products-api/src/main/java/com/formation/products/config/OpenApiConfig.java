package com.formation.products.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Products API")
                        .description("API REST de gestion des produits, categories et commandes avec validation, gestion d'erreurs et authentification JWT.")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Fethi Sedjai")
                                .email("fethi.sedjai@example.com")))
                .addServersItem(new Server().url("http://localhost:8081").description("Local environment"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
