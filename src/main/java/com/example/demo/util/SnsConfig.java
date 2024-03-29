package com.example.demo.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnsConfig {
    @Value("${aws.region}")
    private String region;

    @Bean
    public AmazonSNS amazonSNS() {
//        AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAVMPSPWKHQKFZLVPL",
//                "FpCY9r1lswGZpy/8qQDE9PWznk6d3haDx0Bw2dCY");
        AmazonSNS amazonSNS = AmazonSNSClient.builder()
                .withRegion(region)
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
        return amazonSNS;
    }
}
