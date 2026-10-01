package com.pokevault.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pokeVaultOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PokéVault Commerce API")
                        .description("Pokémon TCG Pocket Vault & Chat Commerce Trade Platform")
                        .version("v0.0.1"));
    }
}
