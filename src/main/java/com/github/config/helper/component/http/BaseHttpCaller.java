package com.github.config.helper.component.http;

import com.github.config.helper.component.json.JacksonUtil;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.collections.MapUtils;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * BaseHttpCaller
 *
 * @author: lupeng10
 * @create: 2023-05-23 18:13
 */
public abstract class BaseHttpCaller {

    private static final Logger log = Logger.getInstance(BaseHttpCaller.class);

    protected static final CloseableHttpClient HTTP_CLIENT;

    static {
        RequestConfig requestConfig = RequestConfig.custom()
                // 从连接池获取可用连接的超时时间，单位毫秒
                .setConnectionRequestTimeout(10000, TimeUnit.MILLISECONDS)
                // 链接超时时间
                .setConnectTimeout(5000, TimeUnit.MILLISECONDS)
                // 响应超时时间
                .setResponseTimeout(5000, TimeUnit.MILLISECONDS)
                .build();

        HTTP_CLIENT = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    protected abstract Map<String, Object> getHttpHeaders();


    public String get(String url, Map<String, String> params) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            for (Map.Entry<String, Object> entry : getHttpHeaders().entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue());
            }
            CloseableHttpResponse response = HTTP_CLIENT.execute(httpGet);
            print(httpGet, response);
            int code = response.getCode();
            if (code >= 200 && code < 300) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public String put(String url, Map<String, String> params, Map<String, Object> jsonBody) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            HttpPut httpPut = new HttpPut(uriBuilder.build());
            for (Map.Entry<String, Object> entry : getHttpHeaders().entrySet()) {
                httpPut.addHeader(entry.getKey(), entry.getValue());
            }
            httpPut.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
            if (MapUtils.isNotEmpty(jsonBody)) {
                httpPut.setEntity(new StringEntity(JacksonUtil.toJson(jsonBody), ContentType.APPLICATION_JSON));
            }
            CloseableHttpResponse response = HTTP_CLIENT.execute(httpPut);
            print(httpPut, response);
            int code = response.getCode();
            if (code >= 200 && code < 300) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    public String post(String url, Map<String, String> params, Map<String, Object> jsonBody) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            HttpPost httpPost = new HttpPost(uriBuilder.build());
            for (Map.Entry<String, Object> entry : getHttpHeaders().entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }
            httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
            if (MapUtils.isNotEmpty(jsonBody)) {
                httpPost.setEntity(new StringEntity(JacksonUtil.toJson(jsonBody), ContentType.APPLICATION_JSON));
            }
            CloseableHttpResponse response = HTTP_CLIENT.execute(httpPost);
            print(httpPost, response);
            int code = response.getCode();
            if (code >= 200 && code < 300) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public String delete(String url, Map<String, String> params) {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
            HttpDelete httpDelete = new HttpDelete(uriBuilder.toString());
            for (Map.Entry<String, Object> entry : getHttpHeaders().entrySet()) {
                httpDelete.addHeader(entry.getKey(), entry.getValue());
            }
            httpDelete.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
            CloseableHttpResponse response = HTTP_CLIENT.execute(httpDelete);
            print(httpDelete, response);
            int code = response.getCode();
            if (code >= 200 && code < 300) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private void print(HttpUriRequestBase requestBase, CloseableHttpResponse response) {
        log.info(requestBase.toString());
        log.info(response.getReasonPhrase());
    }
}
