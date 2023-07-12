package com.github.config.helper.localstorage;

import org.apache.commons.lang3.StringUtils;

/**
 * ConfigContentType
 *
 * @author: lupeng10
 * @create: 2023-05-25 12:50
 */
public enum ConfigContentType {
    properties,
    txt,
    ;

    public static ConfigContentType fromName(String name) {
        for (ConfigContentType type : ConfigContentType.values()) {
            if (StringUtils.equals(type.name(), name)) {
                return type;
            }
        }
        return null;
    }
}
