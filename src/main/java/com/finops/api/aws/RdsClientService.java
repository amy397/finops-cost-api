package com.finops.api.aws;

import com.finops.api.entity.AwsResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;
import software.amazon.awssdk.services.rds.model.Tag;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("!local")
@RequiredArgsConstructor
public class RdsClientService {

    private final RdsClient rdsClient;

    public List<AwsResource> describeDbInstances() {
        log.info("RDS 인스턴스 조회 시작");
        List<AwsResource> resources = new ArrayList<>();

        try {
            DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder().build();
            var response = rdsClient.describeDBInstances(request);

            for (DBInstance dbInstance : response.dbInstances()) {
                AwsResource resource = mapToAwsResource(dbInstance);
                resources.add(resource);
            }

            log.info("RDS 인스턴스 {} 개 조회 완료", resources.size());
        } catch (Exception e) {
            log.error("RDS 인스턴스 조회 실패", e);
            throw new RuntimeException("RDS 인스턴스 조회 실패", e);
        }

        return resources;
    }

    private AwsResource mapToAwsResource(DBInstance dbInstance) {
        Map<String, String> tags = new HashMap<>();
        if (dbInstance.tagList() != null) {
            tags = dbInstance.tagList().stream()
                    .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> a));
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("engine", dbInstance.engine());
        metadata.put("engineVersion", dbInstance.engineVersion());
        metadata.put("allocatedStorage", dbInstance.allocatedStorage());
        metadata.put("storageType", dbInstance.storageType());
        metadata.put("multiAZ", dbInstance.multiAZ());
        metadata.put("endpoint", dbInstance.endpoint() != null ? dbInstance.endpoint().address() : null);
        metadata.put("port", dbInstance.endpoint() != null ? dbInstance.endpoint().port() : null);
        metadata.put("vpcId", dbInstance.dbSubnetGroup() != null ? dbInstance.dbSubnetGroup().vpcId() : null);

        String region = "ap-northeast-2";
        if (dbInstance.availabilityZone() != null) {
            region = dbInstance.availabilityZone().substring(0, dbInstance.availabilityZone().length() - 1);
        }

        return AwsResource.builder()
                .resourceId(dbInstance.dbInstanceArn())
                .resourceType("RDS")
                .resourceName(dbInstance.dbInstanceIdentifier())
                .region(region)
                .availabilityZone(dbInstance.availabilityZone())
                .state(dbInstance.dbInstanceStatus())
                .instanceType(dbInstance.dbInstanceClass())
                .tags(tags)
                .metadata(metadata)
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }
}
