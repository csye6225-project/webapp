package com.example.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.demo.repository",
        entityManagerFactoryRef = "lcemfb1",
        transactionManagerRef = "ptm1" )
public class JpaConfig1 {
    @Autowired
    @Qualifier(value = "primaryDataSource")
    DataSource ds1;
    @Autowired
    JpaProperties jp;

    @Bean
    @Primary
    LocalContainerEntityManagerFactoryBean lcemfb1(EntityManagerFactoryBuilder builder) {
//        Map<String, String> properties = jp.getProperties();
//        properties.put("spring.jpa.hibernate.hbm2ddl.auto", "update");
        return builder.dataSource(ds1)
                .properties(jp.getProperties())
                .packages("com.example.demo.model")
                .build();
    }

    @Bean
    @Primary
    PlatformTransactionManager ptm1(EntityManagerFactoryBuilder builder) {
        LocalContainerEntityManagerFactoryBean l1 = lcemfb1(builder);
        return new JpaTransactionManager(l1.getObject());
    }
}
