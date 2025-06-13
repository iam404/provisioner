package com.nodemanager.model.instancegroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InstancesDistribution {
    @JsonProperty("onDemandAllocationStrategy")
    private String onDemandAllocationStrategy;
    
    @JsonProperty("onDemandBaseCapacity")
    private Integer onDemandBaseCapacity;
    
    @JsonProperty("onDemandPercentageAboveBaseCapacity")
    private Integer onDemandPercentageAboveBaseCapacity;
    
    @JsonProperty("spotAllocationStrategy")
    private String spotAllocationStrategy;
    
    @JsonProperty("spotInstancePools")
    private Integer spotInstancePools;
}
