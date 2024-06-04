package com.nodemanager.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.lang.model.element.AnnotationMirror;

@Data
public class NodeSpec {

        @JsonProperty("ami")
        private String ami;
        @JsonProperty("key")
        private String key;
        @JsonProperty("securityGroup")
        private String securityGroup;
        @JsonProperty("instanceType")
        private String instanceType;
        @JsonProperty("userdata")
        private String userdata;
        @JsonProperty("vpcId")
        private String vpcId;
        @JsonProperty("subnet")
        private String subnet;
        @JsonProperty("lifecycle")
        private String lifecycle;



}
