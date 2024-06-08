package com.nodemanager.cloud;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3Builder {

    private final S3Client s3Client;

    public S3Builder(String region) {
         this.s3Client = S3Client.builder().region(Region.of(region)).build();
    }

    public void  createBucket(String bucketName) {

        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();
        CreateBucketResponse response = s3Client.createBucket(bucketRequest);

    }

    public void deleteBucket(String bucketName) {
        s3Client.deleteBucket(b -> b.bucket(bucketName));
    }




}
