package com.nodemanager.model.instancegroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.crd.generator.annotation.PrinterColumn;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class InstanceGroupSpec {

    @JsonProperty("name")
    @PrinterColumn(name = "NAME")
    private String name;

    private String region;
    private  String amiId;
    private String vpcZoneIdentifier;
    private int minSize;
    private int maxSize;
    private int desiredCapacity;
    private String instanceType;
    private int volumeSize;
    private boolean mixedInstancesPolicy;
    private List<String> mixedInstanceTypes;
    private String userData;
    private String keyName;
    private String securityGroupId;
    private String iamInstanceProfile;

}
