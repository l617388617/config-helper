package com.github.config.helper.component;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ConfigInfo
 *
 * @author lupeng10
 * @create 2023-07-14 18:22
 */
@Data
@Builder
@EqualsAndHashCode
public class ConfigInfo {
    String clusterKey;
    String clusterName;
    String group;
    String namespace;
    boolean master;
    String grayName;
    String grayBranchName;
    int grayVersion;
    List<String> grayIps;
    String format;
    String content;
}
