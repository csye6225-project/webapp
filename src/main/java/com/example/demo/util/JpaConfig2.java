package com.example.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.demo.repository1",
        entityManagerFactoryRef = "lcemfb2",
        transactionManagerRef = "ptm2" )
public class JpaConfig2 {
    @Autowired
    @Qualifier(value = "secondaryDataSource")
    DataSource ds2;
    @Autowired
    JpaProperties jp;

    @Bean
    LocalContainerEntityManagerFactoryBean lcemfb2(EntityManagerFactoryBuilder builder) {
        return builder.dataSource(ds2)
                .properties(jp.getProperties())
                .packages("com.example.demo.model")
                .build();
    }

    @Bean
    PlatformTransactionManager ptm2(EntityManagerFactoryBuilder builder) {
        LocalContainerEntityManagerFactoryBean lcemfb = lcemfb2(builder);
        return new JpaTransactionManager(lcemfb.getObject());
    }
}
