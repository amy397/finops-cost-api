package com.finops.api.aws;

import com.finops.api.entity.AwsResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Profile("!local")
@RequiredArgsConstructor
public class LambdaClientService {

    private final LambdaClient lambdaClient;

    @Value("${aws.region:ap-northeast-2}")
    private String defaultRegion;

    public List<AwsResource> listFunctions() {
        log.info("Lambda 함수 조회 시작");
        List<AwsResource> resources = new ArrayList<>();

        try {
            ListFunctionsRequest request = ListFunctionsRequest.builder().build();
            var response = lambdaClient.listFunctions(request);

            for (FunctionConfiguration function : response.functions()) {
                AwsResource resource = mapToAwsResource(function);
                resources.add(resource);
            }

            log.info("Lambda 함수 {} 개 조회 완료", resources.size());
        } catch (Exception e) {
            log.error("Lambda 함수 조회 실패", e);
            throw new RuntimeException("Lambda 함수 조회 실패", e);
        }

        return resources;
    }

    private AwsResource mapToAwsResource(FunctionConfiguration function) {
        Map<String, String> tags = new HashMap<>();

        try {
            ListTagsRequest tagsRequest = ListTagsRequest.builder()
                    .resource(function.functionArn())
                    .build();
            var tagsResponse = lambdaClient.listTags(tagsRequest);
            tags = new HashMap<>(tagsResponse.tags());
        } catch (Exception e) {
            log.debug("Lambda 함수 {} 태그 조회 실패", function.functionName());
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("runtime", function.runtimeAsString());
        metadata.put("handler", function.handler());
        metadata.put("codeSize", function.codeSize());
        metadata.put("memorySize", function.memorySize());
        metadata.put("timeout", function.timeout());
        metadata.put("lastModified", function.lastModified());
        metadata.put("description", function.description());

        return AwsResource.builder()
                .resourceId(function.functionArn())
                .resourceType("LAMBDA")
                .resourceName(function.functionName())
                .region(defaultRegion)
                .state(function.stateAsString() != null ? function.stateAsString() : "Active")
                .tags(tags)
                .metadata(metadata)
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }
}
