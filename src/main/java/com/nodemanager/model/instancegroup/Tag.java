package com.nodemanager.model.instancegroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Tag {
    @JsonProperty("key")
    private String key;
    
    @JsonProperty("value")
    private String value;
    
    @JsonProperty("propagateAtLaunch")
    private boolean propagateAtLaunch = true;
}
