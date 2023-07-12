package com.github.config.helper.component.http.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * NamespaceContentResponse
 *
 * @author: lupeng10
 * @create: 2023-05-23 19:51
 */
@NoArgsConstructor
@Data
public class NamespaceContentResponse {


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
        private String updatedBy;
        @JsonProperty("updateTimestamp")
        private String updateTimestamp;
        @JsonProperty("namespaceMappingId")
        private String namespaceMappingId;
        @JsonProperty("status")
        private String status;
        @JsonProperty("itemKey")
        private String itemKey;
        @JsonProperty("itemValue")
        private String itemValue;
        @JsonProperty("lineNum")
        private Integer lineNum;
        @JsonProperty("comment")
        private String comment;
        @JsonProperty("masterKey")
        private Boolean masterKey;
        @JsonProperty("cover")
        private Boolean cover;
    }
}
