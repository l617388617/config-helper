package com.github.config.helper;

import com.github.config.helper.component.CommonComponent;
import com.github.config.helper.component.ConfigInfo;
import com.github.config.helper.component.ConfigInfoManager;
import com.github.config.helper.component.http.ConfigCall4OpenApi;
import com.github.config.helper.component.http.ItemKeyValueDto;
import com.github.config.helper.component.http.res4openapi.GetMasterRes;
import com.github.config.helper.component.http.res4openapi.NoContentRes;
import com.github.config.helper.component.json.JacksonUtil;
import com.github.config.helper.localstorage.LocalStorage;
import com.github.config.helper.service.analysis.AnalysisNamespaceFormChain;
import com.github.config.helper.views.CreateNamespaceDialog;
import com.github.config.helper.views.CustomDiffWindow;
import com.google.common.collect.ImmutableList;
import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonFile;
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
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * ConfigShowDiffAction
 * 打开diff比较，可以提交
 *
 * @author lupeng10
 * @create 2023-06-11 20:39
 */
public class ConfigShowDiffAction extends AnAction {

    private static final Logger logger = Logger.getInstance(ConfigShowDiffAction.class);

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
                if (StringUtils.isBlank(LocalStorage.getOAName())) {
                    CommonComponent.notification(false, "OAName未配置", d.getCluster(), psiFile.getProject());
                    return;
                }
                String key = LocalStorage.getClusterKeyByName(d.getCluster());
                if (StringUtils.isBlank(key)) {
                    CommonComponent.notification(false, "未找到ClusterKey", d.getCluster(), psiFile.getProject());
                    return;
                }
                ConfigCall4OpenApi configCall4OpenApi = ConfigCall4OpenApi.getInstance();
                GetMasterRes res = configCall4OpenApi.getMaster(key, d.getCluster(), d.getGroup(), d.getNamespace());
                if (CollectionUtils.isNotEmpty(res.getData())) {
                    CommonComponent.notification(false, "配置已存在", d.getNamespace(), psiFile.getProject());
                    return;
                }
                ConfigInfo.ConfigInfoBuilder builder = ConfigInfo.builder();
                if (StringUtils.equals(d.getFormat(), CommonComponent.PROPERTIES)) {
                    builder.clusterName(d.getCluster())
                            .group(d.getGroup())
                            .namespace(d.getNamespace())
                            .format("properties");
                } else {
                    builder.clusterName(d.getCluster())
                            .group(d.getGroup())
                            .namespace(d.getNamespace())
                            .format("txt");
                }

                NoContentRes createRes = configCall4OpenApi.createOrUpdateMaster(key, d.getCluster(), d.getGroup(), d.getNamespace(), LocalStorage.getOAName(), Collections.emptyList());
                if (createRes.getCode() == 200) {
                    VirtualFile vf = ConfigInfoManager.getInstance().generateVirtualFile(psiFile.getProject(), builder.build());
                    FileEditorManager.getInstance(psiFile.getProject()).openFile(vf, true, true);
                    // 通知
                    CommonComponent.notification(true, "创建成功",
                            String.format("<div>%s</div><div>%s</div><div>%s</div>", d.getCluster(), d.getGroup(), namespace),
                            psiFile.getProject());
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
        Settings setting = LocalStorage.getSetting();
        ConfigInfoManager configInfoManager = ConfigInfoManager.getInstance();
        Set<String> clusterNameSet = LocalStorage.getClusterNameSet();
        Set<String> groupSet = LocalStorage.getGroupSet();
        String defaultGroup = LocalStorage.getDefaultGroup();

        ConfigInfo configInfo = configInfoManager.getConfigInfoByNamespace(namespace, c -> {
            if (!clusterNameSet.contains(c.getClusterName())) {
                return false;
            }
            if (StringUtils.isNotBlank(defaultGroup) && !StringUtils.equals(c.getGroup(), defaultGroup)) {
                return false;
            }
            if (!groupSet.contains(c.getGroup())) {
                return false;
            }
            if (setting.isEnableGray()) {
                if (c.isMaster()) {
                    return false;
                }
                if (StringUtils.isNotBlank(setting.getGrayIp()) &&
                        !setting.getGrayIp().contains(setting.getGrayIp())) {
                    return false;
                }
            }
            return true;
        }).orElse(null);

        if (configInfo == null) {
            showCreateNamespaceDialog(psiFile, namespace);
            logger.info("根据namespace未找到configInfo");
            return;
        }
        VirtualFile virtualFile = configInfoManager.generateVirtualFile(project, configInfo);
        FileEditorManager.getInstance(psiFile.getProject()).openFile(virtualFile, true, true);
    }

