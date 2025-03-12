package com.mmt.flights.config;

import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import com.mmt.flights.constants.ProtobufPropertiesModule;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.configuration.ObjectMapperConfigured;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.mmt.flights.constants.SwaggerConstants.*;

/**
 * @author MMT5680
 *
 *         Contains swagger config for bootstrapping swagger with some default
 *         values.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig implements ApplicationListener<ObjectMapperConfigured> {

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage(SWAGER_CONTROLLER_SCANNER_PACKAGE))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
            SWAGGER_API_TITLE, 
            SWAGGER_API_DESCRIPTION, 
            SWAGGER_API_TERMS_OF_CONDITION,
            SWAGGER_API_TERMS_OF_SERVICE,
            "",
            SWAGGER_API_LICENSE,
            SWAGGER_API_LICENSE_URL);
    }

    @Override
    public void onApplicationEvent(ObjectMapperConfigured omc) {
        omc.getObjectMapper().registerModule(new ProtobufModule());
        omc.getObjectMapper().registerModule(new ProtobufPropertiesModule());
    }
}
