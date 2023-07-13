package com.github.config.helper.component.http.res4openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NoContentRes
 *
 * @author lupeng10
 * @create 2023-07-13 15:11
 */
@NoArgsConstructor
@Data
public class NoContentRes {

    @JsonProperty("code")
    private Integer code;
    @JsonProperty("message")
    private String message;
}
