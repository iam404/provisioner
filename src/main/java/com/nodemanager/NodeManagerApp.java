package com.nodemanager;

import com.nodemanager.cloud.AwsInstanceBuilder;
import com.nodemanager.controller.InstanceGroupController;
import com.nodemanager.controller.NodeManagerController;
import com.nodemanager.controller.S3ProvisionerController;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.Operator;
import io.javaoperatorsdk.operator.api.config.LeaderElectionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.nodemanager.common.OperatorKubernetesClientBuilder;


@SpringBootApplication
public class NodeManagerApp {



    static final Logger log = LoggerFactory.getLogger(NodeManagerApp.class);

    public static void main(String[] args) {

        String identity = System.getenv("POD_NAME");
        String namespace = System.getenv("POD_NAMESPACE");
        log.info("Starting operator with identity: {}", identity);

        KubernetesClient client = OperatorKubernetesClientBuilder.build();
        Operator operator = new Operator(client,
                c -> c.withLeaderElectionConfiguration(
                        new LeaderElectionConfiguration("leader-election", namespace, identity)));

        AwsInstanceBuilder awsInstanceBuilder = new AwsInstanceBuilder();

        operator.register(new NodeManagerController(client, identity, awsInstanceBuilder));
        operator.register(new S3ProvisionerController());
        operator.register(new InstanceGroupController());

        SpringApplication.run(NodeManagerApp.class, args);
        operator.start();
    }

}