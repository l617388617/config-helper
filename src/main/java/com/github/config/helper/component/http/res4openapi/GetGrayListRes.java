package com.github.config.helper.component.http.res4openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GetGrayListRes
 *
 * @author lupeng10
 * @create 2023-07-13 14:50
 */
@NoArgsConstructor
@Data
public class GetGrayListRes {

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
            @JsonProperty("grayName")
            private String grayName;
            @JsonProperty("grayBranchName")
            private String grayBranchName;
            @JsonProperty("grayVersion")
            private Integer grayVersion;
            @JsonProperty("grayIps")
            private List<String> grayIps;
            @JsonProperty("configValue")
            private String configValue;
            @JsonProperty("createTimestamp")
            private Long createTimestamp;
        }
    }
}
