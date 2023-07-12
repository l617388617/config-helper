package com.github.config.helper.views;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.util.List;

/**
 * CreateNamespaceComponent
 *
 * @author lupeng10
 * @create 2023-07-03 15:08
 */
public class CreateNamespaceComponent {
    private JPanel main;
    JComboBox<String> cluster;
    private JComboBox<String> group;
    private JTextField namespace;
    private JComboBox<String> format;
    private JTextArea desc;
    private JLabel error;

    public JPanel getMain() {
        return this.main;
    }

    public String getNamespace() {
        return namespace.getText();
    }

    public String getDesc() {
        return desc.getText();
    }

    public void setClusterList(List<String> clusterList, String defaultSelect) {
        cluster.removeAllItems();
        for (String s : clusterList) {
            cluster.addItem(s);
        }
        cluster.setSelectedItem(defaultSelect);
    }

    public String getCluster() {
        if (cluster.getSelectedItem() == null) {
            return "";
        }
        return cluster.getSelectedItem().toString();
    }

    public void setGroupList(List<String> groupList, String defaultSelect) {
        group.removeAllItems();
        for (String s : groupList) {
            group.addItem(s);
        }
        group.setSelectedItem(defaultSelect);
    }

    public String getGroup() {
        if (group.getSelectedItem() == null) {
            return "";
        }
        return group.getSelectedItem().toString();
    }

    public void setFormatList(List<String> formatList, String defaultSelect) {
        format.removeAllItems();
        for (String s : formatList) {
            format.addItem(s);
        }
        format.setSelectedItem(defaultSelect);
    }

    public String getFormat() {
        if (format.getSelectedItem() == null) {
            return "";
        }
        return format.getSelectedItem().toString();
    }

    public void setNamespace(String namespace) {
        this.namespace.setText(namespace);
    }

    public void setErrorMsg(String msg) {
        error.setForeground(JBColor.RED);
        error.setText(msg);
    }

}
