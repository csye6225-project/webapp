package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        System.setProperty("aws.access_key_id", "AKIAVMPSPWKHQKFZLVPL");
        System.setProperty("aws.secret_access_key", "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");
        System.setProperty("javax.net.ssl.trustStore", "/home/ubuntu/app/clientkeystore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "12345678");
        SpringApplication.run(DemoApplication.class, args);
    }
}
