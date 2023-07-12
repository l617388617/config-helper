package com.github.wconfig.helper.service.analysis;

import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * AnalysisNamespaceFormChain
 *
 * @author: lupeng10
 * @create: 2023-05-28 22:02
 */
public class AnalysisNamespaceFormChain implements AnalysisNamespaceForm {

    private static final AnalysisNamespaceFormChain INSTANCE = new AnalysisNamespaceFormChain();

    private final List<AnalysisNamespaceForm> chain;

    public static AnalysisNamespaceFormChain getInstance() {
        return INSTANCE;
    }

    private AnalysisNamespaceFormChain() {
        this.chain = new ArrayList<>();
        this.chain.add(new AnalysisNamespaceSpringBootForm());
        this.chain.add(new AnalysisNamespaceWConfigSupportForm());
    }

    @Override
    public String analysis(PsiElement element) {
        for (AnalysisNamespaceForm analysisNamespaceForm : chain) {
            String namespace = analysisNamespaceForm.analysis(element);
            if (StringUtils.isNotBlank(namespace)) {
                return namespace;
            }
        }
        return null;
    }

}
