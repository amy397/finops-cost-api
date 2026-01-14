package com.finops.api.aws;

import com.finops.api.entity.AwsResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Profile("local")
public class MockResourceService {

    private final Random random = new Random();

    public List<AwsResource> getMockEc2Instances() {
        log.info("[MOCK] EC2 인스턴스 생성");
        List<AwsResource> resources = new ArrayList<>();

        String[] instanceTypes = {"t3.micro", "t3.small", "t3.medium", "m5.large", "c5.xlarge"};
        String[] states = {"running", "running", "running", "stopped", "running"};
        String[] azs = {"ap-northeast-2a", "ap-northeast-2b", "ap-northeast-2c"};

        for (int i = 1; i <= 5; i++) {
            Map<String, String> tags = new HashMap<>();
            tags.put("Name", "finops-ec2-" + i);
            tags.put("Environment", i % 2 == 0 ? "production" : "development");
            tags.put("Team", "platform");

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("imageId", "ami-0c55b159cbfafe1f0");
            metadata.put("privateIpAddress", "10.0." + i + ".100");
            metadata.put("vpcId", "vpc-12345678");
            metadata.put("launchTime", LocalDateTime.now().minusDays(random.nextInt(30)).toString());

            AwsResource resource = AwsResource.builder()
                    .resourceId("i-" + String.format("%017d", i))
                    .resourceType("EC2")
                    .resourceName("finops-ec2-" + i)
                    .region("ap-northeast-2")
                    .availabilityZone(azs[i % 3])
                    .state(states[i % 5])
                    .instanceType(instanceTypes[i % 5])
                    .tags(tags)
                    .metadata(metadata)
                    .lastSyncedAt(LocalDateTime.now())
                    .build();

            resources.add(resource);
        }

        return resources;
    }

    public List<AwsResource> getMockRdsInstances() {
        log.info("[MOCK] RDS 인스턴스 생성");
        List<AwsResource> resources = new ArrayList<>();

        String[] engines = {"mysql", "postgresql", "aurora-mysql"};
        String[] instanceClasses = {"db.t3.micro", "db.t3.small", "db.r5.large"};

        for (int i = 1; i <= 3; i++) {
            Map<String, String> tags = new HashMap<>();
            tags.put("Name", "finops-rds-" + i);
            tags.put("Environment", i == 1 ? "production" : "development");

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("engine", engines[i - 1]);
            metadata.put("engineVersion", "8.0.28");
            metadata.put("allocatedStorage", 20 * i);
            metadata.put("storageType", "gp2");
            metadata.put("multiAZ", i == 1);
            metadata.put("endpoint", "finops-rds-" + i + ".xxxxx.ap-northeast-2.rds.amazonaws.com");

            AwsResource resource = AwsResource.builder()
                    .resourceId("arn:aws:rds:ap-northeast-2:123456789012:db:finops-rds-" + i)
                    .resourceType("RDS")
                    .resourceName("finops-rds-" + i)
                    .region("ap-northeast-2")
                    .availabilityZone("ap-northeast-2a")
                    .state("available")
                    .instanceType(instanceClasses[i - 1])
                    .tags(tags)
                    .metadata(metadata)
                    .lastSyncedAt(LocalDateTime.now())
                    .build();

            resources.add(resource);
        }

        return resources;
    }

    public List<AwsResource> getMockS3Buckets() {
        log.info("[MOCK] S3 버킷 생성");
        List<AwsResource> resources = new ArrayList<>();

        String[] bucketNames = {"finops-data-bucket", "finops-logs-bucket", "finops-backup-bucket", "finops-assets-bucket"};

        for (int i = 0; i < bucketNames.length; i++) {
            Map<String, String> tags = new HashMap<>();
            tags.put("Name", bucketNames[i]);
            tags.put("Environment", i < 2 ? "production" : "development");

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("creationDate", LocalDateTime.now().minusDays(60 + i * 10).toString());

            AwsResource resource = AwsResource.builder()
                    .resourceId("arn:aws:s3:::" + bucketNames[i])
                    .resourceType("S3")
                    .resourceName(bucketNames[i])
                    .region("ap-northeast-2")
                    .state("active")
                    .tags(tags)
                    .metadata(metadata)
                    .lastSyncedAt(LocalDateTime.now())
                    .build();

            resources.add(resource);
        }

        return resources;
    }

    public List<AwsResource> getMockLambdaFunctions() {
        log.info("[MOCK] Lambda 함수 생성");
        List<AwsResource> resources = new ArrayList<>();

        String[] functionNames = {"finops-cost-processor", "finops-alert-handler", "finops-data-sync"};
        String[] runtimes = {"python3.11", "nodejs18.x", "java17"};
        int[] memorySizes = {128, 256, 512};

        for (int i = 0; i < functionNames.length; i++) {
            Map<String, String> tags = new HashMap<>();
            tags.put("Name", functionNames[i]);
            tags.put("Environment", "production");

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("runtime", runtimes[i]);
            metadata.put("handler", "index.handler");
            metadata.put("memorySize", memorySizes[i]);
            metadata.put("timeout", 30);
            metadata.put("lastModified", LocalDateTime.now().minusDays(5 + i).toString());

            AwsResource resource = AwsResource.builder()
                    .resourceId("arn:aws:lambda:ap-northeast-2:123456789012:function:" + functionNames[i])
                    .resourceType("LAMBDA")
                    .resourceName(functionNames[i])
                    .region("ap-northeast-2")
                    .state("Active")
                    .tags(tags)
                    .metadata(metadata)
                    .lastSyncedAt(LocalDateTime.now())
                    .build();

            resources.add(resource);
        }

        return resources;
    }

    public List<AwsResource> getAllMockResources() {
        List<AwsResource> all = new ArrayList<>();
        all.addAll(getMockEc2Instances());
        all.addAll(getMockRdsInstances());
        all.addAll(getMockS3Buckets());
        all.addAll(getMockLambdaFunctions());
        return all;
    }
}
