package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        System.setProperty("aws.sns.arn", "arn:aws:sns:us-east-1:370412597903:verification-notice");
        SpringApplication.run(DemoApplication.class, args);
    }
}
