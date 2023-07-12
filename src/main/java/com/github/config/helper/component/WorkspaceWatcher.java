package com.github.config.helper.component;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;

/**
 * WorkspaceWatcher
 *
 * @author lupeng10
 * @create 2023-06-30 19:57
 */
public class WorkspaceWatcher {

    private static final Logger logger = Logger.getInstance(WorkspaceWatcher.class);

    private static final Map<String, List<String>> namespace2PathMap = new ConcurrentHashMap<>();

    public static volatile FileAlterationMonitor monitor = null;

    public static void doWatch(String vfWsPath) {
        if (monitor == null) {
            synchronized (WorkspaceWatcher.class) {
                if (monitor == null) {
                    try {
                        // VirtualFile vf = ScratchFileService.getInstance().findFile(ScratchRootType.getInstance(),
                        //         LocalStorage.getWorkspace(), ScratchFileService.Option.existing_only);
                        Collection<File> wConfigFiles = FileUtils.listFiles(new File(vfWsPath), new String[]{CommonComponent.JSON_5, CommonComponent.PROPERTIES}, true);
                        wConfigFiles.forEach(f -> {
                            CommonComponent.ConfigFileInfo confInfo = CommonComponent.parseFileName(f.getPath());
                            namespace2PathMap.putIfAbsent(confInfo.getNamespace(), new ArrayList<>());
                            namespace2PathMap.get(confInfo.getNamespace()).add(f.getPath());
                        });

                        FileAlterationObserver observer = new FileAlterationObserver(vfWsPath);
                        monitor = new FileAlterationMonitor(100, observer);
                        observer.addListener(new FileAlterationListenerAdaptor() {
                            @Override
                            public void onFileCreate(File file) {
                                if (StringUtils.endsWith(file.getPath(), CommonComponent.JSON_5)
                                        || StringUtils.endsWith(file.getPath(), CommonComponent.PROPERTIES)) {
                                    CommonComponent.ConfigFileInfo confInfo = CommonComponent.parseFileName(file.getPath());
                                    namespace2PathMap.putIfAbsent(confInfo.getNamespace(), new ArrayList<>());
                                    namespace2PathMap.get(confInfo.getNamespace()).add(file.getPath());
                                }
                            }

                            @Override
                            public void onFileDelete(File file) {
                                if (StringUtils.endsWith(file.getPath(), CommonComponent.JSON_5)
                                        || StringUtils.endsWith(file.getPath(), CommonComponent.PROPERTIES)) {
                                    CommonComponent.ConfigFileInfo confInfo = CommonComponent.parseFileName(file.getPath());
                                    namespace2PathMap.get(confInfo.getNamespace()).remove(file.getPath());
                                }
                            }
                        });
                        monitor.start();
                    } catch (Exception e) {
                        logger.error("WorkspaceWatcher init error", e);
                    }
                }
            }
        }
    }


    public static List<String> getPathByNamespace(String namespace) {
        return namespace2PathMap.get(namespace);
    }
}
