package com.github.config.helper.localstorage;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * ConfigEntity
 *
 * @author: lupeng10
 * @create: 2023-05-23 19:58
 */
public interface ConfigEntity {

    String getClusterName();
    String getGroupName();
    String getNamespace();
    String getFormat();
    Gray getGray();

    @Data
    @AllArgsConstructor
    class Gray {
        private String grayBranchName;
        private List<String> grayIps;
    }
}
