package com.nodemanager.cloud;

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

    /**
     * Updates an existing launch template with the specified parameters.
     * 
     * @param launchTemplateName The name of the launch template.
     * @param instanceType       The type of instance.
     * @param keyName            The name of the key pair.
     * @param securityGroup      The ID of the security group.
     * @param instanceProfile    The ARN of the instance profile.
     * @param userData           The user data for the instance. This should be base64 encoded.
     * @param volumeSize         The size of the volume.
     */
    public void updateLaunchTemplate(String launchTemplateName, String amiId, String instanceType, String keyName, String securityGroup, String instanceProfile, String userData, int volumeSize) {
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

        CreateLaunchTemplateVersionRequest request = CreateLaunchTemplateVersionRequest.builder()
                .launchTemplateName(launchTemplateName)
                .versionDescription("Updated version")
                .launchTemplateData(launchTemplateData)
                .build();

        CreateLaunchTemplateVersionResponse response = ec2Client.createLaunchTemplateVersion(request);
        if (response.launchTemplateVersion() != null) {
            System.out.println("Launch Template updated: " + launchTemplateName + ", version: " + response.launchTemplateVersion().versionNumber());
        } else {
            System.out.println("Launch Template update initiated for: " + launchTemplateName);
        }
    }

    /**
     * Updates an existing Auto Scaling Group with the specified parameters.
     * 
     * @param autoScalingGroupName The name of the Auto Scaling Group.
     * @param vpcZoneIdentifier    The VPC zone identifier.
     * @param minSize              Minimum size of the group.
     * @param maxSize              Maximum size of the group.
     * @param desiredCapacity      Desired capacity of the group.
     * @param mixedInstanceTypes   List of instance types for mixed instances policy.
     * @param tags                 List of tags to apply.
     */
    public void updateAutoScalingGroup(String autoScalingGroupName, String vpcZoneIdentifier, 
                                     int minSize, int maxSize, int desiredCapacity, List<String> mixedInstanceTypes, 
                                     List<com.nodemanager.model.instancegroup.Tag> tags,
                                     com.nodemanager.model.instancegroup.InstancesDistribution instancesDistributionConfig) {
        
        // Launch Template
        LaunchTemplateSpecification launchTemplateSpecification = LaunchTemplateSpecification.builder()
                .launchTemplateName(autoScalingGroupName)
                .version("$Latest")
                .build();

        software.amazon.awssdk.services.autoscaling.model.InstancesDistribution.Builder instancesDistributionBuilder = 
            software.amazon.awssdk.services.autoscaling.model.InstancesDistribution.builder()
                .onDemandAllocationStrategy("prioritized")
                .onDemandBaseCapacity(100)
                .onDemandPercentageAboveBaseCapacity(0)
                .spotAllocationStrategy("lowest-price");

        // Apply custom instancesDistribution configuration if provided
        if (instancesDistributionConfig != null) {
                if (instancesDistributionConfig.getOnDemandAllocationStrategy() != null) {
                    instancesDistributionBuilder.onDemandAllocationStrategy(instancesDistributionConfig.getOnDemandAllocationStrategy());
                }
                if (instancesDistributionConfig.getOnDemandBaseCapacity() != null) {
                    instancesDistributionBuilder.onDemandBaseCapacity(instancesDistributionConfig.getOnDemandBaseCapacity());
                }
                if (instancesDistributionConfig.getOnDemandPercentageAboveBaseCapacity() != null) {
                    instancesDistributionBuilder.onDemandPercentageAboveBaseCapacity(instancesDistributionConfig.getOnDemandPercentageAboveBaseCapacity());
                }
                if (instancesDistributionConfig.getSpotAllocationStrategy() != null) {
                    instancesDistributionBuilder.spotAllocationStrategy(instancesDistributionConfig.getSpotAllocationStrategy());
                }
                if (instancesDistributionConfig.getSpotInstancePools() != null) {
                    instancesDistributionBuilder.spotInstancePools(instancesDistributionConfig.getSpotInstancePools());
                }
            }
    
        software.amazon.awssdk.services.autoscaling.model.InstancesDistribution instancesDistribution = instancesDistributionBuilder.build();

        List<LaunchTemplateOverrides> overrides = mixedInstanceTypes.stream()
                .map(instanceType -> LaunchTemplateOverrides.builder()
                        .instanceType(instanceType)
                        .weightedCapacity(1)
                        .build())
                .collect(Collectors.toList());

        LaunchTemplate launchTemplate = LaunchTemplate.builder()
                .launchTemplateSpecification(launchTemplateSpecification)
                .overrides(overrides)
                .build();

        MixedInstancesPolicy mixedInstancesPolicy = MixedInstancesPolicy.builder()
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

        // Build update request
        UpdateAutoScalingGroupRequest request = UpdateAutoScalingGroupRequest.builder()
                .autoScalingGroupName(autoScalingGroupName)
                .vpcZoneIdentifier(vpcZoneIdentifier)
                .minSize(minSize)
                .maxSize(maxSize)
                .desiredCapacity(desiredCapacity)
                .mixedInstancesPolicy(mixedInstancesPolicy)
                .build();

        // Update Auto Scaling Group
        UpdateAutoScalingGroupResponse updateResponse = autoScalingClient.updateAutoScalingGroup(request);
        if (updateResponse.sdkHttpResponse() != null && updateResponse.sdkHttpResponse().isSuccessful()) {
            System.out.println("Successfully updated Auto Scaling Group: " + autoScalingGroupName);
        }
        System.out.println("Auto Scaling Group updated: " + autoScalingGroupName);

        // Update tags if they exist
        if (awsTags != null && !awsTags.isEmpty()) {
            CreateOrUpdateTagsRequest tagRequest = CreateOrUpdateTagsRequest.builder()
                .tags(awsTags)
                .build();
            
            autoScalingClient.createOrUpdateTags(tagRequest);
        }
    }


    // to add later List<Tag> tags,
    /**
     * Creates a launch template with the specified parameters.
     * 
     * @param launchTemplateName The name of the launch template.
     * @param amiId              The ID of the Amazon Machine Image (AMI).
     * @param instanceType       The type of instance.
     * @param keyName            The name of the key pair.
     * @param securityGroup      The ID of the security group.
     * @param instanceProfile    The ARN of the instance profile.
     * @param userData           The user data for the instance. This should be base64 encoded.
     * @param volumeSize         The size of the volume.
     */
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
                .userData(userData)  // This should be base64 encoded
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
                                     List<com.nodemanager.model.instancegroup.Tag> tags,
                                     com.nodemanager.model.instancegroup.InstancesDistribution instancesDistributionConfig) {

        // Launch Template
        LaunchTemplateSpecification launchTemplateSpecification = LaunchTemplateSpecification.builder()
                .launchTemplateName(launchConfigurationName)
                .version("$Latest")
                .build();

        // Create LaunchTemplateOverrides for mixed instances
        List<LaunchTemplateOverrides> overrides = mixedInstanceTypes.stream()
                .map(instanceType -> LaunchTemplateOverrides.builder()
                        .instanceType(instanceType)
                        .build())
                .collect(Collectors.toList());

        // Create LaunchTemplate
        LaunchTemplate launchTemplate = LaunchTemplate.builder()
                .launchTemplateSpecification(launchTemplateSpecification)
                .overrides(overrides)
                .build();

        // Configure InstancesDistribution with defaults that can be overridden
        software.amazon.awssdk.services.autoscaling.model.InstancesDistribution.Builder instancesDistributionBuilder = 
            software.amazon.awssdk.services.autoscaling.model.InstancesDistribution.builder()
                .onDemandAllocationStrategy("prioritized")
                .onDemandBaseCapacity(100)
                .onDemandPercentageAboveBaseCapacity(0)
                .spotAllocationStrategy("lowest-price");

        // Apply custom instancesDistribution configuration if provided
        if (instancesDistributionConfig != null) {
            if (instancesDistributionConfig.getOnDemandAllocationStrategy() != null) {
                instancesDistributionBuilder.onDemandAllocationStrategy(instancesDistributionConfig.getOnDemandAllocationStrategy());
            }
            if (instancesDistributionConfig.getOnDemandBaseCapacity() != null) {
                instancesDistributionBuilder.onDemandBaseCapacity(instancesDistributionConfig.getOnDemandBaseCapacity());
            }
            if (instancesDistributionConfig.getOnDemandPercentageAboveBaseCapacity() != null) {
                instancesDistributionBuilder.onDemandPercentageAboveBaseCapacity(instancesDistributionConfig.getOnDemandPercentageAboveBaseCapacity());
            }
            if (instancesDistributionConfig.getSpotAllocationStrategy() != null) {
                instancesDistributionBuilder.spotAllocationStrategy(instancesDistributionConfig.getSpotAllocationStrategy());
            }
            if (instancesDistributionConfig.getSpotInstancePools() != null) {
                instancesDistributionBuilder.spotInstancePools(instancesDistributionConfig.getSpotInstancePools());
            }
        }

        software.amazon.awssdk.services.autoscaling.model.InstancesDistribution instancesDistribution = instancesDistributionBuilder.build();

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

    /**
     * Deletes an Auto Scaling Group by name.
     */
    public void deleteAutoScalingGroup(String autoScalingGroupName) {
        try {
            DeleteAutoScalingGroupRequest request = DeleteAutoScalingGroupRequest.builder()
                    .autoScalingGroupName(autoScalingGroupName)
                    .forceDelete(true)
                    .build();
            autoScalingClient.deleteAutoScalingGroup(request);
            System.out.println("Auto Scaling Group deleted: " + autoScalingGroupName);
        } catch (Exception e) {
            System.err.println("Failed to delete Auto Scaling Group: " + e.getMessage());
        }
    }

    /**
     * Deletes a Launch Template by name.
     */
    public void deleteLaunchTemplate(String launchTemplateName) {
        try {
            DeleteLaunchTemplateRequest request = DeleteLaunchTemplateRequest.builder()
                    .launchTemplateName(launchTemplateName)
                    .build();
            ec2Client.deleteLaunchTemplate(request);
            System.out.println("Launch Template deleted: " + launchTemplateName);
        } catch (Exception e) {
            System.err.println("Failed to delete Launch Template: " + e.getMessage());
        }
    }
}

