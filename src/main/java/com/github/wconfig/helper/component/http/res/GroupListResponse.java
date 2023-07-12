package com.github.wconfig.helper.component.http.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GroupListResponse
 *
 * @author: lupeng10
 * @create: 2023-05-23 19:31
 */
@NoArgsConstructor
@Data
public class GroupListResponse {


    @JsonProperty("code")
    private Integer code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private List<DataDTO> data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("id")
        private String id;
        @JsonProperty("createdBy")
        private String createdBy;
        @JsonProperty("createTimestamp")
        private String createTimestamp;
        @JsonProperty("updatedBy")
        private Object updatedBy;
        @JsonProperty("updateTimestamp")
        private String updateTimestamp;
        @JsonProperty("name")
        private String name;
        @JsonProperty("clusterName")
        private String clusterName;
        @JsonProperty("parentGroupName")
        private Object parentGroupName;
    }
}
