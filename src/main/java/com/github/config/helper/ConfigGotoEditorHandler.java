package com.github.config.helper;

import com.github.config.helper.component.ConfigInfo;
import com.github.config.helper.component.ConfigInfoManager;
import com.github.config.helper.localstorage.LocalStorage;
import com.github.config.helper.service.analysis.AnalysisNamespaceFormChain;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * WConfigGotoEditerHandler
 *
 * @author lupeng10
 * @create 2023-05-20 22:32
 */
public class ConfigGotoEditorHandler implements GotoDeclarationHandler {

    private static final Logger logger = Logger.getInstance(ConfigGotoEditorHandler.class);

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
            Set<String> clusterNameSet = LocalStorage.getClusterNameSet();
            Set<String> groupSet = LocalStorage.getGroupSet();
            List<ConfigInfo> configInfoList = ConfigInfoManager.getInstance().getConfigInfoByNamespace(namespace)
                    .stream()
                    .filter(c -> {
                        if (!clusterNameSet.contains(c.getClusterName())) {
                            return false;
                        }
                        if (!groupSet.contains(c.getGroup())) {
                            return false;
                        }
                        return StringUtils.equals(c.getNamespace(), namespace);
                    }).collect(Collectors.toList());

            if (CollectionUtils.isEmpty(configInfoList)) {
                return null;
            }
            List<PsiElement> ans = new ArrayList<>();
            for (ConfigInfo configInfo : configInfoList) {
                if (!configInfo.isMaster()) {
                    continue;
                }
                VirtualFile vf = ConfigInfoManager.getInstance().generateVirtualFile(project, configInfo);
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
