package com.nodemanager.cloud;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

public class AwsInstanceBuilder {
    private String amiId;
    private String instanceType;
    private int minCount;
    private int maxCount;
    private String keyName;
    private String securityGroup;
    private String subnetId;
    private boolean monitoringEnabled;

    public AwsInstanceBuilder() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AwsInstanceBuilder instanceBuilder;

        private Builder() {
            instanceBuilder = new AwsInstanceBuilder();
        }

        public Builder amiId(String amiId) {
            instanceBuilder.amiId = amiId;
            return this;
        }

        public Builder instanceType(String instanceType) {
            instanceBuilder.instanceType = instanceType;
            return this;
        }

        public Builder minCount(int minCount) {
            instanceBuilder.minCount = minCount;
            return this;
        }

        public Builder maxCount(int maxCount) {
            instanceBuilder.maxCount = maxCount;
            return this;
        }

        public Builder keyName(String keyName) {
            instanceBuilder.keyName = keyName;
            return this;
        }

        public Builder securityGroup(String securityGroup) {
            instanceBuilder.securityGroup = securityGroup;
            return this;
        }

        public Builder subnetId(String subnetId) {
            instanceBuilder.subnetId = subnetId;
            return this;
        }

        public Builder monitoringEnabled(boolean monitoringEnabled) {
            instanceBuilder.monitoringEnabled = monitoringEnabled;
            return this;
        }

        public AwsInstanceBuilder build() {
            return instanceBuilder;
        }
    }

    public void createInstanceTest() {

        System.out.println("Test creating instance ..");

    }

    public RunInstancesResponse createInstance() {
        Ec2Client ec2Client = Ec2Client.builder().region(Region.AP_SOUTH_1).build();

        RunInstancesRequest runInstancesRequest = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceType(instanceType)
                .minCount(minCount)
                .maxCount(maxCount)
                .keyName(keyName)
                .securityGroupIds(securityGroup)
                .subnetId(subnetId)
                .monitoring(RunInstancesMonitoringEnabled.builder().enabled(monitoringEnabled).build())
                .build();

        RunInstancesResponse response = ec2Client.runInstances(runInstancesRequest);
        System.out.println("EC2 Instance created with ID: " + response.instances().get(0).instanceId());
        return response;
    }


    public void cleanupInstanceTest() {

        System.out.println("Cleaning up instance ..");

    }
}