package com.github.config.helper.component.http;

import com.github.config.helper.component.http.res4openapi.GetGrayListRes;
import com.github.config.helper.component.http.res4openapi.GetMasterRes;
import com.github.config.helper.component.http.res4openapi.GetNamespaceListRes;
import com.github.config.helper.component.http.res4openapi.NoContentRes;
import com.github.config.helper.component.json.JacksonUtil;
import com.intellij.openapi.diagnostic.Logger;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ConfigCall4OpenApi
 *
 * @author lupeng10
 * @create 2023-07-12 14:40
 */
public class ConfigCall4OpenApi extends BaseHttpCaller {

    private static final Logger log = Logger.getInstance(BaseHttpCaller.class);

    private static volatile ConfigCall4OpenApi instance;

    public static ConfigCall4OpenApi getInstance() {
        if (instance == null) {
            synchronized (ConfigCall4OpenApi.class) {
                if (instance == null) {
                    instance = new ConfigCall4OpenApi();
                }
            }
        }
        return instance;
    }

    @Override
    protected Map<String, Object> getHttpHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        return headers;
    }


    public GetNamespaceListRes getNamespaceList(String clusterKey, String clusterName, String groupName,
            int currentPage, int pageSize) {
        String url = "https://portal-wconfig.58corp.com/api/namespace/page";
        Map<String, String> params = new HashMap<>();
        params.put("clusterKey", clusterKey);
        params.put("clusterName", clusterName);
        params.put("groupName", groupName);
        params.put("currentPage", String.valueOf(currentPage));
        params.put("pageSize", String.valueOf(pageSize));
        String resStr = super.get(url, params);
        log.info("【getNamespaceList】response: " + resStr);
        return JacksonUtil.fromJson(resStr, GetNamespaceListRes.class);
    }


    public GetGrayListRes getGrayList(String clusterKey, String clusterName, String groupName, String namespaceName,
            int currentPage, int pageSize) {
        String url = "https://portal-wconfig.58corp.com/api/grayscale/page";
        Map<String, String> params = new HashMap<>();
        params.put("clusterKey", clusterKey);
        params.put("clusterName", clusterName);
        params.put("groupName", groupName);
        params.put("namespaceName", namespaceName);
        params.put("currentPage", String.valueOf(currentPage));
        params.put("pageSize", String.valueOf(pageSize));
        String resStr = super.get(url, params);
        log.info("【getGrayList】response: " + resStr);
        return JacksonUtil.fromJson(resStr, GetGrayListRes.class);
    }

    public NoContentRes createGray(String clusterKey, String clusterName, String groupName, String namespaceName,
            String ip, String operator) {
        String url = "https://portal-wconfig.58corp.com/api/grayscale";
        Map<String, Object> params = new HashMap<>();
        params.put("clusterKey", clusterKey);
        params.put("clusterName", clusterName);
        params.put("groupName", groupName);
        params.put("namespaceName", namespaceName);

        String comment = generateComment(operator);
        Map<String, Object> ruleRequestDTO = new HashMap<>();
        ruleRequestDTO.put("name", comment);
        ruleRequestDTO.put("comment", comment);
        ruleRequestDTO.put("ips", Arrays.asList(ip));
        params.put("ruleRequestDTO", ruleRequestDTO);
        params.put("operator", operator);

        String resStr = super.post(url, null, params);
        log.info("【createGray】response: " + resStr);
        return JacksonUtil.fromJson(resStr, NoContentRes.class);
    }

    public NoContentRes updateGray(String clusterKey, String clusterName, String groupName, String namespaceName,
            String grayVersion, String operator, List<ItemKeyValueDto> keyValueDtoList) {
        String url = "https://portal-wconfig.58corp.com/api/grayconfig";
        Map<String, Object> params = new HashMap<>();
        params.put("clusterKey", clusterKey);
        params.put("clusterName", clusterName);
        params.put("groupName", groupName);
        params.put("namespaceName", namespaceName);
        params.put("grayVersion", grayVersion);
        params.put("itemRequestDTOList", keyValueDtoList);
        params.put("releaseComment", generateComment(operator));
        params.put("operator", operator);
        String resStr = super.post(url, null, params);
        log.info("【updateGray】response: " + resStr);
        return JacksonUtil.fromJson(resStr, NoContentRes.class);
    }

    public GetMasterRes getMaster(String clusterKey, String clusterName, String groupName, String namespaceName) {
        String url = "https://portal-wconfig.58corp.com/api/namespace/item/master";
        Map<String, String> params = new HashMap<>();
        params.put("clusterKey", clusterKey);
        params.put("clusterName", clusterName);
        params.put("groupName", groupName);
        params.put("namespaceName", namespaceName);
        String resStr = super.get(url, params);
        log.info("【getMaster】response: " + resStr);
        return JacksonUtil.fromJson(resStr, GetMasterRes.class);
    }

    public NoContentRes createOrUpdateMaster(String clusterKey, String clusterName, String groupName,
            String namespaceName, String operator, List<ItemKeyValueDto> keyValueDtoList) {
        String url = "https://portal-wconfig.58corp.com/api/namespace/item/master";
        Map<String, Object> params = new HashMap<>();
        params.put("clusterKey", clusterKey);
        params.put("clusterName", clusterName);
        params.put("groupName", groupName);
        params.put("namespaceName", namespaceName);
        params.put("itemRequestDTOList", keyValueDtoList);
        params.put("releaseComment", generateComment(operator));
        params.put("operator", operator);
        String resStr = super.post(url, null, params);
        log.info("【updateGray】response: " + resStr);
        return JacksonUtil.fromJson(resStr, NoContentRes.class);
    }


    private String generateComment(String operator) {
        return operator + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }


}
