package com.vetcitas.gestion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Añade un título comprensible al contrato que Swagger genera a partir de los controladores. */
@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI documentacionApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("VetCitas — API de citas veterinarias")
                .version("1.0")
                .description(
                    "CRUD de usuarios, servicios y reservas. Inicia sesión en el panel antes de"
                        + " probar. Las operaciones respetan el rol de la sesión y la protección"
                        + " CSRF."));
  }
}
