package com.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Elements;

@Configuration
public class SwaggerConfig {


    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(components())
                .addSecurityItem(securityRequirement())
                .info(new Info().title("Dev Station API 명세서")
                        .version("v0.0.1")
                        .description("개발자 취업 도우미 애플리케이션입니다."));
    }
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList(Elements.JWT);
    }


    private Components components() {
        return new Components().addSecuritySchemes(Elements.JWT, securityScheme());
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(Elements.JWT)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat(Elements.JWT);
    }

}
