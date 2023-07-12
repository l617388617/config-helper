package com.github.config.helper.views.settings;

import com.github.config.helper.Settings;
import com.github.config.helper.localstorage.LocalStorage;
import com.github.config.helper.service.ApplicationService;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang3.StringUtils;

/**
 * SettingsPanelView
 *
 * @author: lupeng10
 * @create: 2023-05-24 14:19
 */
public class SettingsPanelView {

    private static final Logger logger = Logger.getInstance(SettingsPanelView.class);

    private JPanel main;
    private JTextField cookiesField;
    private JTextField oaNameField;
    private JButton buildIndexButton;
    private JTextField searchKeys;
    private JCheckBox grayEnableCheckBox;
    private JTextField grayIp;
    private JCheckBox enableDefaultGroup;
    private JComboBox<String> defaultGroupComboBox;


    public SettingsPanelView(String cookie, String oaName) {
        cookiesField.setText(cookie);
        oaNameField.setText(oaName);
        buildIndexButton.addActionListener(e -> {
            try {
                buildIndexButton.setEnabled(false);
                new Thread(() -> {
                    logger.info("点击事件==构建索引==start");
                    ApplicationManager.getApplication().getService(ApplicationService.class)
                            .pullAllConfig(LocalStorage.getProject());
                    logger.info("点击事件==构建索引==end");
                }).start();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            } finally {
                buildIndexButton.setEnabled(true);
            }
        });
        // 初始状态不开启灰度
        grayEnableCheckBox.setSelected(false);
        grayIp.setEnabled(false);
        grayEnableCheckBox.addActionListener(e -> grayIp.setEnabled(grayEnableCheckBox.isSelected()));

        // 默认开启defaultGroup = default_group
        enableDefaultGroup.setSelected(true);
        enableDefaultGroup.addActionListener(e -> defaultGroupComboBox.setEnabled(enableDefaultGroup.isSelected()));
        ImmutableList.of("default_group", "sandbox").forEach(defaultGroupComboBox::addItem);
        defaultGroupComboBox.setSelectedItem("default_group");
    }

    public boolean isModified() {
        Settings settings = LocalStorage.getSetting();
        return !StringUtils.equals(cookiesField.getText(), settings.getCookie())
                || !StringUtils.equals(oaNameField.getText(), settings.getOaName())
                || !StringUtils.equals(searchKeys.getText(), settings.getSearchKeys())
                || grayEnableCheckBox.isSelected() != settings.isEnableGray()
                || !StringUtils.equals(grayIp.getText(), settings.getGrayIp())
                || enableDefaultGroup.isSelected() != settings.isEnableDefaultGroup()
                || !StringUtils.equals(this.getGroup(), settings.getDefaultGroup())
                ;
    }

    public void initGroup(TreeSet<String> groups) {
        defaultGroupComboBox.removeAllItems();
        for (String group : groups) {
            defaultGroupComboBox.addItem(group);
        }
    }

    public void setGroup(String group) {
        defaultGroupComboBox.setSelectedItem(group);
    }

    public String getGroup() {
        Object selectedItem = defaultGroupComboBox.getSelectedItem();
        if (selectedItem == null) {
            return "";
        }
        return selectedItem.toString();
    }

    public boolean isEnableDefaultGroup() {
        return enableDefaultGroup.isSelected();
    }

    public void setEnableDefaultGroup(boolean enableDefaultGroup) {
        this.enableDefaultGroup.setSelected(enableDefaultGroup);
    }


    public JPanel getMainComponent() {
        return this.main;
    }

    public boolean isEnableGray() {
        return grayEnableCheckBox.isSelected();
    }

    public void setEnableGray(boolean enableGray) {
        this.grayEnableCheckBox.setSelected(enableGray);
    }

    public String getGrayIp() {
        return grayIp.getText();
    }

    public void setGrayIp(String ip) {
        this.grayIp.setText(ip);
    }

    public String getCookiesValue() {
        return cookiesField.getText();
    }

    public String getOaNameValue() {
        return oaNameField.getText();
    }

    public String getSearchKeys() {
        return searchKeys.getText();
    }

    public void setSearchKeys(String searchKeys) {
        this.searchKeys.setText(searchKeys);
    }

    public void reset() {
        Settings settings = LocalStorage.getSetting();
        cookiesField.setText(settings.getCookie());
        oaNameField.setText(settings.getOaName());
        searchKeys.setText(settings.getSearchKeys());
        grayIp.setText(settings.getGrayIp());
        enableDefaultGroup.setSelected(settings.isEnableDefaultGroup());
        defaultGroupComboBox.setSelectedItem(settings.getDefaultGroup());

        grayEnableCheckBox.setSelected(settings.isEnableGray());
        grayIp.setText(settings.getGrayIp());
    }
}
