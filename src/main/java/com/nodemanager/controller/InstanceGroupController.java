package com.nodemanager.controller;

import com.nodemanager.cloud.InstanceGroupBuilder;
import com.nodemanager.model.instancegroup.InstanceGroupCustomResource;
import com.nodemanager.model.instancegroup.InstanceGroupStatus;
import io.javaoperatorsdk.operator.api.reconciler.*;

import java.util.Optional;
import java.util.Collections;
import io.fabric8.kubernetes.api.model.ObjectMeta;


@ControllerConfiguration
public class InstanceGroupController implements Reconciler<InstanceGroupCustomResource>, Cleaner<InstanceGroupCustomResource> {

    private static final String FINALIZER = "instancegroup.finalizers.provisioner.app";

    @Override
    public UpdateControl<InstanceGroupCustomResource> reconcile(InstanceGroupCustomResource resource, Context<InstanceGroupCustomResource> context) throws Exception {
        ObjectMeta metadata = resource.getMetadata();
        InstanceGroupStatus status = Optional.ofNullable(resource.getStatus()).orElse(new InstanceGroupStatus());
        long gen = metadata.getGeneration();
        InstanceGroupBuilder instanceGroupBuilder = new InstanceGroupBuilder(resource.getSpec().getRegion());

        // Add finalizer if not present
        if (metadata.getFinalizers() == null || !metadata.getFinalizers().contains(FINALIZER)) {
            metadata.setFinalizers(Collections.singletonList(FINALIZER));
            resource.setMetadata(metadata);
            return UpdateControl.updateResource(resource);
        }

        // 2) Track generation & skip if already up-to-date
        if (status.getObservedGeneration() != null && status.getObservedGeneration() == gen) {
            return UpdateControl.noUpdate();
        }
        status.setObservedGeneration(gen);

        // Handle deletion
        if (metadata.getDeletionTimestamp() != null) {
            status.setStatus("DELETING");
            resource.setStatus(status);
            // Actual deletion is handled in cleanup, but we update status here
            return UpdateControl.updateStatus(resource);
        }

        // Normal reconciliation
        // Handle Launch Template and Auto Scaling Group creation with epoch
        if (status.getLaunchTemplateName() == null || status.getLaunchTemplateName().isEmpty()) {
            String baseName = resource.getSpec().getName();
            String epochName = baseName + "-" + System.currentTimeMillis();
            instanceGroupBuilder.createLaunchTemplate(
                    epochName,
                    resource.getSpec().getAmiId(),
                    resource.getSpec().getInstanceType(),
                    resource.getSpec().getKeyName(),
                    resource.getSpec().getSecurityGroupId(),
                    resource.getSpec().getIamInstanceProfile(),
                    resource.getSpec().getUserDataBase64(),
                    resource.getSpec().getVolumeSize());

            status.setLaunchTemplateName(epochName);
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
            System.out.println("Auto Scaling Group does not exist. Creating...");
            // Use the same epochName as launch template
            String epochName = status.getLaunchTemplateName();
            instanceGroupBuilder.createAutoScalingGroup(
                    epochName,
                    epochName,
                    resource.getSpec().getVpcZoneIdentifier(),
                    resource.getSpec().getMinSize(),
                    resource.getSpec().getMaxSize(),
                    resource.getSpec().getDesiredCapacity(),
                    resource.getSpec().getMixedInstanceTypes(),
                    resource.getSpec().getTags(),
                    resource.getSpec().getInstancesDistribution());

            status.setAutoScalingGroupName(epochName);
            resource.setStatus(status);
        } else {
            System.out.println("Auto Scaling Group already exists: " + status.getAutoScalingGroupName());
            // Check for auto scaling group updates
            instanceGroupBuilder.updateAutoScalingGroup(
                    status.getAutoScalingGroupName(),
                    resource.getSpec().getVpcZoneIdentifier(),
                    resource.getSpec().getMinSize(),
                    resource.getSpec().getMaxSize(),
                    resource.getSpec().getDesiredCapacity(),
                    resource.getSpec().getMixedInstanceTypes(),
                    resource.getSpec().getTags(),
                    resource.getSpec().getInstancesDistribution());
        }

        status.setStatus("SUCCESS");
        resource.setStatus(status);

        return UpdateControl.updateStatus(resource);
    }

    @Override
    public DeleteControl cleanup(InstanceGroupCustomResource resource, Context<InstanceGroupCustomResource> context) {
        InstanceGroupStatus status = Optional.ofNullable(resource.getStatus()).orElse(new InstanceGroupStatus());
        InstanceGroupBuilder instanceGroupBuilder = new InstanceGroupBuilder(resource.getSpec().getRegion());
        boolean deleted = true;

        // Deletion protection
        if (resource.getSpec().isPreventDeletion()) {
            System.out.println("Deletion Protection is Enabled. Not Deleting the Resource.");
        } else {
            // Delete Auto Scaling Group if exists
            if (status.getAutoScalingGroupName() != null && !status.getAutoScalingGroupName().isEmpty()) {
                instanceGroupBuilder.deleteAutoScalingGroup(status.getAutoScalingGroupName());
            }
            // Delete Launch Template if exists
            if (status.getLaunchTemplateName() != null && !status.getLaunchTemplateName().isEmpty()) {
                instanceGroupBuilder.deleteLaunchTemplate(status.getLaunchTemplateName());
            }
        }

        // Remove finalizer only after resources are deleted or protected
        ObjectMeta metadata = resource.getMetadata();
        if (metadata.getFinalizers() != null && metadata.getFinalizers().contains(FINALIZER)) {
            metadata.getFinalizers().remove(FINALIZER);
            resource.setMetadata(metadata);
        }
        return DeleteControl.defaultDelete();
    }
}
