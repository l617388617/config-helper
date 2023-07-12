package com.github.wconfig.helper;

import com.github.wconfig.helper.component.CommonComponent;
import com.github.wconfig.helper.component.WorkspaceWatcher;
import com.github.wconfig.helper.component.http.WConfigCaller;
import com.github.wconfig.helper.component.http.res.CreateGrayResponse;
import com.github.wconfig.helper.component.http.res.CreateNamespaceResponse;
import com.github.wconfig.helper.component.http.res.NamespaceContentResponse;
import com.github.wconfig.helper.component.json.JacksonUtil;
import com.github.wconfig.helper.localstorage.ConfigContentType;
import com.github.wconfig.helper.localstorage.ConfigEntity;
import com.github.wconfig.helper.localstorage.LocalStorage;
import com.github.wconfig.helper.localstorage.PropertiesConfigEntity;
import com.github.wconfig.helper.localstorage.TextConfigEntity;
import com.github.wconfig.helper.service.analysis.AnalysisNamespaceFormChain;
import com.github.wconfig.helper.views.CreateNamespaceDialog;
import com.github.wconfig.helper.views.CustomDiffWindow;
import com.google.common.collect.ImmutableList;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.json.json5.Json5Language;
import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonFile;
import com.intellij.lang.Language;
import com.intellij.lang.properties.PropertiesLanguage;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

/**
 * WConfigShowDiffAction
 * 打开diff比较，可以提交
 *
 * @author lupeng10
 * @create 2023-06-11 20:39
 */
public class WConfigShowDiffAction extends AnAction {

