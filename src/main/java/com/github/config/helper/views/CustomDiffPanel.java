package com.github.config.helper.views;

import com.intellij.diff.util.DiffUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

/**
 * CustomDiffPanel
 *
 * @author lupeng10
 * @create 2023-05-27 20:16
 */
public class CustomDiffPanel extends JPanel {

    private final JButton okButton;
    private final JButton grayButton;

    public CustomDiffPanel(@NotNull JComponent content) {
        super(new BorderLayout());
        add(content, BorderLayout.CENTER);

        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEADING,20,5));
        grayButton = new JButton("创建灰度");
        okButton = new JButton("提交配置");
        jPanel.add(grayButton);
        jPanel.add(okButton);

        JPanel jPanel1 = new JPanel(new BorderLayout());
        jPanel1.add(jPanel, BorderLayout.EAST);

        add(jPanel1, BorderLayout.PAGE_END);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension windowSize = DiffUtil.getDefaultDiffWindowSize();
        Dimension size = super.getPreferredSize();
        return new Dimension(Math.max(windowSize.width, size.width), Math.max(windowSize.height, size.height));
    }

    public void onOkAction(ActionListener actionListener) {
        okButton.addActionListener(actionListener);
    }

    public void onGrayAction(ActionListener actionListener) {
        this.grayButton.addActionListener(actionListener);
    }

}
