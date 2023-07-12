package com.github.wconfig.helper.localstorage;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * TextConfigEntiry
 *
 * @author: lupeng10
 * @create: 2023-05-23 19:57
 */
public class TextConfigEntity implements ConfigEntity {

    public static final String DEFAULT_KEY = "default_key";

    private String clusterName;
    private String groupName;
    private String namespace;
    private String content;
    private Gray gray;

    public TextConfigEntity() {
    }

    public TextConfigEntity(String clusterName, String groupName, String namespace, String content) {
        this.clusterName = clusterName;
        this.groupName = groupName;
        this.namespace = namespace;
        this.content = content;
    }

    @Override
    public Gray getGray() {
        return this.gray;
    }

    public void setGray(Gray gray) {
        this.gray = gray;
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

    public String getContent() {
        return content;
    }

    @Override
    public String getFormat() {
        return ConfigContentType.txt.name();
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterName, groupName, namespace);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PropertiesConfigEntity
                && StringUtils.equals(((PropertiesConfigEntity) obj).getClusterName(), clusterName)
                && StringUtils.equals(((PropertiesConfigEntity) obj).getGroupName(), groupName)
                && StringUtils.equals(((PropertiesConfigEntity) obj).getNamespace(), namespace);
    }
}