    private void showDiff(PsiFile psiFile) throws IOException {
        String configFilePath = psiFile.getVirtualFile().getPath();
        logger.info(String.format("开始showDiff方法打开diff窗口filePath:%s", configFilePath));

        ConfigInfoManager configInfoManager = ConfigInfoManager.getInstance();
        ConfigInfo configInfo = configInfoManager.parseFileName(configFilePath);
        configInfo.setMaster(true);
        configInfo.setClusterKey(LocalStorage.getClusterKeyByName(configInfo.getClusterName()));
        VirtualFile diffConfigVf = ConfigInfoManager.getInstance().generateVirtualFile(psiFile.getProject(), configInfo, null, true);

        LocalFileSystem fileSystem = LocalFileSystem.getInstance();
        VirtualFile configVf = fileSystem.findFileByIoFile(new File(configFilePath));

        if (configVf == null || diffConfigVf == null) {
            logger.warn(String.format("【本地config文件或diff文件未获取到cluster:%s group:%s namespace:%s】", configInfo.getClusterName(),
                    configInfo.getGroup(), configInfo.getNamespace()));
            return;
        }
        CustomDiffWindow diffWindow = new CustomDiffWindow(psiFile.getProject(), configVf, diffConfigVf);
        diffWindow.show();

        // 点击提交按钮的处理逻辑
        diffWindow.onOkAction(e -> {
            try {
                if (!StringUtils.equals(configInfo.getGroup(), "default_group")) {
                    // todo 复制到剪切板
                    diffWindow.close();
                }

                // 保存所有未保存的文件
                commitConfig(e, configVf, diffConfigVf, psiFile, configInfo);
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
        // diffWindow.onGrayAction(e -> {
        //     // 点击创建灰度的处理逻辑
        //     CreateGrayDialog dialog = new CreateGrayDialog(psiFile.getProject(), ips -> {
        //         try {
        //             List<String> ipList = Arrays.stream(ips.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        //             if (CollectionUtils.isEmpty(ipList)) {
        //                 logger.warn("创建灰度配置-灰度ip没有填写");
        //                 return;
        //             }
        //             CreateGrayResponse response = ConfigCaller.INSTANCE.creatGray(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace(), ipList);
        //             String branchName;
        //             if (response.getCode() == 200
        //                     && StringUtils.isNotBlank((branchName = response.getData().getBranchName()))) {
        //                 // 创建本地灰度配置
        //                 for (String ip : ipList) {
        //                     CommonComponent.setGrayIp2GrayBranchName(ip, confInfo);
        //                     Language language;
        //                     if (confInfo.getContentType() == ConfigContentType.properties) {
        //                         language = PropertiesLanguage.INSTANCE;
        //                     } else {
        //                         language = Json5Language.INSTANCE;
        //                     }
        //
        //                     String grayFilePath = "wconfigws" + File.separator + CommonComponent.generateFilePathWithoutWs(confInfo.getCluster(),
        //                             confInfo.getGroup(), confInfo.getNamespace(), ip, confInfo.getContentType(), false);
        //                     ScratchRootType.getInstance().createScratchFile(psiFile.getProject(), grayFilePath, language,
        //                             psiFile.getContainingFile().getText(), ScratchFileService.Option.create_if_missing);
        //                 }
        //                 // 发布灰度
        //                 ConfigCaller.INSTANCE.grayRelease(confInfo.getCluster(), confInfo.getGroup(), confInfo.getNamespace(), branchName);
        //                 // 通知
        //                 CommonComponent.notification(true, "灰度创建成功",
        //                         String.format("<div>%s</div><div>%s</div>", confInfo.getNamespace(), StringUtils.join(ips, ",")),
        //                         psiFile.getProject());
        //             } else {
        //                 // 通知
        //                 CommonComponent.notification(false, "灰度创建失败",
        //                         String.format("<div>%s</div><div>%s</div>", confInfo.getNamespace(), StringUtils.join(ips, ",")),
        //                         psiFile.getProject());
        //             }
        //         } catch (Exception ex) {
        //             logger.error(ex.getMessage(), ex);
        //         }
        //     });
        //     dialog.showAndGet();
        //     diffWindow.close();
        // });
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
            PsiFile psiFile, ConfigInfo confInfo) throws IOException {
        FileDocumentManager.getInstance().saveAllDocuments();
        // 逐行比较两个文件的变更内容，将变更的内容取出来提交变更
        String f1 = new String(configVf.contentsToByteArray(), StandardCharsets.UTF_8);
        String f2 = new String(diffConfigVf.contentsToByteArray(), StandardCharsets.UTF_8);
        String key = LocalStorage.getClusterKeyByName(confInfo.getClusterName());
        GetMasterRes masterRes = ConfigCall4OpenApi.getInstance().getMaster(key, confInfo.getClusterName(), confInfo.getGroup(), confInfo.getNamespace());
        List<ItemKeyValueDto> data = masterRes.getData();
        // 提交文件，如果是properties拿到变化的key然后只提交变化的key
        if (psiFile instanceof PropertiesFile) {
            Properties p1 = new Properties();
            p1.load(new StringReader(f1));
            List<ItemKeyValueDto> valueDtoList = p1.entrySet().stream().map(en -> {
                ItemKeyValueDto itemKeyValueDto = new ItemKeyValueDto();
                itemKeyValueDto.setItemKey(en.getKey().toString());
                itemKeyValueDto.setItemValue(en.getValue().toString());
                return itemKeyValueDto;
            }).collect(Collectors.toList());
            NoContentRes orUpdateMaster = ConfigCall4OpenApi.getInstance().createOrUpdateMaster(key, confInfo.getClusterName(), confInfo.getGroup(), confInfo.getNamespace(),
                    LocalStorage.getOAName(), valueDtoList);
            if (orUpdateMaster.getCode() == 200) {
                CommonComponent.notification(true, "主干发布成功", String.format("<div>%s</div>", confInfo.getNamespace()), psiFile.getProject());
            } else {
                CommonComponent.notification(false, "主干发布失败", String.format("<div>%s</div><div>%s</div>", confInfo.getNamespace(), JacksonUtil.toJson(orUpdateMaster)), psiFile.getProject());
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

            NoContentRes orUpdateMaster = ConfigCall4OpenApi.getInstance()
                    .createOrUpdateMaster(key, confInfo.getClusterName(), confInfo.getGroup(), confInfo.getNamespace(),
                            LocalStorage.getOAName(), ImmutableList.of(new ItemKeyValueDto("default_key", f1)));
            if (orUpdateMaster.getCode() == 200) {
                CommonComponent.notification(true, "主干发布成功", String.format("<div>%s</div>", confInfo.getNamespace()), psiFile.getProject());
            } else {
                CommonComponent.notification(false, "主干发布失败", String.format("<div>%s</div><div>%s</div>", confInfo.getNamespace(), JacksonUtil.toJson(orUpdateMaster)), psiFile.getProject());
            }
        }
    }

}
