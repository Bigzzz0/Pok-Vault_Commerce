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
                        .title("PokéVault Commerce API")
                        .description("RESTful API documentation for CP353002 Principles of Software Design and Development.\n" +
                                     "Covers Card Catalog, Vault/Stock Management, Order Placement (Chat Commerce Handshake), " +
                                     "Strategy Discounts, Order State Machine & In-Game Trade Matching.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("PokéVault Commerce Team (CP353002)")
                                .url("https://github.com/Bigzzz0/Pok-Vault_Commerce"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation & Architecture Blueprints")
                        .url("https://github.com/Bigzzz0/Pok-Vault_Commerce/tree/main/doc"));
    }
}
