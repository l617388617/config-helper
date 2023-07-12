package com.github.config.helper.views;

import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.chains.SimpleDiffRequestChain;
import com.intellij.diff.impl.DiffRequestProcessor;
import com.intellij.diff.impl.DiffWindow;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.diff.util.DiffUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.WindowWrapper;
import com.intellij.openapi.ui.WindowWrapperBuilder;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import java.awt.event.ActionListener;
import org.jetbrains.annotations.Nullable;

/**
 * CustomDiffWindow
 *
 * @author: lupeng10
 * @create: 2023-05-27 20:08
 */
public class CustomDiffWindow extends DiffWindow {

    private static final Logger logger = Logger.getInstance(CustomDiffWindow.class);

    DiffRequestProcessor myProcessor;
    private WindowWrapper myWrapper;
    private VirtualFile vf1;
    private VirtualFile vf2;
    private CustomDiffPanel component;

    public CustomDiffWindow(@Nullable Project project, VirtualFile vf1, VirtualFile vf2) {
        super(project, new SimpleDiffRequestChain(DiffRequestFactory.getInstance().createFromFiles(project, vf1, vf2)), DiffDialogHints.DEFAULT);
        this.vf1 = vf1;
        this.vf2 = vf2;
    }

    public void show() {
        logger.info("调用showDiffDialog方法");
        myProcessor = createProcessor();
        String contextUserData = myProcessor.getContextUserData(DiffUserDataKeys.DIALOG_GROUP_KEY);
        component = new CustomDiffPanel(myProcessor.getComponent());
        myWrapper = new WindowWrapperBuilder(DiffUtil.getWindowMode(myHints), component)
                .setProject(myProject)
                .setParent(myHints.getParent())
                .setDimensionServiceKey(contextUserData)
                .setPreferredFocusedComponent(() -> myProcessor.getPreferredFocusedComponent())
                .setOnShowCallback(() -> myProcessor.updateRequest())
                .build();
        Disposer.register(myWrapper, myProcessor);

        Consumer<WindowWrapper> wrapperHandler = myHints.getWindowConsumer();
        if (wrapperHandler != null) wrapperHandler.consume(myWrapper);

        myWrapper.show();
        logger.info("showDiffDialogEnd");
    }

    @Override
    protected WindowWrapper getWrapper() {
        return myWrapper;
    }

    public void close() {
        getWrapper().close();
    }

    public void onOkAction(ActionListener actionListener) {
        component.onOkAction(actionListener);
    }

    public void onGrayAction(ActionListener actionListener) {
        component.onGrayAction(actionListener);
    }
}
