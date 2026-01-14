package com.finops.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.pricing.PricingClient;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.resourcegroupstaggingapi.ResourceGroupsTaggingApiClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;

@Slf4j
@Configuration
public class AwsSdkConfig {

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    @Bean
    @Profile("!local")
    public Ec2Client ec2Client() {
        log.info("EC2 Client 생성 - Region: {}", region);
        return Ec2Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("!local")
    public RdsClient rdsClient() {
        log.info("RDS Client 생성 - Region: {}", region);
        return RdsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("!local")
    public S3Client s3Client() {
        log.info("S3 Client 생성 - Region: {}", region);
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("!local")
    public LambdaClient lambdaClient() {
        log.info("Lambda Client 생성 - Region: {}", region);
        return LambdaClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("!local")
    public CloudWatchClient cloudWatchClient() {
        log.info("CloudWatch Client 생성 - Region: {}", region);
        return CloudWatchClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("!local")
    public ResourceGroupsTaggingApiClient taggingClient() {
        log.info("Resource Groups Tagging API Client 생성 - Region: {}", region);
        return ResourceGroupsTaggingApiClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("!local")
    public PricingClient pricingClient() {
        log.info("Pricing Client 생성 - Region: us-east-1 (Pricing API는 us-east-1에서만 사용 가능)");
        return PricingClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Profile("!local")
    public SesClient sesClient() {
        log.info("SES Client 생성 - Region: {}", region);
        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    // Local profile용 Mock beans
    @Bean
    @Profile("local")
    public Ec2Client mockEc2Client() {
        log.info("Mock EC2 Client 생성 (로컬 환경)");
        return null;
    }

    @Bean
    @Profile("local")
    public RdsClient mockRdsClient() {
        log.info("Mock RDS Client 생성 (로컬 환경)");
        return null;
    }

    @Bean
    @Profile("local")
    public S3Client mockS3Client() {
        log.info("Mock S3 Client 생성 (로컬 환경)");
        return null;
    }

    @Bean
    @Profile("local")
    public LambdaClient mockLambdaClient() {
        log.info("Mock Lambda Client 생성 (로컬 환경)");
        return null;
    }

    @Bean
    @Profile("local")
    public CloudWatchClient mockCloudWatchClient() {
        log.info("Mock CloudWatch Client 생성 (로컬 환경)");
        return null;
    }

    @Bean
    @Profile("local")
    public ResourceGroupsTaggingApiClient mockTaggingClient() {
        log.info("Mock Tagging Client 생성 (로컬 환경)");
        return null;
    }

    @Bean
    @Profile("local")
    public PricingClient mockPricingClient() {
        log.info("Mock Pricing Client 생성 (로컬 환경)");
        return null;
    }

    @Bean
    @Profile("local")
    public SesClient mockSesClient() {
        log.info("Mock SES Client 생성 (로컬 환경)");
        return null;
    }
}
