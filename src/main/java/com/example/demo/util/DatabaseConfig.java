package com.example.demo.util;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
//    @Primary
//    @Bean(name = "primaryDataSourceProperties")
//    @ConfigurationProperties(prefix="spring.datasource")
//    public DataSourceProperties primaryDataSourceProperties() {
//        return new DataSourceProperties();
//    }

    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
//        return primaryDataSourceProperties().initializeDataSourceBuilder().build();
    }

//    @Bean(name = "secondaryDataSourceProperties")
//    @ConfigurationProperties(prefix="spring.seconddatasource")
//    public DataSourceProperties secondaryDataSourceProperties() {
//        return new DataSourceProperties();
//    }

    @Bean(name = "secondaryDataSource")
    @ConfigurationProperties(prefix="spring.seconddatasource")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
//        return secondaryDataSourceProperties().initializeDataSourceBuilder().build();
    }
}


