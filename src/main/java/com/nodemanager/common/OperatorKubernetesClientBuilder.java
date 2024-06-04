package com.nodemanager.common;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public class OperatorKubernetesClientBuilder {

    private OperatorKubernetesClientBuilder() {}

    public static KubernetesClient build() {
        KubernetesClient client = new KubernetesClientBuilder().build();
        return client;
    }


}
