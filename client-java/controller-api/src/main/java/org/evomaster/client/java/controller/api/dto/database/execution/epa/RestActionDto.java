package org.evomaster.client.java.controller.api.dto.database.execution.epa;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.models.HttpMethod;


public class RestActionDto {
    @JsonProperty("verb")
    public String verb;

    @JsonProperty("path")
    public String path;

    public RestActionDto(@JsonProperty("verb") String verb, @JsonProperty("path")String path) {
        this.verb = verb;
        this.path = path;
    }
}
