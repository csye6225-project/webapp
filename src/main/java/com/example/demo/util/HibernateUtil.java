package com.example.demo.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HibernateUtil {

    private static SessionFactory sessionfactory;
    public static SessionFactory buildSessionFactory() throws IOException {
        Configuration config = new Configuration();
        Properties properties = new Properties();
        HibernateUtil.class.getClassLoader();
        FileInputStream fis = new FileInputStream("/home/ubuntu/app/application.properties");
//        File file = ResourceUtils.getFile("classpath:application.properties");
//        FileInputStream fis = new FileInputStream(file);
        properties.load(fis);

        config.mergeProperties(properties).configure("hibernate.cfg.xml");

        sessionfactory = config.buildSessionFactory();
        return sessionfactory;
    }
}
