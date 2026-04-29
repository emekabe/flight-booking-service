package com.emekachukwulobe.flightbookingservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "liquibase")
public class LiquibaseProperties {
    private String changeLog = "classpath:db/changelog/db.changelog-master.yaml";
    private boolean shouldRun = true;
}
