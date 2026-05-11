package com.wtls.blog_server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Antigravity Blog Server API")
                        .description("拼团商城与积分系统接口文档")
                        .version("v1.0.0")
                        .contact(new Contact().name("Antigravity").url("https://github.com/wtls").email("admin@example.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
