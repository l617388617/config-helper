package com.github.config.helper.component.http.res4openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.config.helper.component.http.ItemKeyValueDto;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GetMasterRes
 *
 * @author lupeng10
 * @create 2023-07-13 15:56
 */
@NoArgsConstructor
@Data
public class GetMasterRes {

    @JsonProperty("code")
    private Integer code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private List<ItemKeyValueDto> data;
}
