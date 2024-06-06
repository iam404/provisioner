package com.nodemanager;

import com.nodemanager.cloud.AwsInstanceBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nodemanager.model.*;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ControllerConfiguration
public class NodeManagerController implements Reconciler<NodeManager>, Cleaner<NodeManager> {


    private AwsInstanceBuilder awsInstanceBuilder;
    private KubernetesClient client;
    private String identity;

    private List<String> instanceIds;


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

       log.info(resource.getSpec().toString());

        resource.getStatus().getReconciledBy().add(identity);
        log.info("reconciled by {} {}", resource.getStatus().getReconciledBy(), identity);

        // Check if instance IDs already exist in the status
        if (resource.getStatus().getInstanceIds() == null || resource.getStatus().getInstanceIds().isEmpty()) {


            try {

                // Create new instances if no instance IDs are found
                RunInstancesResponse resp = awsInstanceBuilder.createInstance(resource.getSpec());

                if (this.instanceIds == null) {
                    this.instanceIds = new ArrayList<>();
                }

                for (Iterator<Instance> it = resp.instances().iterator(); it.hasNext(); ) {
                    Instance instance = it.next();

                    this.instanceIds.add(instance.instanceId());
                }

                resource.getStatus().setInstanceIds(instanceIds);
                resource.getStatus().setStatus("SUCCESS");
            } catch(Exception e) {
                log.error(e.getMessage());
                resource.getStatus().setStatus("FAILED");
            }
        }



        return UpdateControl.updateStatus(resource);
    }

    @Override
    public DeleteControl cleanup(NodeManager nodeManager, Context<NodeManager> context) {

        awsInstanceBuilder.cleanupInstance(nodeManager.getStatus().getInstanceIds());
        return DeleteControl.defaultDelete();
    }
}
