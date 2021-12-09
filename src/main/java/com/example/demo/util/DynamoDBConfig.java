package com.example.demo.util;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamoDBConfig {
    @Value("${aws.region}")
    private String region;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
//        AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAVMPSPWKHQKFZLVPL",
//                "FpCY9r1lswGZpy/8qQDE9PWznk6d3haDx0Bw2dCY");
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(region)
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

        return amazonDynamoDB;
    }
}
