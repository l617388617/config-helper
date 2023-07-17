package com.github.config.helper.component;

import com.github.config.helper.component.http.ConfigCall4OpenApi;
import com.github.config.helper.component.http.ItemKeyValueDto;
import com.github.config.helper.component.http.res4openapi.GetMasterRes;
import com.github.config.helper.localstorage.ConfigContentType;
import com.github.config.helper.localstorage.LocalStorage;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.json.json5.Json5Language;
import com.intellij.lang.Language;
import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * ConfigInfoManager
 *
 * @author lupeng10
 * @create 2023-07-14 18:23
 */
public class ConfigInfoManager {

    private static final Set<ConfigInfo> infoSet = new HashSet<>();

    public static volatile ConfigInfoManager instance;

    public static ConfigInfoManager getInstance() {
        if (instance == null) {
            synchronized (ConfigInfoManager.class) {
                if (instance == null) {
                    instance = new ConfigInfoManager();
                }
            }
        }
        return instance;
    }

    public boolean addConfigInfo(ConfigInfo configInfo) {
        return infoSet.add(configInfo);
    }

    public void setConfigInfoSet(Set<ConfigInfo> infoList) {
        ConfigInfoManager.infoSet.clear();
        ConfigInfoManager.infoSet.addAll(infoList);
    }


    public Optional<ConfigInfo> getConfigInfoByNamespace(String namespace, Predicate<ConfigInfo> predicate) {
        return infoSet.stream()
                .filter(c -> StringUtils.equals(c.getNamespace(), namespace))
                .filter(predicate)
                .findFirst();
    }

    public VirtualFile generateVirtualFile(Project project, ConfigInfo configInfo) {
        return generateVirtualFile(project, configInfo, null, false);
    }

    public VirtualFile generateVirtualFile(Project project, ConfigInfo configInfo, String ip) {
        return generateVirtualFile(project, configInfo, ip, false);
    }

    public VirtualFile generateVirtualFile(Project project, ConfigInfo configInfo, String ip, boolean diff) {
        String fileName = Joiner.on("@").join(configInfo.getClusterName(), configInfo.getGroup(), configInfo.getNamespace());
        if (configInfo.isMaster() && StringUtils.isNotBlank(ip)) {
            String targetIp = configInfo.getGrayIps().stream().filter(s -> StringUtils.equals(s, ip)).findAny().orElse(null);
            if (StringUtils.isNotBlank(targetIp)) {
                fileName += "@" + targetIp;
            }
        }
        if (diff) {
            fileName += "@diff";
        }
        ConfigCall4OpenApi call4OpenApi = ConfigCall4OpenApi.getInstance();
        String content;
        Language language;
        if (StringUtils.equals(configInfo.getFormat(), "properties")) {
            fileName += ".properties";
            language = PropertiesLanguage.INSTANCE;
            if (configInfo.isMaster()) {
                GetMasterRes res = call4OpenApi.getMaster(configInfo.getClusterKey(), configInfo.getClusterName(), configInfo.getGroup(), configInfo.getNamespace());
                content = res.getData().stream()
                        .map(i -> Joiner.on("=").join(i.getItemKey(), i.getItemValue()))
                        .collect(Collectors.joining("\n"));
            } else {
                content = configInfo.getContent();
            }
        } else {
            fileName += ".json5";
            language = Json5Language.INSTANCE;
            if (configInfo.isMaster()) {
                GetMasterRes res = call4OpenApi.getMaster(configInfo.getClusterKey(), configInfo.getClusterName(), configInfo.getGroup(), configInfo.getNamespace());
                content = res.getData().stream().findFirst().map(ItemKeyValueDto::getItemValue).orElse("");
            } else {
                content = configInfo.getContent();
            }
        }
        String fullPath = Joiner.on(File.separator).join("wconfigws", fileName);
        return ScratchRootType.getInstance()
                .createScratchFile(project, fullPath, language, content, ScratchFileService.Option.create_if_missing);
    }


    public ConfigInfo parseFileName(String fileName) {
        ConfigInfo.ConfigInfoBuilder builder = ConfigInfo.builder();

        String fullNamespace = fileName;
        String workspace = LocalStorage.getWorkspace();
        if (StringUtils.startsWith(fileName, workspace)) {
            fullNamespace = fileName.substring(workspace.length() + 1);
        }
        if (StringUtils.contains(fullNamespace, "wconfigws" + File.separator)) {
            fullNamespace = StringUtils.substringAfter(fullNamespace, "wconfigws" + File.separator);
        }

        if (StringUtils.endsWith(fullNamespace, ".properties")) {
            builder.format(ConfigContentType.properties.name());
        } else {
            builder.format(ConfigContentType.txt.name());
        }

        fullNamespace = StringUtils.substringBeforeLast(fullNamespace, ".");
        String[] split = fullNamespace.split("@");
        for (int i = 0; i < split.length; i++) {
            if (i == 0) {
                builder.clusterName(split[i]);
            } else if (i == 1) {
                builder.group(split[i]);
            } else if (i == 2) {
                builder.namespace(split[i]);
            } else if (i == 3) {
                builder.grayIps(ImmutableList.of(split[i]));
            }
        }
        return builder.build();
    }
}
