package com.example.demo.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.util.Properties;

public class HibernateUtil {

    private static SessionFactory sessionfactory;
    public static SessionFactory buildSessionFactory() throws IOException {
        Configuration config = new Configuration();
        Properties properties = new Properties();
        HibernateUtil.class.getClassLoader();
        properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("application.properties"));

        config.mergeProperties(properties).configure("hibernate.cfg.xml");

//        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
//                .applySettings(config.getProperties()).build();

        sessionfactory = config.buildSessionFactory();
        return sessionfactory;
    }
}
