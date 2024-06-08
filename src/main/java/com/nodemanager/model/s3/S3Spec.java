package com.nodemanager.model.s3;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.crd.generator.annotation.PrinterColumn;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class S3Spec {

    @JsonProperty("name")
    @PrinterColumn(name = "BUCKET")
    private String name;

    @JsonProperty("region")
    @PrinterColumn(name = "REGION")
    private String region;


}


