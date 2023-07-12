package com.github.config.helper.service.impl;

import com.github.config.helper.component.CommonComponent;
import com.github.config.helper.component.CommonThreadPool;
import com.github.config.helper.component.http.ConfigCaller;
import com.github.config.helper.component.http.res.ClusterListResponse;
import com.github.config.helper.component.http.res.GrayIpListResponse;
import com.github.config.helper.component.http.res.GroupListResponse;
import com.github.config.helper.component.http.res.NamespaceContentResponse;
import com.github.config.helper.component.http.res.NamespaceListResponse;
import com.github.config.helper.localstorage.ConfigContentType;
import com.github.config.helper.localstorage.ConfigEntity;
import com.github.config.helper.localstorage.PropertiesConfigEntity;
import com.github.config.helper.localstorage.TextConfigEntity;
import com.github.config.helper.service.ApplicationService;
import com.google.common.base.Stopwatch;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.lang3.StringUtils;

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
                pull();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                lock.unlock();
            }
        }
    }

    private static void pull() {
        ConfigCaller configCaller = new ConfigCaller();
        int currPage = 1;
        List<CompletableFuture<Object>> futures = new ArrayList<>();
        Queue<ConfigEntity> configEntityList = new LinkedBlockingQueue<>();
        Stopwatch stopwatch = Stopwatch.createStarted();
        while (true) {
            ClusterListResponse clusterList = configCaller.getClusterList(currPage);
            List<ClusterListResponse.DataDTO.RecordsDTO> records = clusterList.getData().getRecords();
            for (ClusterListResponse.DataDTO.RecordsDTO record : records) {
                String clusterName = record.getName();
                List<CompletableFuture<Object>> pullFutures = pullConfigByCluster(configCaller, configEntityList, clusterName);
                futures.addAll(pullFutures);
            }
            if (clusterList.getData().getPages() <= currPage) {
                break;
            }
            currPage++;
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        // 构建索引
        ArrayList<ConfigEntity> configEntities = new ArrayList<>(configEntityList);
        log.info(String.format("【拉取config耗时: %s 毫秒，共拉取 %s 个配置】", stopwatch.elapsed(TimeUnit.MILLISECONDS), configEntities.size()));
        CommonComponent.buildIndex(configEntities, true);
    }

    private static List<CompletableFuture<Object>> pullConfigByCluster(ConfigCaller configCaller, Queue<ConfigEntity> configEntityList, String clusterName) {
        List<CompletableFuture<Object>> ans = new ArrayList<>();
        GroupListResponse groupList = configCaller.getGroupList(clusterName);
        for (GroupListResponse.DataDTO datum : groupList.getData()) {
            String groupName = datum.getName();
            int p = 1;
            while (true) {
                NamespaceListResponse namespaceList = configCaller.getNamespaceList(p, clusterName, groupName);
                if (namespaceList == null || namespaceList.getData() == null) {
                    continue;
                }
                for (NamespaceListResponse.DataDTO.RecordsDTO dto : namespaceList.getData().getRecords()) {
                    CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                        if (StringUtils.equals(dto.getFormat(), ConfigContentType.properties.name())) {
                            // master
                            LinkedHashMap<String, String> content = configCaller.getNamespaceContent(clusterName, groupName, dto.getName()).getData().stream()
                                    .collect(LinkedHashMap::new, (c, t) -> c.put(t.getItemKey(), t.getItemValue()), HashMap::putAll);

                            configEntityList.add(new PropertiesConfigEntity(clusterName, groupName, dto.getName(), content));
                            // 灰度列表
                            for (GrayIpListResponse.DataDTO grayDto : configCaller.getGrayIpList(clusterName, groupName, dto.getName()).getData()) {
                                LinkedHashMap<String, String> grayContent = configCaller.getNamespaceContent(clusterName, groupName, dto.getName()).getData().stream()
                                        .collect(LinkedHashMap::new, (c, t) -> c.put(t.getItemKey(), t.getItemValue()), HashMap::putAll);

                                String grayBranchName = grayDto.getGrayBranchName();
                                List<String> grayIps = grayDto.getGrayIps();
                                PropertiesConfigEntity grayConfig = new PropertiesConfigEntity(clusterName, groupName, dto.getName(), grayContent);
                                grayConfig.setGray(new ConfigEntity.Gray(grayBranchName, grayIps));
                                configEntityList.add(grayConfig);
                            }
                        } else {
                            // master
                            String content = configCaller.getNamespaceContent(clusterName, groupName, dto.getName())
                                    .getData()
                                    .stream()
                                    .findAny()
                                    .map(NamespaceContentResponse.DataDTO::getItemValue)
                                    .orElse("");
                            configEntityList.add(new TextConfigEntity(clusterName, groupName, dto.getName(), content));
                            // 灰度列表
                            for (GrayIpListResponse.DataDTO grayDto : configCaller.getGrayIpList(clusterName, groupName, dto.getName()).getData()) {
                                String grayBranchName = grayDto.getGrayBranchName();
                                String grayContent = configCaller.getNamespaceContent(clusterName, grayBranchName, dto.getName())
                                        .getData()
                                        .stream()
                                        .findAny()
                                        .map(NamespaceContentResponse.DataDTO::getItemValue)
                                        .orElse("");

                                List<String> grayIps = grayDto.getGrayIps();
                                TextConfigEntity grayConfig = new TextConfigEntity(clusterName, groupName, dto.getName(), grayContent);
                                grayConfig.setGray(new ConfigEntity.Gray(grayBranchName, grayIps));
                                configEntityList.add(grayConfig);
                            }
                        }
                        return null;
                    }, CommonThreadPool.pool);
                    ans.add(future);
                }
                if (namespaceList.getData().getPages() <= p) {
                    break;
                }
                p++;
            }
        }
        return ans;
    }
}
