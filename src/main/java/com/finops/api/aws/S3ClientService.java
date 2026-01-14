package com.finops.api.aws;

import com.finops.api.entity.AwsResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("!local")
@RequiredArgsConstructor
public class S3ClientService {

    private final S3Client s3Client;

    @Value("${aws.region:ap-northeast-2}")
    private String defaultRegion;

    public List<AwsResource> listBuckets() {
        log.info("S3 버킷 조회 시작");
        List<AwsResource> resources = new ArrayList<>();

        try {
            ListBucketsRequest request = ListBucketsRequest.builder().build();
            var response = s3Client.listBuckets(request);

            for (Bucket bucket : response.buckets()) {
                AwsResource resource = mapToAwsResource(bucket);
                resources.add(resource);
            }

            log.info("S3 버킷 {} 개 조회 완료", resources.size());
        } catch (Exception e) {
            log.error("S3 버킷 조회 실패", e);
            throw new RuntimeException("S3 버킷 조회 실패", e);
        }

        return resources;
    }

    private AwsResource mapToAwsResource(Bucket bucket) {
        Map<String, String> tags = new HashMap<>();
        String region = defaultRegion;

        try {
            // Get bucket location
            GetBucketLocationRequest locationRequest = GetBucketLocationRequest.builder()
                    .bucket(bucket.name())
                    .build();
            var locationResponse = s3Client.getBucketLocation(locationRequest);
            if (locationResponse.locationConstraint() != null) {
                region = locationResponse.locationConstraintAsString();
            }

            // Get bucket tagging
            GetBucketTaggingRequest taggingRequest = GetBucketTaggingRequest.builder()
                    .bucket(bucket.name())
                    .build();
            var taggingResponse = s3Client.getBucketTagging(taggingRequest);
            tags = taggingResponse.tagSet().stream()
                    .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> a));
        } catch (S3Exception e) {
            // Tagging might not exist, ignore
            log.debug("S3 버킷 {} 태그 조회 실패 (태그 없음)", bucket.name());
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("creationDate", bucket.creationDate() != null ? bucket.creationDate().toString() : null);

        return AwsResource.builder()
                .resourceId("arn:aws:s3:::" + bucket.name())
                .resourceType("S3")
                .resourceName(bucket.name())
                .region(region.isEmpty() ? "us-east-1" : region)
                .state("active")
                .tags(tags)
                .metadata(metadata)
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }
}
