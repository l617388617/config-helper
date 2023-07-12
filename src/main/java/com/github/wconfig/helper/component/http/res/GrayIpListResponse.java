package com.github.wconfig.helper.component.http.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GrayIpListResponse
 *
 * @author lupeng10
 * @create 2023-07-01 15:23
 */
@NoArgsConstructor
@Data
public class GrayIpListResponse {


    @JsonProperty("code")
    private Integer code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private List<DataDTO> data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @JsonProperty("grayName")
        private String grayName;

        // 灰度发布的时候需要
        @JsonProperty("grayBranchName")
        private String grayBranchName;
        @JsonProperty("grayVersion")
        private Integer grayVersion;

        // 一个对应多个
        @JsonProperty("grayIps")
        private List<String> grayIps;


        @JsonProperty("configValue")
        private String configValue;
        @JsonProperty("createTimestamp")
        private String createTimestamp;
    }
}
