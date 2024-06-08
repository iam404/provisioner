package com.nodemanager.model.instancegroup;

import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;
import lombok.Data;

import java.util.List;

@Data
public class InstanceGroupStatus extends ObservedGenerationAwareStatus {

    @PrinterColumn(name = "LAUNCH_TEMPLATE")
    private String launchTemplateName;

    @PrinterColumn(name = "AUTOSCALING_GROUP")
    private String autoScalingGroupName;

    @PrinterColumn(name = "STATUS")
    private String status;

}
