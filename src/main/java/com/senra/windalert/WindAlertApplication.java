package com.senra.windalert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senra.windalert.config.WindAlertProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(WindAlertProperties.class)
public class WindAlertApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplicationBuilder(WindAlertApplication.class)
                .web(WebApplicationType.NONE)
                .build();

        ConfigurableApplicationContext context = app.run(args);

        System.exit(SpringApplication.exit(context));
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
