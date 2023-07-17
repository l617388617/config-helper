package com.github.config.helper.component;

import com.github.config.helper.component.http.ConfigCall4OpenApi;
import com.github.config.helper.component.http.ItemKeyValueDto;
import com.github.config.helper.component.http.res4openapi.GetMasterRes;
import com.github.config.helper.localstorage.ConfigContentType;
import com.github.config.helper.localstorage.ConfigEntity;
import com.github.config.helper.localstorage.LocalStorage;
import com.github.config.helper.localstorage.PropertiesConfigEntity;
import com.github.config.helper.localstorage.TextConfigEntity;
import com.google.common.base.Joiner;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.json.json5.Json5Language;
import com.intellij.lang.Language;
import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * CommonComponent
 *
 * @author lupeng10
 * @create 2023-06-11 21:03
 */
public class CommonComponent {

    private static final Logger log = Logger.getInstance(CommonComponent.class);

    public static final String delimiter = "@";
    public static final String PROPERTIES = "properties";
    public static final String JSON_5 = "json5";

    private static final Map<String, HashSet<ConfigFileInfo>> grayIp2GrayBranchName = new ConcurrentHashMap<>();
    public static final TreeSet<String> allGroup = new TreeSet<>();
    public static final Map<String, TreeSet<String>> cluster2GroupsMap = new ConcurrentHashMap<>();

    public static String getGrayBranchName(String ip, String cluster) {
        if (StringUtils.isBlank(ip) || StringUtils.isBlank(cluster)) {
            return "";
        }
        if (!grayIp2GrayBranchName.containsKey(ip)) {
            return "";
        }
        return grayIp2GrayBranchName.get(ip).stream()
                .filter(p -> StringUtils.equals(p.getCluster(), cluster))
                .map(ConfigFileInfo::getGrayBranchName)
                .findFirst().orElse("");
    }

    public static void setGrayIp2GrayBranchName(String ip, CommonComponent.ConfigFileInfo configFileInfo) {
        grayIp2GrayBranchName.putIfAbsent(ip, new HashSet<>());
        grayIp2GrayBranchName.get(ip).add(configFileInfo);
    }

