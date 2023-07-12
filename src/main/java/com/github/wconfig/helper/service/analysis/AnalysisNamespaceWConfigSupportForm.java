package com.github.wconfig.helper.service.analysis;

import com.github.wconfig.helper.localstorage.LocalStorage;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceExpression;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * AnalysisNamespaceWConfigSupportForm
 *
 * @author lupeng10
 * @create 2023-06-21 16:28
 */
public class AnalysisNamespaceWConfigSupportForm implements AnalysisNamespaceForm {

    private static final Logger logger = Logger.getInstance(AnalysisNamespaceWConfigSupportForm.class);

    private static final Pattern PATTERN = Pattern.compile("^\"[\\da-zA-Z-_/]*\"$");

    @Override
    public String analysis(PsiElement element) {
        String namespace = null;
        List<String> keys = Lists.newArrayList();
        keys.add("WConfigSupport");
        keys.add("WConfigParseUtil");
        Arrays.stream(Strings.nullToEmpty(LocalStorage.getSearchKeys()).split(","))
                .filter(StringUtils::isNotBlank)
                .forEach(keys::add);
        try {
            int count = 100;
            while (element != null) {
                if (element instanceof PsiReferenceExpression) {
                    PsiElement resolve = ((PsiReferenceExpression) element).resolve();
                    if (resolve != null) {
                        for (PsiElement child : resolve.getChildren()) {
                            if (child instanceof PsiLiteralExpression) {
                                namespace = StringUtils.trim(StringUtils.replace(child.getText(), "\"", ""));
                                break;
                            }
                        }
                    }
                }

                String elementText = element.getText();
                if (StringUtils.isBlank(namespace) && PATTERN.matcher(elementText).find()) {
                    namespace = StringUtils.trim(StringUtils.replace(elementText, "\"", ""));
                }
                if (keys.stream().anyMatch(key -> StringUtils.contains(elementText, key))) {
                    return namespace;
                }
                element = element.getParent();
                count--;
                if (count <= 0) {
                    return null;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return namespace;
    }
}
