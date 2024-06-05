package com.nodemanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.lang.model.element.AnnotationMirror;

@Data
//@Setter
@Getter
public class NodeSpec {

        @JsonProperty("amiId")
        private String amiId;
        @JsonProperty("keyName")
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
        private String vpcId;
        @JsonProperty("subnetId")
        private String subnetId;
        @JsonProperty("lifecycle")
        private String lifecycle;

        @JsonProperty("monitoringEnabled")
        private boolean monitoringEnabled;



}
