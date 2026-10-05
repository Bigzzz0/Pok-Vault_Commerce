package com.pokevault.common.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pokeVaultOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pokémon TCG Pocket Inventory Management API")
                        .description("RESTful API documentation for CP353002 Principles of Software Design and Development.\n" +
                                     "Covers Card Catalog, Inventory/Stock Management, Order Placement, Strategy Discounts & State Machine.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("CP353002 Student Development Team")
                                .email("dev@tcgpocket.local"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation & Architecture Blueprints")
                        .url("https://github.com/your-username/tcg-pocket-inventory"));
    }
}
