package com.nodemanager.controller;

import com.nodemanager.cloud.InstanceGroupBuilder;
import com.nodemanager.model.instancegroup.InstanceGroupCustomResource;
import com.nodemanager.model.instancegroup.InstanceGroupStatus;
import io.javaoperatorsdk.operator.api.reconciler.*;

import java.util.Optional;

@ControllerConfiguration
public class InstanceGroupController implements Reconciler<InstanceGroupCustomResource>, Cleaner<InstanceGroupCustomResource> {

    @Override
    public UpdateControl<InstanceGroupCustomResource> reconcile(InstanceGroupCustomResource resource, Context<InstanceGroupCustomResource> context) throws Exception {
        InstanceGroupStatus status = Optional.ofNullable(resource.getStatus()).orElse(new InstanceGroupStatus());
        InstanceGroupBuilder instanceGroupBuilder = new InstanceGroupBuilder(resource.getSpec().getRegion());

        // Handle Launch Template updates
        if (status.getLaunchTemplateName() == null || status.getLaunchTemplateName().isEmpty()) {
            instanceGroupBuilder.createLaunchTemplate(
                    resource.getSpec().getName(),
                    resource.getSpec().getAmiId(),
                    resource.getSpec().getInstanceType(),
                    resource.getSpec().getKeyName(),
                    resource.getSpec().getSecurityGroupId(),
                    resource.getSpec().getIamInstanceProfile(),
                    resource.getSpec().getUserDataBase64(),
                    resource.getSpec().getVolumeSize());

            status.setLaunchTemplateName(resource.getSpec().getName());
            resource.setStatus(status);
        } else {
            // Check for launch template updates
            instanceGroupBuilder.updateLaunchTemplate(
                    status.getLaunchTemplateName(),
                    resource.getSpec().getAmiId(),
                    resource.getSpec().getInstanceType(),
                    resource.getSpec().getKeyName(),
                    resource.getSpec().getSecurityGroupId(),
                    resource.getSpec().getIamInstanceProfile(),
                    resource.getSpec().getUserDataBase64(),
                    resource.getSpec().getVolumeSize());
        }

        // Handle Auto Scaling Group updates
        if (status.getAutoScalingGroupName() == null || status.getAutoScalingGroupName().isEmpty()) {
            instanceGroupBuilder.createAutoScalingGroup(
                    resource.getSpec().getName(),
                    resource.getSpec().getName(),
                    resource.getSpec().getVpcZoneIdentifier(),
                    resource.getSpec().getMinSize(),
                    resource.getSpec().getMaxSize(),
                    resource.getSpec().getDesiredCapacity(),
                    resource.getSpec().getMixedInstanceTypes(),
                    resource.getSpec().getTags());

            status.setAutoScalingGroupName(resource.getSpec().getName());
            resource.setStatus(status);
        } else {
            // Check for auto scaling group updates
            instanceGroupBuilder.updateAutoScalingGroup(
                    status.getAutoScalingGroupName(),
                    resource.getSpec().getVpcZoneIdentifier(),
                    resource.getSpec().getMinSize(),
                    resource.getSpec().getMaxSize(),
                    resource.getSpec().getDesiredCapacity(),
                    resource.getSpec().getMixedInstanceTypes(),
                    resource.getSpec().getTags());
        }

        status.setStatus("SUCCESS");
        resource.setStatus(status);

        return UpdateControl.updateStatus(resource);
    }

    @Override
    public DeleteControl cleanup(InstanceGroupCustomResource resource, Context<InstanceGroupCustomResource> context) {
        return DeleteControl.defaultDelete();
    }
}
