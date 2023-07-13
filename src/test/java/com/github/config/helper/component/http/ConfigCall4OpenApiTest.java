package com.github.config.helper.component.http;

import com.github.config.helper.component.http.res4openapi.GetGrayListRes;
import com.github.config.helper.component.http.res4openapi.GetNamespaceListRes;
import com.github.config.helper.component.json.JacksonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ConfigCall4OpenApiTest
 *
 * @author lupeng10
 * @create 2023-07-13 16:05
 */
public class ConfigCall4OpenApiTest {

    public static final String clusterKey = "8ce6397b5a6a24400a288b40693056d5";
    public static final String clusterName = "zhaopin_web_instructorapi";
    public static final String groupName = "sandbox";
    public static final String namespaceName = "position_commerce_advise_raw_item_config";

    private ConfigCall4OpenApi instance;

    @BeforeEach
    public void beforeEach() {
        this.instance = ConfigCall4OpenApi.getInstance();
    }

    @Test
    public void getNamespaceListTest() {
        GetNamespaceListRes namespaceList = instance.getNamespaceList(clusterKey, clusterName, groupName, 1, 200);
        System.out.println(JacksonUtil.toJson(namespaceList));
        Assertions.assertTrue(namespaceList.getCode() == 200
                && CollectionUtils.isNotEmpty(namespaceList.getData().getRecords()), "获取Namespace列表失败");
    }

    @Test
    public void getGrayListTest() {
        GetGrayListRes grayListRes = instance.getGrayList(clusterKey, clusterName, groupName, namespaceName, 1, 200);
        System.out.println(JacksonUtil.toJson(grayListRes));
        Assertions.assertTrue(grayListRes.getCode() == 200
                && CollectionUtils.isNotEmpty(grayListRes.getData().getRecords()), "获取Namespace列表失败");
    }


}
