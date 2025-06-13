package com.nodemanager.model.instancegroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.crd.generator.annotation.PrinterColumn;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.Map;

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
    private String userDataBase64;
    private String keyName;
    private String securityGroupId;
    private String iamInstanceProfile;
    private List<Tag> tags;
    
    @JsonProperty("instancesDistribution")
    private InstancesDistribution instancesDistribution;

    /**
     * If true, prevents deletion of AWS resources when the InstanceGroup CR is deleted.
     */
    @JsonProperty("preventDeletion")
    private boolean preventDeletion = false;

    public String getUserDataBase64() {
        if (userDataBase64 != null && !userDataBase64.isEmpty()) {
            return userDataBase64;
        }
        if (userData != null && !userData.isEmpty()) {
            return java.util.Base64.getEncoder().encodeToString(userData.getBytes());
        }
        return null;
    }
}