    private static final Logger logger = Logger.getInstance(WorkspaceWatcher.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            PsiFile psiFile = e.getData(PlatformDataKeys.PSI_FILE);
            Editor editor = e.getData(PlatformDataKeys.EDITOR);
            if (psiFile == null || editor == null) {
                logger.info("psiFile未发现 or editor 未发现");
                return;
            }
            if (psiFile instanceof PsiJavaFile) {
                openEditConfig(psiFile, editor);
            } else {
                showDiff(psiFile);
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void showCreateNamespaceDialog(PsiFile psiFile, String namespace) {
        new CreateNamespaceDialog(psiFile.getProject(), namespace, d -> {
            try {
                NamespaceContentResponse response = WConfigCaller.INSTANCE.getNamespaceContent(d.getCluster(), d.getGroup(), d.getNamespace());
                if (CollectionUtils.isNotEmpty(response.getData())) {
                    CommonComponent.notification(false, "配置已存在", d.getNamespace(), psiFile.getProject());
                    return;
                }
                ConfigEntity configEntity;
                String format;
                if (StringUtils.equals(d.getFormat(), CommonComponent.PROPERTIES)) {
                    configEntity = new PropertiesConfigEntity(d.getCluster(), d.getGroup(), d.getNamespace(), new LinkedHashMap<>());
                    format = "properties";
                } else {
                    configEntity = new TextConfigEntity(d.getCluster(), d.getGroup(), d.getNamespace(), "");
                    format = "txt";
                }

                CreateNamespaceResponse createRes = WConfigCaller.INSTANCE.createNamespace(d.getCluster(), d.getGroup(), d.getNamespace(), d.getDesc(), format);
                if (createRes.getCode() == 200) {
                    WConfigCaller.INSTANCE.releaseMaster(d.getCluster(), d.getGroup(), d.getNamespace());
                    Map<ConfigEntity, VirtualFile> config2VfMap = CommonComponent.buildIndex(ImmutableList.of(configEntity), false);
                    config2VfMap.entrySet().stream().findFirst().map(Map.Entry::getValue)
                            .ifPresent(virtualFile -> {
                                FileEditorManager.getInstance(psiFile.getProject()).openFile(virtualFile, true, true);
                                // 通知
                                CommonComponent.notification(true, "创建成功",
                                        String.format("<div>%s</div><div>%s</div><div>%s</div>", d.getCluster(), d.getGroup(), namespace),
                                        psiFile.getProject());
                            });
                }
            } catch (Exception e) {
                logger.error("showCreateNamespaceDialog error", e);
            }
        }).showAndGet();
    }

    private void openEditConfig(PsiFile psiFile, Editor editor) throws IOException {
        Project project = editor.getProject();
        PsiElement source = psiFile.findElementAt(editor.getCaretModel().getOffset());
        if (project == null || source == null) {
            logger.info("光标未发现");
            return;
        }
        String namespace = AnalysisNamespaceFormChain.getInstance().analysis(source);
        if (namespace == null) {
            logger.info("namespace解析为空");
            return;
        }
        List<String> pathList = WorkspaceWatcher.getPathByNamespace(namespace);
        if (CollectionUtils.isEmpty(pathList)) {
            logger.info("根据Namespace未找到对应的本地config文件，引导创建");
            // 提示创建新的config
            showCreateNamespaceDialog(psiFile, namespace);
            return;
        }
        for (String path : pathList) {
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
            String path1 = StringUtils.substringAfter(path, "wconfigws");
            String pathName = File.separator + "wconfigws" + File.separator + path1;
            VirtualFile vf = ScratchFileService.getInstance().findFile(ScratchRootType.getInstance(),
                    pathName, ScratchFileService.Option.existing_only);
            // VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(new File(path));
            if (vf == null) {
                logger.info(String.format("根据path: %s 未能找到VirtualFile, fullPath: %s", pathName, path));
                continue;
            }
            FileEditorManager.getInstance(psiFile.getProject()).openFile(vf, true, true);
            return;
        }
    }

    private void showDiff(PsiFile psiFile) throws IOException {
        String configFilePath = psiFile.getVirtualFile().getPath();
        logger.info(String.format("开始showDiff方法打开diff窗口filePath:%s", configFilePath));
        if (!StringUtils.startsWith(configFilePath, LocalStorage.getVirtualWorkspace())) {
            logger.warn(String.format("虚拟文件路径 %s 不是以homeDir %s 开头", configFilePath, LocalStorage.getWorkspace()));
            return;
        }
        CommonComponent.ConfigFileInfo confInfo = CommonComponent.parseFileName(configFilePath);

        String diffFilePath = "wconfigws" + File.separator + CommonComponent.generateFilePathWithoutWs(confInfo.getCluster(),
                confInfo.getGroup(), confInfo.getNamespace(), confInfo.getGrayIp(), confInfo.getContentType(), true);

        CommonComponent.writeConfigContent2File(confInfo.getCluster(), confInfo.getGroup(),
                confInfo.getNamespace(), confInfo.getGrayIp(), diffFilePath, psiFile.getProject());

        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        fileSystem.refreshIoFiles(ImmutableList.of(new File(diffFilePath)));
        VirtualFile configVf = fileSystem.findFileByIoFile(new File(configFilePath));
        // VirtualFile diffConfigVf = fileSystem.findFileByIoFile(new File(diffFilePath));

        // diffFilePath
        VirtualFile diffConfigVf = ScratchFileService.getInstance().findFile(ScratchRootType.getInstance(),
                diffFilePath, ScratchFileService.Option.existing_only);

        if (configVf == null || diffConfigVf == null) {
            logger.warn(String.format("【本地config文件或diff文件未获取到cluster:%s group:%s namespace:%s】", confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace()));
            return;
        }
        CustomDiffWindow diffWindow = new CustomDiffWindow(psiFile.getProject(), configVf, diffConfigVf);
        diffWindow.show();

        // 点击提交按钮的处理逻辑
        diffWindow.onOkAction(e -> {
            try {
                // 保存所有未保存的文件
                commitConfig(e, configVf, diffConfigVf, psiFile, confInfo);
                // 删除临时创建的diff文件
                ApplicationManager.getApplication().runWriteAction((ThrowableComputable<VirtualFile, IOException>) () -> {
                    if (diffConfigVf.isDirectory()) {
                        return null;
                    }
                    diffConfigVf.delete(this);
                    logger.info(String.format("成功删除diff临时文件 %s", diffConfigVf.getPath()));
                    return null;
                });
                diffWindow.close();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        });
        diffWindow.onGrayAction(e -> {
            // 点击创建灰度的处理逻辑
            CreateGrayDialog dialog = new CreateGrayDialog(psiFile.getProject(), ips -> {
                try {
                    List<String> ipList = Arrays.stream(ips.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(ipList)) {
                        logger.warn("创建灰度配置-灰度ip没有填写");
                        return;
                    }
                    CreateGrayResponse response = WConfigCaller.INSTANCE.creatGray(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace(), ipList);
                    String branchName;
                    if (response.getCode() == 200
                            && StringUtils.isNotBlank((branchName = response.getData().getBranchName()))) {
                        // 创建本地灰度配置
                        for (String ip : ipList) {
                            CommonComponent.setGrayIp2GrayBranchName(ip, confInfo);
                            Language language;
                            if (confInfo.getContentType() == ConfigContentType.properties) {
                                language = PropertiesLanguage.INSTANCE;
                            } else {
                                language = Json5Language.INSTANCE;
                            }

                            String grayFilePath = "wconfigws" + File.separator + CommonComponent.generateFilePathWithoutWs(confInfo.getCluster(),
                                    confInfo.getGroup(), confInfo.getNamespace(), ip, confInfo.getContentType(), false);
                            ScratchRootType.getInstance().createScratchFile(psiFile.getProject(), grayFilePath, language,
                                    psiFile.getContainingFile().getText(), ScratchFileService.Option.create_if_missing);
                        }
                        // 发布灰度
                        WConfigCaller.INSTANCE.grayRelease(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace(), branchName);
                        // 通知
                        CommonComponent.notification(true, "灰度创建成功",
                                String.format("<div>%s</div><div>%s</div>", confInfo.getNamespace(), StringUtils.join(ips, ",")),
                                psiFile.getProject());
                    } else {
                        // 通知
                        CommonComponent.notification(false, "灰度创建失败",
                                String.format("<div>%s</div><div>%s</div>", confInfo.getNamespace(), StringUtils.join(ips, ",")),
                                psiFile.getProject());
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            });
            dialog.showAndGet();
            diffWindow.close();
        });
    }

    static class CreateGrayDialog extends DialogWrapper {
        private final Project project;
        private final Consumer<String> ipsConsumer;
        private final JTextField field;

        CreateGrayDialog(Project project, Consumer<String> ipsConsumer) {
            super(project);
            this.project = project;
            this.ipsConsumer = ipsConsumer;
            this.field = new JTextField("", 30);
            init();
        }

        @Override
        protected JComponent createCenterPanel() {
            JPanel jPanel = new JPanel();
            jPanel.add(new JLabel("灰度ip"));
            jPanel.add(field);
            return jPanel;
        }

        @Override
        protected void doOKAction() {
            ipsConsumer.accept(field.getText());
            super.doOKAction();
        }
    }

    private void commitConfig(ActionEvent e, VirtualFile configVf, VirtualFile diffConfigVf,
            PsiFile psiFile, CommonComponent.ConfigFileInfo confInfo) throws IOException {
        FileDocumentManager.getInstance().saveAllDocuments();
        // 逐行比较两个文件的变更内容，将变更的内容取出来提交变更
        String f1 = new String(configVf.contentsToByteArray(), StandardCharsets.UTF_8);
        String f2 = new String(diffConfigVf.contentsToByteArray(), StandardCharsets.UTF_8);
        List<NamespaceContentResponse.DataDTO> data = WConfigCaller.INSTANCE
                .getNamespaceContent(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace())
                .getData();
        // 提交文件，如果是properties拿到变化的key然后只提交变化的key
        if (psiFile instanceof PropertiesFile) {
            Properties p1 = new Properties();
            p1.load(new StringReader(f1));
            Properties p2 = new Properties();
            p2.load(new StringReader(f2));
            Map<String, String> add = new HashMap<>();
            Map<String, String> delete = new HashMap<>();
            Map<String, String> modify = new HashMap<>();
            p2.forEach((k, v) -> {
                if (p1.containsKey(k)) {
                    if (!StringUtils.equals(p1.get(k).toString(), p2.get(k).toString())) {
                        modify.put(k.toString(), p1.get(k).toString());
                    }
                } else {
                    delete.put(k.toString(), v.toString());
                }
            });
            p1.forEach((k, v) -> {
                if (!p2.containsKey(k)) {
                    add.put(k.toString(), v.toString());
                }
            });
            // 提交新增
            add.forEach((k, v) -> WConfigCaller.INSTANCE.addConfig(confInfo.getCluster(), confInfo.getGroup(),
                    confInfo.getNamespace(), k, v));
            // 提交删除
            delete.forEach((k, v) ->
                    data.stream().filter(d -> StringUtils.equals(d.getItemKey(), k))
                            .map(NamespaceContentResponse.DataDTO::getId)
                            .findFirst()
                            .ifPresent(id -> WConfigCaller.INSTANCE.delete(confInfo.getCluster(), id))
            );
            // 如果是灰度编辑，group 需要用灰度名替换, release 也需要替换
            String grayBranchName = CommonComponent.getGrayBranchName(confInfo.getGrayIp(), confInfo.getCluster());
            if (LocalStorage.getSetting().isEnableGray() && StringUtils.isNotBlank(confInfo.getGrayIp())) {
                if (StringUtils.isNotBlank(grayBranchName)) {
                    // 提交修改 & 主干发布
                    if (CollectionUtils.isEmpty(data)) {
                        modify.forEach((k, v) -> WConfigCaller.INSTANCE.postCommitConfig(confInfo.getCluster(), grayBranchName, confInfo.getNamespace(), k, v));
                    } else {
                        modify.forEach((k, v) -> WConfigCaller.INSTANCE.commitConfig(confInfo.getCluster(), grayBranchName, confInfo.getNamespace(), k, v));
                    }

                    Map<String, Object> result = WConfigCaller.INSTANCE.grayRelease(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace(), grayBranchName);
                    if (NumberUtils.toInt(MapUtils.getString(result, "code")) == 200) {
                        CommonComponent.notification(true, "灰度发布成功:" + confInfo.getGrayIp(), String.format("<div>%s</div>", confInfo.getNamespace()), psiFile.getProject());
                    } else {
                        CommonComponent.notification(false, "灰度发布失败:" + confInfo.getGrayIp(), String.format("<div>%s</div><div>%s</div>", confInfo.getNamespace(), JacksonUtil.toJson(result)), psiFile.getProject());
                    }
                }
            } else {
                // 提交修改 & 主干发布
                if (CollectionUtils.isEmpty(data)) {
                    modify.forEach((k, v) -> WConfigCaller.INSTANCE.postCommitConfig(confInfo.getCluster(), grayBranchName, confInfo.getNamespace(), k, v));
                } else {
                    modify.forEach((k, v) -> WConfigCaller.INSTANCE.commitConfig(confInfo.getCluster(), grayBranchName, confInfo.getNamespace(), k, v));
                }
                Map<String, Object> result = WConfigCaller.INSTANCE.releaseMaster(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace());
                if (NumberUtils.toInt(MapUtils.getString(result, "code")) == 200) {
                    CommonComponent.notification(true, "主干发布成功", String.format("<div>%s</div>", confInfo.getNamespace()), psiFile.getProject());
                } else {
                    CommonComponent.notification(false, "主干发布失败", String.format("<div>%s</div><div>%s</div>", confInfo.getNamespace(), JacksonUtil.toJson(result)), psiFile.getProject());
                }
            }
        } else if (psiFile instanceof JsonFile) {
            try {
                JsonFile jsonFile = (JsonFile) psiFile;
                if (jsonFile.getTopLevelValue() instanceof JsonArray) {
                    JacksonUtil.ofJsonCollection(f1, List.class, Object.class);
                } else {
                    JacksonUtil.fromJson(f1);
                }
            } catch (Exception e1) {
                logger.error(e1.getMessage(), e1);
                return;
            }
            // 提交更新 & 主干发布
            // 如果是灰度编辑，group 需要用灰度名替换, release 也需要替换
            if (LocalStorage.getSetting().isEnableGray() && StringUtils.isNotBlank(confInfo.getGrayIp())) {
                String grayBranchName = CommonComponent.getGrayBranchName(confInfo.getGrayIp(), confInfo.getCluster());
                if (StringUtils.isNotBlank(grayBranchName)) {
                    if (CollectionUtils.isEmpty(data)) {
                        WConfigCaller.INSTANCE.postCommitConfig(confInfo.getCluster(), grayBranchName, confInfo.getNamespace(), "default_key", f1);
                    } else {
                        WConfigCaller.INSTANCE.commitConfig(confInfo.getCluster(), grayBranchName, confInfo.getNamespace(), "default_key", f1);
                    }
                    Map<String, Object> result = WConfigCaller.INSTANCE.grayRelease(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace(), grayBranchName);
                    if (NumberUtils.toInt(MapUtils.getString(result, "code")) == 200) {
                        CommonComponent.notification(true, "灰度发布成功:" + confInfo.getGrayIp(), String.format("<div>%s</div>", confInfo.getNamespace()), psiFile.getProject());
                    } else {
                        CommonComponent.notification(false, "灰度发布失败:" + confInfo.getGrayIp(), String.format("<div>%s</div><div>%s</div>", confInfo.getNamespace(), JacksonUtil.toJson(result)), psiFile.getProject());
                    }
                }
            } else {
                if (CollectionUtils.isEmpty(data)) {
                    WConfigCaller.INSTANCE.postCommitConfig(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace(), "default_key", f1);
                } else {
                    WConfigCaller.INSTANCE.commitConfig(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace(), "default_key", f1);
                }
                Map<String, Object> result = WConfigCaller.INSTANCE.releaseMaster(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace());
                if (NumberUtils.toInt(MapUtils.getString(result, "code")) == 200) {
                    CommonComponent.notification(true, "主干发布成功", String.format("<div>%s</div>", confInfo.getNamespace()), psiFile.getProject());
                } else {
                    CommonComponent.notification(false, "主干发布失败", String.format("<div>%s</div><div>%s</div>", confInfo.getNamespace(), JacksonUtil.toJson(result)), psiFile.getProject());
                }
            }
        }
    }

}
