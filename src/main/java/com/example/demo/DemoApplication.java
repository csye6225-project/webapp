package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        System.setProperty("javax.net.ssl.trustStore", "/home/ubuntu/app/clientkeystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "12345678");
        SpringApplication.run(DemoApplication.class, args);
    }
}
