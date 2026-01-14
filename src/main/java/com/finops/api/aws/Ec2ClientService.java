package com.finops.api.aws;

import com.finops.api.entity.AwsResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Tag;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("!local")
@RequiredArgsConstructor
public class Ec2ClientService {

    private final Ec2Client ec2Client;

    public List<AwsResource> describeInstances() {
        log.info("EC2 인스턴스 조회 시작");
        List<AwsResource> resources = new ArrayList<>();

        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            var response = ec2Client.describeInstances(request);

            for (Reservation reservation : response.reservations()) {
                for (Instance instance : reservation.instances()) {
                    AwsResource resource = mapToAwsResource(instance);
                    resources.add(resource);
                }
            }

            log.info("EC2 인스턴스 {} 개 조회 완료", resources.size());
        } catch (Exception e) {
            log.error("EC2 인스턴스 조회 실패", e);
            throw new RuntimeException("EC2 인스턴스 조회 실패", e);
        }

        return resources;
    }

    private AwsResource mapToAwsResource(Instance instance) {
        Map<String, String> tags = instance.tags().stream()
                .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> a));

        String name = tags.getOrDefault("Name", instance.instanceId());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("imageId", instance.imageId());
        metadata.put("privateIpAddress", instance.privateIpAddress());
        metadata.put("publicIpAddress", instance.publicIpAddress());
        metadata.put("vpcId", instance.vpcId());
        metadata.put("subnetId", instance.subnetId());
        metadata.put("launchTime", instance.launchTime() != null ? instance.launchTime().toString() : null);
        metadata.put("platform", instance.platformAsString());
        metadata.put("architecture", instance.architectureAsString());

        return AwsResource.builder()
                .resourceId(instance.instanceId())
                .resourceType("EC2")
                .resourceName(name)
                .region(extractRegion(instance.placement()))
                .availabilityZone(instance.placement() != null ? instance.placement().availabilityZone() : null)
                .state(instance.state() != null ? instance.state().nameAsString() : null)
                .instanceType(instance.instanceTypeAsString())
                .tags(tags)
                .metadata(metadata)
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }

    private String extractRegion(software.amazon.awssdk.services.ec2.model.Placement placement) {
        if (placement != null && placement.availabilityZone() != null) {
            String az = placement.availabilityZone();
            return az.substring(0, az.length() - 1);
        }
        return "ap-northeast-2";
    }
}
