package com.github.config.helper.component.http.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CreateNamespaceResponse
 *
 * @author lupeng10
 * @create 2023-07-03 14:57
 */
@NoArgsConstructor
@Data
public class CreateNamespaceResponse {
    @JsonProperty("code")
    private Integer code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("createdBy")
        private String createdBy;
        @JsonProperty("createTimestamp")
        private Object createTimestamp;
        @JsonProperty("updatedBy")
        private Object updatedBy;
        @JsonProperty("updateTimestamp")
        private Object updateTimestamp;
        @JsonProperty("name")
        private String name;
        @JsonProperty("clusterName")
        private String clusterName;
        @JsonProperty("format")
        private String format;
        @JsonProperty("shared")
        private Boolean shared;
        @JsonProperty("comment")
        private String comment;
    }
}
