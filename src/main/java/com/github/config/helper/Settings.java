package com.github.config.helper;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Settings
 *
 * @author: lupeng10
 * @create: 2023-05-24 11:37
 */
@Data
@State(name = "com.bj58.plugin.wconfig-helper", storages = {@Storage(value = "wconfig-helper.xml")})
public class Settings implements PersistentStateComponent<Settings> {

    private String cookie;
    private String oaName;
    private String workspace;
    private String searchKeys;
    private boolean enableGray;
    private String grayIp;
    private boolean enableDefaultGroup;
    private String defaultGroup;


    @Override
    public @Nullable Settings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull Settings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
