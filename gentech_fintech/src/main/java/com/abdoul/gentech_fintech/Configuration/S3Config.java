package com.abdoul.gentech_fintech.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

@Configuration
public class S3Config {
    @Value("${s3.region}")
    private String region;

    @Value("${s3.secret.key}")
    private String secretKey;

    @Value("$s3.access.key}")
    private String accessKey;

    @Bean
    public S3Client s3Client(){
        AwsCredentials credentials = AwsBasicCredentials.builder().accessKeyId(accessKey).secretAccessKey(secretKey).build();

        return S3Client.builder().region(Region.of(region)).credentialsProvider(StaticCredentialsProvider.create(credentials)).build();

    }

    @Bean
    public S3Presigner preSigner (){
        AwsBasicCredentials credentials = AwsBasicCredentials.builder().accessKeyId(accessKey).secretAccessKey(secretKey).build();
        return S3Presigner.builder().credentialsProvider(StaticCredentialsProvider.create(credentials)).region(Region.of(region)).build();
    }
}