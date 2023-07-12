package com.github.wconfig.helper;

import com.github.wconfig.helper.component.CommonComponent;
import com.github.wconfig.helper.component.WorkspaceWatcher;
import com.github.wconfig.helper.localstorage.LocalStorage;
import com.github.wconfig.helper.service.analysis.AnalysisNamespaceFormChain;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * WConfigGotoEditerHandler
 *
 * @author lupeng10
 * @create 2023-05-20 22:32
 */
public class WConfigGotoEditorHandler implements GotoDeclarationHandler {

    private static final Logger logger = Logger.getInstance(WConfigGotoEditorHandler.class);

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement source, int offset, Editor editor) {
        try {
            Project project = editor.getProject();
            if (project == null || source == null) {
                return null;
            }
            String namespace = AnalysisNamespaceFormChain.getInstance().analysis(source);
            if (namespace == null) {
                return null;
            }
            List<String> pathList = WorkspaceWatcher.getPathByNamespace(namespace);
            if (CollectionUtils.isEmpty(pathList)) {
                return null;
            }
            List<PsiElement> ans = new ArrayList<>();
            for (String path : pathList) {
                if (StringUtils.contains(path, "@diff")) {
                    continue;
                }
                CommonComponent.ConfigFileInfo confInfo = CommonComponent.parseFileName(path);
                Settings setting = LocalStorage.getSetting();
                if (setting.isEnableDefaultGroup()
                        && StringUtils.isNotBlank(setting.getDefaultGroup())
                        && !StringUtils.contains(path, setting.getDefaultGroup())) {
                    continue;
                }
                if (!setting.isEnableGray()) {
                    // 灰度编辑没有打开不展示灰度的配置
                    if (StringUtils.isNotBlank(confInfo.getGrayIp())) {
                        continue;
                    }
                } else {
                    // 灰度编辑打开，如果配置了ip则根据ip过滤，没有填不过滤
                    String grayIp = StringUtils.trim(setting.getGrayIp());
                    if (StringUtils.isNotBlank(grayIp) && !StringUtils.contains(path, grayIp)) {
                        continue;
                    }
                }
                VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(path));
                if (vf != null) {
                    ans.add(PsiManager.getInstance(project).findFile(vf));
                }
            }
            return CollectionUtils.isNotEmpty(ans) ? ans.toArray(new PsiElement[0]) : null;
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

}
