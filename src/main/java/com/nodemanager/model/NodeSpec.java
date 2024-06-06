package com.nodemanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.crd.generator.annotation.PrinterColumn;
import io.fabric8.generator.annotation.Nullable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.lang.model.element.AnnotationMirror;

@Data
//@Setter
@Getter
public class NodeSpec {

        @JsonProperty("amiId")
        @PrinterColumn(name = "AMI_ID")
        private String amiId;
        @JsonProperty("keyName")
        @PrinterColumn(name = "KEY_NAME")
        private String keyName;
        @JsonProperty("securityGroup")
        private String securityGroup;
        @JsonProperty("instanceType")
        private String instanceType;
        @JsonProperty("userdata")
        private String userdata;

        @JsonProperty("volumeSize")
        private int volumeSize;

        @JsonProperty("minCount")
        private int minCount;
        @JsonProperty("maxCount")
        private int maxCount;

        @JsonProperty("vpcId")
        @PrinterColumn(name = "VPC")
        private String vpcId;
        @JsonProperty("subnetId")
        private String subnetId;
        @JsonProperty("lifecycle")
        private String lifecycle;

        @Nullable
        @JsonProperty("instanceProfile")
        private String instanceProfile;

        @JsonProperty("monitoringEnabled")
        private boolean monitoringEnabled;



}
