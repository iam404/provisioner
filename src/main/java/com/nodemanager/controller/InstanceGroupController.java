package com.nodemanager.controller;

import com.nodemanager.cloud.InstanceGroupBuilder;
import com.nodemanager.model.instancegroup.InstanceGroupCustomResource;
import io.javaoperatorsdk.operator.api.reconciler.*;

@ControllerConfiguration
public class InstanceGroupController implements Reconciler<InstanceGroupCustomResource>, Cleaner<InstanceGroupCustomResource> {

    @Override
    public UpdateControl<InstanceGroupCustomResource> reconcile(InstanceGroupCustomResource resource, Context<InstanceGroupCustomResource> context) throws Exception {

        InstanceGroupBuilder instanceGroupBuilder = new InstanceGroupBuilder(resource.getSpec().getRegion());

        instanceGroupBuilder.createLaunchTemplate(
                resource.getSpec().getName(),
                resource.getSpec().getAmiId(),
                resource.getSpec().getInstanceType(),
                resource.getSpec().getKeyName(),
                resource.getSpec().getSecurityGroupId(),
                resource.getSpec().getIamInstanceProfile(),
                resource.getSpec().getUserData(), resource.getSpec().getVolumeSize());

        instanceGroupBuilder.createAutoScalingGroup(
                resource.getSpec().getName(),
                resource.getSpec().getName(),
                resource.getSpec().getVpcZoneIdentifier(),
                resource.getSpec().getMinSize(),
                resource.getSpec().getMaxSize(),
                resource.getSpec().getDesiredCapacity(),
                resource.getSpec().getMixedInstanceTypes()
        );

        return null;
    }

    @Override
    public DeleteControl cleanup(InstanceGroupCustomResource resource, Context<InstanceGroupCustomResource> context) {
        return null;
    }
}
