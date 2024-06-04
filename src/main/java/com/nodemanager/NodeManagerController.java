package com.nodemanager;

import com.nodemanager.cloud.AwsInstanceBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nodemanager.model.*;

@ControllerConfiguration
public class NodeManagerController implements Reconciler<NodeManager>, Cleaner<NodeManager> {


    private AwsInstanceBuilder awsInstanceBuilder;
    private KubernetesClient client;
    private String identity;


    static final Logger log = LoggerFactory.getLogger(NodeManagerController.class);



    public NodeManagerController(KubernetesClient client,String identity, AwsInstanceBuilder awsInstanceBuilder) {
        this.client = client;
        this.identity = identity;
        this.awsInstanceBuilder = awsInstanceBuilder;
    }

    @Override
    public UpdateControl<NodeManager> reconcile(NodeManager resource, Context context) {

        if (resource.getStatus() == null) {
            resource.setStatus(new NodeStatus());
        }

        resource.getStatus().getReconciledBy().add(identity);
        log.info("reconciled by {} {}", resource.getStatus().getReconciledBy(), identity);

        awsInstanceBuilder.createInstanceTest();


        return UpdateControl.updateStatus(resource);
    }

    @Override
    public DeleteControl cleanup(NodeManager nodeManager, Context<NodeManager> context) {

        awsInstanceBuilder.cleanupInstanceTest();
        return null;
    }
}
