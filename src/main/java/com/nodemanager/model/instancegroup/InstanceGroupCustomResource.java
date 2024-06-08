package com.nodemanager.model.instancegroup;

import com.nodemanager.model.s3.S3Spec;
import com.nodemanager.model.s3.S3Status;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("aws.provisioner.app")
@Kind("InstanceGroup")
public class InstanceGroupCustomResource extends CustomResource<InstanceGroupSpec, InstanceGroupStatus> implements Namespaced {

}