    public static Map<ConfigEntity, VirtualFile> buildIndex(List<ConfigEntity> configEntities, boolean isClear) {
        if (isClear) {
            // 清空重建本地config
            removeLocalConfig();
        }
        Map<ConfigEntity, VirtualFile> ans = new HashMap<>();
        String vfWsPath = null;
        for (ConfigEntity entity : configEntities) {
            allGroup.add(entity.getGroupName());
            cluster2GroupsMap.putIfAbsent(entity.getClusterName(), new TreeSet<>());
            cluster2GroupsMap.get(entity.getClusterName()).add(entity.getGroupName());

            String content;
            if (StringUtils.equals(entity.getFormat(), ConfigContentType.properties.name())) {
                PropertiesConfigEntity configEntity = (PropertiesConfigEntity) entity;
                content = configEntity.getContent().entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining("\n"));
            } else {
                TextConfigEntity textConfig = (TextConfigEntity) entity;
                content = textConfig.getContent();
            }

            ConfigEntity.Gray gray = entity.getGray();
            if (gray != null) {
                List<String> grayIps = gray.getGrayIps();
                for (String grayIp : grayIps) {
                    String filePath = CommonComponent.generateFilePathWithoutWs(entity.getClusterName(), entity.getGroupName(),
                            entity.getNamespace(), grayIp, ConfigContentType.fromName(entity.getFormat()), false);
                    try {
                        Language language;
                        if (StringUtils.equals(entity.getFormat(), ConfigContentType.properties.name())) {
                            language = PropertiesLanguage.INSTANCE;
                        } else {
                            language = Json5Language.INSTANCE;
                        }
                        VirtualFile scratchFile = ScratchRootType.getInstance().createScratchFile(LocalStorage.getProject(),
                                "wconfigws" + File.separator + filePath,
                                language, content, ScratchFileService.Option.create_if_missing);
                        if (vfWsPath == null && scratchFile != null) {
                            vfWsPath = StringUtils.substringBefore(scratchFile.getPath(), "wconfigws") + "wconfigws";
                            ans.put(entity, scratchFile);
                        }

                        grayIp2GrayBranchName.putIfAbsent(grayIp, new HashSet<>());
                        ConfigFileInfo configFileInfo = new ConfigFileInfo();
                        configFileInfo.setNamespace(entity.getNamespace());
                        configFileInfo.setGroup(entity.getGroupName());
                        configFileInfo.setCluster(entity.getClusterName());
                        configFileInfo.setGrayBranchName(gray.getGrayBranchName());
                        configFileInfo.setFullPath(vfWsPath);
                        grayIp2GrayBranchName.get(grayIp).add(configFileInfo);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            } else {
                String filePath = CommonComponent.generateFilePathWithoutWs(entity.getClusterName(), entity.getGroupName(),
                        entity.getNamespace(), null, ConfigContentType.fromName(entity.getFormat()), false);
                try {
                    Language language;
                    if (StringUtils.equals(entity.getFormat(), ConfigContentType.properties.name())) {
                        language = PropertiesLanguage.INSTANCE;
                    } else {
                        language = Json5Language.INSTANCE;
                    }
                    VirtualFile scratchFile = ScratchRootType.getInstance().createScratchFile(LocalStorage.getProject(),
                            "wconfigws" + File.separator + filePath,
                            language, content, ScratchFileService.Option.create_if_missing);
                    if (vfWsPath == null && scratchFile != null) {
                        vfWsPath = StringUtils.substringBefore(scratchFile.getPath(), "wconfigws") + "wconfigws";
                        ans.put(entity, scratchFile);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            // 将本地文件路径放到内存
            if (StringUtils.isNotBlank(vfWsPath)) {
                WorkspaceWatcher.doWatch(vfWsPath);
                LocalStorage.setVirtualWorkspace(vfWsPath);
            }
        }
        log.info("buildIndex success");
        return ans;
    }

    private static void removeLocalConfig() {
        try {
            IOFileFilter filter = new IOFileFilter() {
                @Override
                public boolean accept(File file) {
                    return !StringUtils.contains(file.getPath(), "cookies");
                }

                @Override
                public boolean accept(File dir, String name) {
                    return !StringUtils.contains(dir.getPath(), "cookies")
                            && !StringUtils.contains(name, "cookies");
                }
            };
            if (StringUtils.isNotBlank(LocalStorage.getVirtualWorkspace())) {
                Collection<File> files = FileUtils.listFilesAndDirs(new File(LocalStorage.getVirtualWorkspace()), filter, filter);
                for (File f : files) {
                    if (!f.isDirectory()) {
                        f.deleteOnExit();
                    }
                }
                for (File f : files) {
                    if (f.isDirectory()) {
                        if (!StringUtils.equals(f.getPath(), LocalStorage.getWorkspace())) {
                            FileUtils.deleteDirectory(f);
                        }
                    }
                }
                log.info("文件清除====重新构建");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    // public static void writeConfigContent2File(String cluster, String group, String namespace, String grayIp, String targetFilePath, @NotNull Project project) {
    //     final String finalGroup = StringUtils.isBlank(group) ? LocalStorage.getDefaultGroup() : group;
    //     ConfigInfoManager.getInstance().getConfigInfoByNamespace(namespace, c -> {
    //         if (!StringUtils.equals(c.getClusterName(), cluster)) {
    //             return false;
    //         }
    //         if (!StringUtils.equals(c.getGroup(), finalGroup)) {
    //             return false;
    //         }
    //
    //     })
    //
    //
    //     List<String> filePathList = WorkspaceWatcher.getPathByNamespace(namespace);
    //     for (String filePath : filePathList) {
    //         ConfigFileInfo configFileInfo = parseFileName(filePath);
    //         if (!StringUtils.equals(configFileInfo.getCluster(), cluster)) {
    //             continue;
    //         }
    //         if (!StringUtils.equals(configFileInfo.getGrayIp(), grayIp)) {
    //             continue;
    //         }
    //         if (!StringUtils.equals(configFileInfo.getGroup(), group)) {
    //             continue;
    //         }
    //         if (!StringUtils.equals(configFileInfo.getNamespace(), namespace)) {
    //             continue;
    //         }
    //         // NamespaceContentResponse content;
    //         // String grayBranchName = CommonComponent.getGrayBranchName(grayIp, cluster);
    //         // if (StringUtils.isNotBlank(grayBranchName)) {
    //         //     content = ConfigCaller.INSTANCE
    //         //             .getNamespaceContent(configFileInfo.getCluster(), grayBranchName, configFileInfo.getNamespace());
    //         // } else {
    //         //     content = ConfigCaller.INSTANCE
    //         //             .getNamespaceContent(configFileInfo.getCluster(), configFileInfo.getGroup(), configFileInfo.getNamespace());
    //         // }
    //
    //         String clusterKey = LocalStorage.getClusterKeyByName(configFileInfo.getCluster());
    //         GetMasterRes masterRes = ConfigCall4OpenApi.getInstance().getMaster(clusterKey, configFileInfo.getCluster(), configFileInfo.getGroup(), configFileInfo.getNamespace());
    //         List<ItemKeyValueDto> data = masterRes.getData();
    //
    //         String contentStr;
    //         if (configFileInfo.getContentType() == ConfigContentType.properties) {
    //             contentStr = Joiner.on("\n").join(data.stream().map(d -> d.getItemKey() + "=" + d.getItemValue()).collect(Collectors.toList()));
    //         } else {
    //             if (CollectionUtils.isEmpty(data)) {
    //                 contentStr = "";
    //             } else {
    //                 contentStr = data.get(0).getItemValue();
    //             }
    //         }
    //         Language language = configFileInfo.getContentType() == ConfigContentType.properties ? PropertiesLanguage.INSTANCE : Json5Language.INSTANCE;
    //         VirtualFile scratchFile = ScratchRootType.getInstance().createScratchFile(project,
    //                 targetFilePath, language, contentStr, ScratchFileService.Option.create_if_missing);
    //
    //         // try {
    //         //     new File(targetFilePath).deleteOnExit();
    //         //     FileUtils.write(new File(targetFilePath), contentStr, StandardCharsets.UTF_8);
    //         // } catch (IOException e) {
    //         //     e.printStackTrace();
    //         // }
    //     }
    // }

    public static String generateFilePath(String cluster, String group, String namespace, String grayIp,
            ConfigContentType contentType, boolean isDiff) {
        return LocalStorage.getWorkspace() + File.separator + generateFilePathWithoutWs(cluster, group, namespace, grayIp, contentType, isDiff);
    }

    public static String generateFilePathWithoutWs(String cluster, String group, String namespace, String grayIp,
            ConfigContentType contentType, boolean isDiff) {
        String fileName = Joiner.on(delimiter).join(cluster, group, namespace);
        if (StringUtils.isNotBlank(grayIp)) {
            fileName += delimiter + grayIp;
        }
        if (isDiff) {
            fileName += delimiter + "diff";
        }
        if (contentType == ConfigContentType.properties) {
            fileName += "." + PROPERTIES;
        } else {
            fileName += "." + JSON_5;
        }
        return fileName;
    }



    @Data
    @EqualsAndHashCode
    public static class ConfigFileInfo {
        private String fullPath;
        String cluster;
        String group;
        String namespace;
        String grayIp;
        String grayBranchName;
        ConfigContentType contentType;
    }

    public static ConfigEntity generate2ConfigEntity(File file) {
        try {
            ConfigFileInfo confInfo = parseFileName(file.getAbsolutePath());
            if (StringUtils.endsWith(file.getAbsolutePath(), PROPERTIES)) {
                LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
                for (String line : FileUtils.readLines(file, StandardCharsets.UTF_8)) {
                    if (StringUtils.isNotBlank(line)) {
                        linkedHashMap.put(line.split("=")[0], line.split("=")[1]);
                    }
                }
                return new PropertiesConfigEntity(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace(), linkedHashMap);
            } else {
                return new TextConfigEntity(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace(), FileUtils.readFileToString(file, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private static final NotificationGroup STICKY_GROUP =
            new NotificationGroup("wconfig-helper.notification", NotificationDisplayType.STICKY_BALLOON, true);

    public static void notification(boolean success, String subTitle, String content, Project project) {
        Notification msg = STICKY_GROUP.createNotification(
                "WConfig-helper", subTitle, content,
                success ? NotificationType.INFORMATION : NotificationType.ERROR);
        msg.notify(project);
    }
}
