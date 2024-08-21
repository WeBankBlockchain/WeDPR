/** Copyright (C) @2014-2022 Webank */
package com.webank.wedpr.components.user.config;

import java.util.ArrayList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ConditionalOnProperty(name = "springfox.documentation.enabled", havingValue = "true")
public class SwaggerConfig {

    @Bean
    public Docket documentation() {
        RequestParameter requestParameter =
                new RequestParameterBuilder()
                        .name("Authorization")
                        .description("jwt")
                        .in(ParameterType.HEADER)
                        .build();
        ArrayList<RequestParameter> requestParameterList = new ArrayList<>();
        requestParameterList.add(requestParameter);
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com"))
                .build()
                .pathMapping("/")
                .apiInfo(apiInfo())
                .globalRequestParameters(requestParameterList)
                .enable(true);
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("API document")
                .description("wedpr api")
                .version("3.0")
                .build();
    }
}
