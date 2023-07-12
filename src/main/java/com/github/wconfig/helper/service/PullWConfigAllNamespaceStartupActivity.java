package com.github.wconfig.helper.service;

import com.github.wconfig.helper.localstorage.LocalStorage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * IndexSpringStartupActivity
 *
 * @author: lupeng10
 * @create: 2023-05-22 17:22
 */
public class PullWConfigAllNamespaceStartupActivity implements StartupActivity.Background {

    private static final Logger log = Logger.getInstance(PullWConfigAllNamespaceStartupActivity.class);

    @Override
    public void runActivity(@NotNull Project project) {
        // 只有打开的第一个项目会去构建索引
        if (LocalStorage.getProject() == null) {
            ApplicationManager.getApplication().getService(ApplicationService.class).pullAllConfig(project);
        }
        LocalStorage.addProject(project);

        // RunOnceUtil.runOnceForApp("com.bj58.wconfig.helper.service.PullWConfigAllNamespaceStartupActivity.runActivity", () -> {
        //     LocalStorage.removeLocalCookiesFile();
        //     LocalStorage.currentProject = project;
        //     ApplicationManager.getApplication().getService(ApplicationService.class).pullAllConfig();
        // });
    }


}
