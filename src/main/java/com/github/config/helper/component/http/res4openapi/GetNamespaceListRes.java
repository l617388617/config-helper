package com.github.config.helper.component.http.res4openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GetNamespaceListRes
 *
 * @author lupeng10
 * @create 2023-07-13 14:38
 */
@NoArgsConstructor
@Data
public class GetNamespaceListRes {

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
        private List<OrdersDTO> orders;
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
            private Object updatedBy;
            @JsonProperty("updateTimestamp")
            private Long updateTimestamp;
            @JsonProperty("name")
            private String name;
            @JsonProperty("clusterName")
            private String clusterName;
            @JsonProperty("groupName")
            private String groupName;
            @JsonProperty("format")
            private String format;
            @JsonProperty("shared")
            private Boolean shared;
            @JsonProperty("comment")
            private String comment;
            @JsonProperty("associated")
            private Boolean associated;
        }

        @NoArgsConstructor
        @Data
        public static class OrdersDTO {
            @JsonProperty("column")
            private String column;
            @JsonProperty("asc")
            private Boolean asc;
        }
    }
}
