package com.nodemanager.model;

import java.util.ArrayList;
import java.util.List;


public class NodeStatus {
    private List<String> reconciledBy;
    private List<String> instanceIds;


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
}
