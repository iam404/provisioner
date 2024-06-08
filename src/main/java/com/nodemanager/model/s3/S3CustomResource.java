package com.nodemanager.model.s3;


import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Version;


@Version("v1alpha1")
@Group("aws.provisioner.app")
@Kind("S3Bucket")
public class S3CustomResource extends CustomResource<S3Spec, S3Status> implements Namespaced {


}