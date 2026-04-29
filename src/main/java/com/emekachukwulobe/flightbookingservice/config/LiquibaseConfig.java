package com.emekachukwulobe.flightbookingservice.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LiquibaseConfig {

    private final LiquibaseProperties properties;

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setDataSource(dataSource);
        liquibase.setShouldRun(properties.isShouldRun());
        return liquibase;
    }
}
