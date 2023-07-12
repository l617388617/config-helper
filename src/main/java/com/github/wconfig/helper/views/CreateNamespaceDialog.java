package com.github.wconfig.helper.views;

import com.github.wconfig.helper.component.CommonComponent;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * CreateNamespaceDialog
 *
 * @author lupeng10
 * @create 2023-07-03 15:30
 */
public class CreateNamespaceDialog extends DialogWrapper {

    private final CreateNamespaceComponent panel;
    private final Consumer<CreateNamespaceDto> consumer;

    public CreateNamespaceDialog(Project project, String namespace, Consumer<CreateNamespaceDto> consumer) {
        super(project);
        this.consumer = consumer;
        this.panel = new CreateNamespaceComponent();
        panel.setNamespace(namespace);
        panel.setClusterList(new ArrayList<>(CommonComponent.cluster2GroupsMap.keySet()),
                CommonComponent.cluster2GroupsMap.keySet().iterator().next());
        panel.cluster.addActionListener(e -> {
            if (panel.cluster.getSelectedItem() != null) {
                TreeSet<String> groups = CommonComponent.cluster2GroupsMap.get(panel.cluster.getSelectedItem().toString());
                panel.setGroupList(new ArrayList<>(groups), groups.iterator().next());
            }
        });
        panel.setFormatList(ImmutableList.of(CommonComponent.JSON_5, CommonComponent.PROPERTIES), CommonComponent.JSON_5);
        init();
        setTitle("创建Namespace");
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel.getMain();
    }

    @Override
    protected void doOKAction() {
        CreateNamespaceDto.CreateNamespaceDtoBuilder builder = CreateNamespaceDto.builder();
        // check
        if (StringUtils.isBlank(panel.getCluster())) {
            panel.setErrorMsg("请选择集群名");
            return;
        }
        builder.cluster(panel.getCluster());
        if (StringUtils.isBlank(panel.getGroup())) {
            panel.setErrorMsg("请选择分组");
            return;
        }
        builder.group(panel.getGroup());
        if (StringUtils.isBlank(panel.getNamespace())) {
            panel.setErrorMsg("请输入Namespace");
            return;
        }
        builder.namespace(panel.getNamespace());
        if (StringUtils.isBlank(panel.getFormat())) {
            panel.setErrorMsg("请选择格式");
            return;
        }
        builder.format(panel.getFormat());
        if (StringUtils.isBlank(panel.getDesc())) {
            panel.setErrorMsg("请输入描述");
            return;
        }
        builder.desc(panel.getDesc());

        consumer.accept(builder.build());
        super.doOKAction();
    }

    @Data
    @Builder
    public static class CreateNamespaceDto {
        String cluster;
        String group;
        String namespace;
        String format;
        String desc;
    }
}
