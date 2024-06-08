package com.nodemanager.controller;

import com.nodemanager.cloud.S3Builder;
import com.nodemanager.model.s3.S3CustomResource;
import io.javaoperatorsdk.operator.api.reconciler.*;

@ControllerConfiguration
public class S3ProvisionerController implements Reconciler<S3CustomResource>, Cleaner<S3CustomResource> {

    @Override
    public UpdateControl<S3CustomResource> reconcile(S3CustomResource resource, Context<S3CustomResource> context) throws Exception {

        S3Builder s3Builder = new S3Builder(resource.getSpec().getRegion());

        s3Builder.createBucket(resource.getSpec().getName());

        return UpdateControl.updateStatus(resource);
    }


    @Override
    public DeleteControl cleanup(S3CustomResource s3CustomResource, Context<S3CustomResource> context) {
        return DeleteControl.defaultDelete();
    }

}
