package com.github.config.helper.localstorage;

import java.util.LinkedHashMap;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * PropertiesConfigEntiry
 *
 * @author: lupeng10
 * @create: 2023-05-23 19:56
 */
public class PropertiesConfigEntity implements ConfigEntity {

    private String clusterName;
    private String groupName;
    private String namespace;
    private LinkedHashMap<String, String> content;
    private Gray gray;

    public PropertiesConfigEntity() {
    }

    public PropertiesConfigEntity(String clusterName, String groupName, String namespace, LinkedHashMap<String, String> content) {
        this.clusterName = clusterName;
        this.groupName = groupName;
        this.namespace = namespace;
        this.content = content;
    }

    @Override
    public String getClusterName() {
        return this.clusterName;
    }

    @Override
    public String getGroupName() {
        return this.groupName;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public Gray getGray() {
        return this.gray;
    }

    public void setGray(Gray gray) {
        this.gray = gray;
    }

    public String get(String key) {
        return content.get(key);
    }

    public LinkedHashMap<String, String> getContent() {
        return content;
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterName, groupName, namespace);
    }


    @Override
    public String getFormat() {
        return ConfigContentType.properties.name();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PropertiesConfigEntity
                && StringUtils.equals(((PropertiesConfigEntity) obj).getClusterName(), clusterName)
                && StringUtils.equals(((PropertiesConfigEntity) obj).getGroupName(), groupName)
                && StringUtils.equals(((PropertiesConfigEntity) obj).getNamespace(), namespace);
    }
}
