package com.github.wconfig.helper.component.http.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * BaseResponse
 *
 * @author: lupeng10
 * @create: 2023-05-23 18:45
 */
@NoArgsConstructor
@Data
public class ClusterListResponse {

    @JsonProperty("code")
    private Integer code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("records")
        private List<RecordsDTO> records;
        @JsonProperty("total")
        private Integer total;
        @JsonProperty("size")
        private Integer size;
        @JsonProperty("current")
        private Integer current;
        @JsonProperty("orders")
        private List<?> orders;
        @JsonProperty("optimizeCountSql")
        private Boolean optimizeCountSql;
        @JsonProperty("searchCount")
        private Boolean searchCount;
        @JsonProperty("countId")
        private Object countId;
        @JsonProperty("maxLimit")
        private Object maxLimit;
        @JsonProperty("pages")
        private Integer pages;

        @NoArgsConstructor
        @Data
        public static class RecordsDTO {
            @JsonProperty("id")
            private String id;
            @JsonProperty("createdBy")
            private String createdBy;
            @JsonProperty("createTimestamp")
            private Long createTimestamp;
            @JsonProperty("updatedBy")
            private String updatedBy;
            @JsonProperty("updateTimestamp")
            private Long updateTimestamp;
            @JsonProperty("name")
            private String name;
            @JsonProperty("ownersId")
            private String ownersId;
            @JsonProperty("ownersName")
            private String ownersName;
            @JsonProperty("applyOwnersName")
            private String applyOwnersName;
            @JsonProperty("orgId")
            private String orgId;
            @JsonProperty("orgName")
            private String orgName;
            @JsonProperty("comment")
            private String comment;
            @JsonProperty("whiteIps")
            private String whiteIps;
            @JsonProperty("modifiedNotice")
            private Boolean modifiedNotice;
            @JsonProperty("orderSwitch")
            private Boolean orderSwitch;
            @JsonProperty("grayOrderSwitch")
            private Boolean grayOrderSwitch;
            @JsonProperty("groupOrderSwitch")
            private Boolean groupOrderSwitch;
            @JsonProperty("ownerOfCluster")
            private Boolean ownerOfCluster;
        }
    }
}
