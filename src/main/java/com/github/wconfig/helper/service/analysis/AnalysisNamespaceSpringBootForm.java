package com.github.wconfig.helper.service.analysis;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * AnalysisNamespaceSpringBootForm
 *
 * @author: lupeng10
 * @create: 2023-05-28 22:08
 */
public class AnalysisNamespaceSpringBootForm implements AnalysisNamespaceForm {

    private static final Logger logger = Logger.getInstance(AnalysisNamespaceSpringBootForm.class);

    private static final Pattern PATTERN = Pattern.compile("^\"[\\da-zA-Z-_/]*\"$");

    @Override
    public String analysis(PsiElement element) {
        String namespace = null;
        try {
            int count = 100;
            while (element != null) {
                if (StringUtils.isBlank(namespace) && PATTERN.matcher(element.getText()).find()) {
                    namespace = StringUtils.trim(StringUtils.replace(element.getText(), "\"", ""));
                }
                if (StringUtils.contains(element.getText(), "@WConfig")) {
                    return namespace;
                }
                element = element.getParent();
                count--;
                if (count <= 0) {
                    return null;
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return namespace;
    }
}
