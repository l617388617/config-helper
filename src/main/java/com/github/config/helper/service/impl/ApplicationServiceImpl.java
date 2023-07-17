package com.github.config.helper.service.impl;

import com.github.config.helper.component.ConfigInfo;
import com.github.config.helper.component.ConfigInfoManager;
import com.github.config.helper.component.http.ConfigCall4OpenApi;
import com.github.config.helper.component.http.res4openapi.GetNamespaceListRes;
import com.github.config.helper.localstorage.LocalStorage;
import com.github.config.helper.service.ApplicationService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ApplicationServiceImpl
 *
 * @author lupeng10
 * @create 2023-07-03 14:27
 */
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger log = Logger.getInstance(ApplicationServiceImpl.class);

    private static final ReentrantLock lock = new ReentrantLock();

    public ApplicationServiceImpl() {
    }

    @Override
    public void pullAllConfig(Project project) {
        boolean lockState = false;
        try {
            lockState = lock.tryLock(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (lockState) {
            try {
                pullWithOpenApi();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }
    }

    private static void pullWithOpenApi() {
        ConfigCall4OpenApi call4OpenApi = ConfigCall4OpenApi.getInstance();
        String clusterKeyName = LocalStorage.getSetting().getClusterKeyName();
        String[] groupArr = LocalStorage.getSetting().getGroupList().split(",");
        String[] keyNameArr = clusterKeyName.split(";");
        ConfigInfoManager configInfoManager = ConfigInfoManager.getInstance();
        Set<ConfigInfo> infoSet = new HashSet<>();
        for (String keyName : keyNameArr) {
            String[] split = keyName.split(",");
            String key = split[0];
            String name = split[1];
            for (String group : groupArr) {
                List<GetNamespaceListRes.DataDTO.RecordsDTO> allNamespace = call4OpenApi.getAllNamespace(key, name, group);
                for (GetNamespaceListRes.DataDTO.RecordsDTO recordsDTO : allNamespace) {
                    String namespace = recordsDTO.getName();

                    infoSet.add(ConfigInfo.builder()
                            .clusterKey(key)
                            .clusterName(name)
                            .group(group)
                            .namespace(namespace)
                            .master(true)
                            .format(recordsDTO.getFormat())
                            .build());
                    // open-api 不支持直接获取灰度
                    // List<GetGrayListRes.DataDTO.RecordsDTO> allGrayList = call4OpenApi.getAllGrayList(key, name, group, namespace);
                    // for (GetGrayListRes.DataDTO.RecordsDTO grayDto : allGrayList) {
                    //     infoSet.add(ConfigInfo.builder()
                    //             .clusterKey(key)
                    //             .clusterName(name)
                    //             .group(group)
                    //             .namespace(namespace)
                    //             .master(false)
                    //             .grayName(grayDto.getGrayName())
                    //             .grayBranchName(grayDto.getGrayBranchName())
                    //             .grayVersion(grayDto.getGrayVersion())
                    //             .grayIps(grayDto.getGrayIps())
                    //             .format(recordsDTO.getFormat())
                    //             .content(grayDto.getConfigValue())
                    //             .build());
                    // }
                }
            }
        }
        configInfoManager.setConfigInfoSet(infoSet);
    }

    // @Deprecated
    // private static void pull() {
    //     ConfigCaller configCaller = new ConfigCaller();
    //     int currPage = 1;
    //     List<CompletableFuture<Object>> futures = new ArrayList<>();
    //     Queue<ConfigEntity> configEntityList = new LinkedBlockingQueue<>();
    //     Stopwatch stopwatch = Stopwatch.createStarted();
    //     while (true) {
    //         ClusterListResponse clusterList = configCaller.getClusterList(currPage);
    //         List<ClusterListResponse.DataDTO.RecordsDTO> records = clusterList.getData().getRecords();
    //         for (ClusterListResponse.DataDTO.RecordsDTO record : records) {
    //             String clusterName = record.getName();
    //             List<CompletableFuture<Object>> pullFutures = pullConfigByCluster(configCaller, configEntityList, clusterName);
    //             futures.addAll(pullFutures);
    //         }
    //         if (clusterList.getData().getPages() <= currPage) {
    //             break;
    //         }
    //         currPage++;
    //     }
    //     CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    //     // 构建索引
    //     ArrayList<ConfigEntity> configEntities = new ArrayList<>(configEntityList);
    //     log.info(String.format("【拉取config耗时: %s 毫秒，共拉取 %s 个配置】", stopwatch.elapsed(TimeUnit.MILLISECONDS), configEntities.size()));
    //     CommonComponent.buildIndex(configEntities, true);
    // }
    //
    // private static List<CompletableFuture<Object>> pullConfigByCluster(ConfigCaller configCaller, Queue<ConfigEntity> configEntityList, String clusterName) {
    //     List<CompletableFuture<Object>> ans = new ArrayList<>();
    //     GroupListResponse groupList = configCaller.getGroupList(clusterName);
    //     for (GroupListResponse.DataDTO datum : groupList.getData()) {
    //         String groupName = datum.getName();
    //         int p = 1;
    //         while (true) {
    //             NamespaceListResponse namespaceList = configCaller.getNamespaceList(p, clusterName, groupName);
    //             if (namespaceList == null || namespaceList.getData() == null) {
    //                 continue;
    //             }
    //             for (NamespaceListResponse.DataDTO.RecordsDTO dto : namespaceList.getData().getRecords()) {
    //                 CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
    //                     if (StringUtils.equals(dto.getFormat(), ConfigContentType.properties.name())) {
    //                         // master
    //                         LinkedHashMap<String, String> content = configCaller.getNamespaceContent(clusterName, groupName, dto.getName()).getData().stream()
    //                                 .collect(LinkedHashMap::new, (c, t) -> c.put(t.getItemKey(), t.getItemValue()), HashMap::putAll);
    //
    //                         configEntityList.add(new PropertiesConfigEntity(clusterName, groupName, dto.getName(), content));
    //                         // 灰度列表
    //                         for (GrayIpListResponse.DataDTO grayDto : configCaller.getGrayIpList(clusterName, groupName, dto.getName()).getData()) {
    //                             LinkedHashMap<String, String> grayContent = configCaller.getNamespaceContent(clusterName, groupName, dto.getName()).getData().stream()
    //                                     .collect(LinkedHashMap::new, (c, t) -> c.put(t.getItemKey(), t.getItemValue()), HashMap::putAll);
    //
    //                             String grayBranchName = grayDto.getGrayBranchName();
    //                             List<String> grayIps = grayDto.getGrayIps();
    //                             PropertiesConfigEntity grayConfig = new PropertiesConfigEntity(clusterName, groupName, dto.getName(), grayContent);
    //                             grayConfig.setGray(new ConfigEntity.Gray(grayBranchName, grayIps));
    //                             configEntityList.add(grayConfig);
    //                         }
    //                     } else {
    //                         // master
    //                         String content = configCaller.getNamespaceContent(clusterName, groupName, dto.getName())
    //                                 .getData()
    //                                 .stream()
    //                                 .findAny()
    //                                 .map(NamespaceContentResponse.DataDTO::getItemValue)
    //                                 .orElse("");
    //                         configEntityList.add(new TextConfigEntity(clusterName, groupName, dto.getName(), content));
    //                         // 灰度列表
    //                         for (GrayIpListResponse.DataDTO grayDto : configCaller.getGrayIpList(clusterName, groupName, dto.getName()).getData()) {
    //                             String grayBranchName = grayDto.getGrayBranchName();
    //                             String grayContent = configCaller.getNamespaceContent(clusterName, grayBranchName, dto.getName())
    //                                     .getData()
    //                                     .stream()
    //                                     .findAny()
    //                                     .map(NamespaceContentResponse.DataDTO::getItemValue)
    //                                     .orElse("");
    //
    //                             List<String> grayIps = grayDto.getGrayIps();
    //                             TextConfigEntity grayConfig = new TextConfigEntity(clusterName, groupName, dto.getName(), grayContent);
    //                             grayConfig.setGray(new ConfigEntity.Gray(grayBranchName, grayIps));
    //                             configEntityList.add(grayConfig);
    //                         }
    //                     }
    //                     return null;
    //                 }, CommonThreadPool.pool);
    //                 ans.add(future);
    //             }
    //             if (namespaceList.getData().getPages() <= p) {
    //                 break;
    //             }
    //             p++;
    //         }
    //     }
    //     return ans;
    // }
}
