package com.nodemanager.model;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;


@Version("v1alpha1")
@Group("nodemanager.app")
@Kind("AwsResourceProvisioner")
public class NodeManager extends CustomResource<NodeSpec, NodeStatus> implements Namespaced {

}
