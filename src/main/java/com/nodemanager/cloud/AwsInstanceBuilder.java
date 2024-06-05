package com.nodemanager.cloud;

import com.nodemanager.model.NodeSpec;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;

public class AwsInstanceBuilder {

    private final Ec2Client ec2Client;


    public AwsInstanceBuilder() {
        this.ec2Client = Ec2Client.builder().region(Region.AP_SOUTH_1).build();
    }


    public RunInstancesResponse createInstance(NodeSpec nodeSpec) {

        BlockDeviceMapping blockDeviceMapping = BlockDeviceMapping.builder()
                .deviceName("/dev/xvda")
                .ebs(EbsBlockDevice.builder().volumeSize(nodeSpec.getVolumeSize()).build())
                .build();

        RunInstancesRequest runInstancesRequest = RunInstancesRequest.builder()
                .imageId(nodeSpec.getAmiId())
                .instanceType(nodeSpec.getInstanceType())
                .minCount(nodeSpec.getMinCount())
                .maxCount(nodeSpec.getMaxCount())
                .keyName(nodeSpec.getKeyName())
                .securityGroupIds(nodeSpec.getSecurityGroup())
                .subnetId(nodeSpec.getSubnetId())
                .blockDeviceMappings(blockDeviceMapping)
               // .userData(nodeSpec.getUserdata())
                .monitoring(RunInstancesMonitoringEnabled.builder().enabled(nodeSpec.isMonitoringEnabled()).build())
               // .dryRun(Boolean.TRUE) // <-- Dry RUN
                .build();

        RunInstancesResponse response = ec2Client.runInstances(runInstancesRequest);
        System.out.println("EC2 Instance created with ID: " + response.instances().get(0).instanceId());
        return response;
    }


    public void cleanupInstance(List<String> instanceIds) {

        for (String instanceId : instanceIds) {
            System.out.println("Cleaning up instance .." + instanceId );
            TerminateInstancesRequest terminateRequest = TerminateInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build();

            TerminateInstancesResponse terminateResponse = ec2Client.terminateInstances(terminateRequest);
            for (InstanceStateChange change : terminateResponse.terminatingInstances()) {
                System.out.printf("Instance %s is %s%n",
                        change.instanceId(), change.currentState().name());
            }
        }



    }
}