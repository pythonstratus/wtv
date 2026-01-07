package com.entity.wtv.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Weekly Time Verification (WTV) API")
                        .version("2.0.0")
                        .description("""
                                WTV Service - Modernized from legacy Pro*C
                                
                                Provides APIs for:
                                - Group Weekly Hours Verification (main view)
                                - Employee Timesheet Detail (drill-down)
                                - Pay Period / Reporting Month navigation
                                
                                Legacy source: entity_common.pc, ent_timeverify.pc
                                """)
                        .contact(new Contact()
                                .name("ENTITY Team")
                                .email("test@test.com")));
    }
}
