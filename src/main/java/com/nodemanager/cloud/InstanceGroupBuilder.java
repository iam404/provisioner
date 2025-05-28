package com.nodemanager.cloud;

import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.*;

import software.amazon.awssdk.services.autoscaling.model.LaunchTemplate;
import software.amazon.awssdk.services.autoscaling.model.LaunchTemplateOverrides;
import software.amazon.awssdk.services.autoscaling.model.LaunchTemplateSpecification;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class InstanceGroupBuilder {

    private final AutoScalingClient autoScalingClient;
    private final Ec2Client ec2Client;

    public InstanceGroupBuilder(String region) {
        this.autoScalingClient = AutoScalingClient.builder().region(Region.of(region)).build();
        this.ec2Client = Ec2Client.builder().region(Region.of(region)).build();
    }


    // to add later List<Tag> tags,
    public void createLaunchTemplate(String launchTemplateName, String amiId, String instanceType, String keyName, String securityGroup, String instanceProfile, String userData, int volumeSize) {

        LaunchTemplateBlockDeviceMappingRequest blockDeviceMapping = LaunchTemplateBlockDeviceMappingRequest.builder()
                .deviceName("/dev/xvda")
                .ebs(LaunchTemplateEbsBlockDeviceRequest.builder().volumeSize(volumeSize).build())
                .build();

        RequestLaunchTemplateData launchTemplateData = RequestLaunchTemplateData.builder()
                .imageId(amiId)
                .instanceType(instanceType)
                .keyName(keyName)
                .securityGroupIds(securityGroup)
                .blockDeviceMappings(blockDeviceMapping)
                .iamInstanceProfile(LaunchTemplateIamInstanceProfileSpecificationRequest.builder().arn(instanceProfile).build())
                .userData(userData)
                .build();

        CreateLaunchTemplateRequest request = CreateLaunchTemplateRequest.builder()
                .launchTemplateName(launchTemplateName)
                .versionDescription("Version 1")
                .launchTemplateData(launchTemplateData)
                .build();

        CreateLaunchTemplateResponse response = ec2Client.createLaunchTemplate(request);
        System.out.println("Launch Template created: " + launchTemplateName);
    }

    public void createAutoScalingGroup(String autoScalingGroupName, String launchConfigurationName, String vpcZoneIdentifier, 
                                     int minSize, int maxSize, int desiredCapacity, List<String> mixedInstanceTypes, 
                                     List<com.nodemanager.model.instancegroup.Tag> tags) {

        // Launch Template
        LaunchTemplateSpecification launchTemplateSpecification = LaunchTemplateSpecification.builder()
                .launchTemplateName(launchConfigurationName)
                .version("$Latest")
                .build();

        InstancesDistribution instancesDistribution = InstancesDistribution.builder()
                .onDemandPercentageAboveBaseCapacity(0) // 100% Spot instances
                .spotAllocationStrategy("lowest-price")
                .build();

        List<LaunchTemplateOverrides> overrides = mixedInstanceTypes.stream()
                .map(instanceType -> LaunchTemplateOverrides.builder()
                        .instanceType(instanceType)
                        .build())
                .collect(Collectors.toList());

        LaunchTemplate launchTemplate = LaunchTemplate.builder()
                .launchTemplateSpecification(launchTemplateSpecification)
                .overrides(overrides)
                .build();

        MixedInstancesPolicy mixedInstancesPolicy = MixedInstancesPolicy.builder()
                //.launchTemplate(launchTemplateSpecification)
                .launchTemplate(launchTemplate)
                .instancesDistribution(instancesDistribution)
                .build();

        // Convert custom tags to AWS Tag type
        List<software.amazon.awssdk.services.autoscaling.model.Tag> awsTags = null;
        if (tags != null && !tags.isEmpty()) {
            awsTags = tags.stream()
                .map(tag -> software.amazon.awssdk.services.autoscaling.model.Tag.builder()
                    .key(tag.getKey())
                    .value(tag.getValue())
                    .propagateAtLaunch(tag.isPropagateAtLaunch())
                    .resourceId(autoScalingGroupName)
                    .resourceType("auto-scaling-group")
                    .build())
                .collect(Collectors.toList());
        }

        // Build ASG request
        CreateAutoScalingGroupRequest.Builder requestBuilder = CreateAutoScalingGroupRequest.builder()
                .autoScalingGroupName(autoScalingGroupName)
                .vpcZoneIdentifier(vpcZoneIdentifier)
                .minSize(minSize)
                .maxSize(maxSize)
                .desiredCapacity(desiredCapacity)
                .mixedInstancesPolicy(mixedInstancesPolicy);
                //.availabilityZones(availabilityZones)
                //.serviceLinkedRoleARN()
                //.placementGroup()

        
        // Add tags if they exist
        if (awsTags != null && !awsTags.isEmpty()) {
            requestBuilder.tags(awsTags);
        }

        CreateAutoScalingGroupRequest request = requestBuilder.build();

        CreateAutoScalingGroupResponse response = autoScalingClient.createAutoScalingGroup(request);
        System.out.println("Auto Scaling Group created: " + autoScalingGroupName);
    }
}
