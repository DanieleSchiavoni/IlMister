package com.ilmister.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** La documentazione OpenAPI: Swagger UI su /swagger-ui.html, con il pulsante "Authorize" per il token JWT */
@Configuration
public class ConfigurazioneOpenApi {

    private static final String SCHEMA_JWT = "tokenJwt";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Il Mister - API")
                        .version("0.1")
                        .description("""
                                API del manageriale di calcio Il Mister.
                                Per provare gli endpoint protetti: registrati con POST /api/auth/register, fai il login \
                                con POST /api/auth/login e incolla l'accessToken nel pulsante Authorize.
                                Tutti gli errori hanno la stessa forma: timestamp, status, errore, messaggio, percorso \
                                (e, per la validazione, l'elenco dei campi non validi)."""))
                .components(new Components().addSecuritySchemes(SCHEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEMA_JWT));
    }
}
