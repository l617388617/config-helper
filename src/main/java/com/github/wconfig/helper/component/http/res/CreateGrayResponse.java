package com.github.wconfig.helper.component.http.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CreateGrayResponse
 *
 * @author lupeng10
 * @create 2023-07-03 10:38
 */
@NoArgsConstructor
@Data
public class CreateGrayResponse {

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
        @JsonProperty("groupName")
        private String groupName;
        @JsonProperty("namespaceName")
        private String namespaceName;
        @JsonProperty("branchName")
        private String branchName;
        @JsonProperty("rules")
        private List<RulesDTO> rules;
        @JsonProperty("releaseId")
        private Object releaseId;
        @JsonProperty("branchStatus")
        private Integer branchStatus;

        @NoArgsConstructor
        @Data
        public static class RulesDTO {
            @JsonProperty("name")
            private String name;
            @JsonProperty("comment")
            private String comment;
            @JsonProperty("ips")
            private List<String> ips;
        }
    }
}
