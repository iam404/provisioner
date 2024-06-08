package com.nodemanager.model;

import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;


public class NodeStatus extends ObservedGenerationAwareStatus {
    private List<String> reconciledBy;

    //@PrinterColumn(name = "INSTANCE_IDS")
    private List<String> instanceIds;

    @PrinterColumn(name = "STATUS")
    private String status;

    @PrinterColumn(name = "INSTANCE_IDS")
    private transient String instanceIdsAsString;


    public List<String> getReconciledBy() {
        if (reconciledBy == null) {
            reconciledBy = new ArrayList<>();
        }
        return reconciledBy;
    }

    public NodeStatus setReconciledBy(List<String> reconciledBy) {
        this.reconciledBy = reconciledBy;
        return this;
    }

    // Add Status information here

    public List<String> getInstanceIds() {
        if (instanceIds == null) {
            instanceIds = new ArrayList<>();
        }
        return instanceIds;
    }

    public NodeStatus setInstanceIds(List<String> resp) {
        this.instanceIds = resp;

        return this;
    }



    public String getStatus() {
        if (status == null) {
            status = new String();
        }
        return status;
    }

    public NodeStatus setStatus(String status) {


            this.status = status;



        return this;
    }

    public NodeStatus updateInstanceIdsAsString() {
        if (instanceIds == null || instanceIds.isEmpty()) {
            this.instanceIdsAsString = "";
        } else {
            StringJoiner joiner = new StringJoiner(", ");
            for (String instanceId : instanceIds) {
                joiner.add(instanceId);
            }
            this.instanceIdsAsString = joiner.toString();
        }

        return this;
    }
}
