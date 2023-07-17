package com.github.config.helper.component.http;

import com.github.config.helper.component.http.res.ClusterListResponse;
import com.github.config.helper.component.http.res.CreateGrayResponse;
import com.github.config.helper.component.http.res.CreateNamespaceResponse;
import com.github.config.helper.component.http.res.GrayIpListResponse;
import com.github.config.helper.component.http.res.GroupListResponse;
import com.github.config.helper.component.http.res.NamespaceContentResponse;
import com.github.config.helper.component.http.res.NamespaceListResponse;
import com.github.config.helper.component.json.JacksonUtil;
import com.github.config.helper.localstorage.LocalStorage;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.diagnostic.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * WConfigCaller
 *
 * @author lupeng10
 * @create 2023-05-23 18:17
 */
// public class ConfigCaller extends BaseHttpCaller {
class ConfigCaller extends BaseHttpCaller {

    private static final Logger log = Logger.getInstance(BaseHttpCaller.class);

    public static final ConfigCaller INSTANCE = new ConfigCaller();

    @Override
    protected Map<String, Object> getHttpHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Language", "zh-CN,zh;q=0.9");
        headers.put("Cache-Control", "no-cache");
        headers.put("Cookie", getCookies());
        headers.put("Pragma", "no-cache");
        headers.put("Referer", "https://portal-wconfig.58corp.com/");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36");
        headers.put("sec-ch-ua", "\"Google Chrome\";v=\"113\", \"Chromium\";v=\"113\", \"Not-A.Brand\";v=\"24\"");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("sec-ch-ua-platform", "macOS");
        return headers;
    }

    private String getCookies() {
        return LocalStorage.getCookies();
    }

    private String getOaName() {
        return Strings.nullToEmpty(LocalStorage.getSetting().getOaName());
    }

    public ClusterListResponse getClusterList(int currPage) {
        String url = "https://portal-wconfig.58corp.com/guest/cluster/page";
        Map<String, String> params = new HashMap<>();
        params.put("env", "ONLINE");
        params.put("ownerName", getOaName());
        params.put("currentPage", String.valueOf(currPage));
        params.put("pageSize", "100");
        String res = super.get(url, params);
        return JacksonUtil.fromJson(res, ClusterListResponse.class);
    }


    public GroupListResponse getGroupList(String clusterName) {
        // https://portal-wconfig.58corp.com/guest/group?clusterName=zhaopin_web_instructorapi&env=ONLINE
        String url = "https://portal-wconfig.58corp.com/guest/group";
        ImmutableMap<String, String> params = ImmutableMap.of("clusterName", clusterName, "env", "ONLINE");
        return JacksonUtil.fromJson(super.get(url, params), GroupListResponse.class);
    }

    public NamespaceListResponse getNamespaceList(int currPage, String clusterName, String group) {
        // https://portal-wconfig.58corp.com/owner/namespace/page?env=ONLINE&clusterName=zhaopin_web_instructorapi&groupName=default_group&currentPage=1&pageSize=10
        String url = "https://portal-wconfig.58corp.com/owner/namespace/page";
        ImmutableMap<String, String> params = ImmutableMap.of("env", "ONLINE", "clusterName", clusterName,
                "groupName", group, "currentPage", String.valueOf(currPage), "pageSize", "10");
        return JacksonUtil.fromJson(super.get(url, params), NamespaceListResponse.class);
    }

    public NamespaceContentResponse getNamespaceContent(String cluster, String group, String namespace) {
        // curl 'https://portal-wconfig.58corp.com/guest/item/list?env=ONLINE&clusterName=zhaopin_web_instructorapi&groupName=20230701152540-9f7206b1de56a7da&namespaceName=position_commerce_advise_raw_item_config' \
        //         -H 'Accept: application/json, text/plain, */*' \
        //         -H 'Accept-Language: zh-CN,zh;q=0.9' \
        //         -H 'Cache-Control: no-cache' \
        //         -H 'Connection: keep-alive' \
        //         -H 'Cookie: ec=5A2whgeS-1680574542987-d18a5a021b7bb-173435817; _bu=20230403110150143b1e59; 58tj_uuid=58572d04-2055-4810-a7e1-21050e369227; wmda_uuid=0dbc649e747c3225181a2158492c5ef0; wmda_new_uuid=1; wmda_visited_projects=%3B18101072869043; new_uv=52; sso_ticket=ST-197430-wWP5YUnk4aAhSyb6NrBb-passport-58corp-com; ishare_sso_username=842489FD39F555DE3AFF25E2257A582DCDA68951F9774499; dunCookie=8ad2858279ac7b18564b708f7be32829ab745524706f6038798760a5088baf67c22dc82ce5d3764d; TGC=eyJhbGciOiJIUzUxMiJ9.ZXlKaGJHY2lPaUprYVhJaUxDSmxibU1pT2lKQk1USTRRMEpETFVoVE1qVTJJbjAuLmhFVlNsaDRONUpJLXVGZlUzanpIWncuOHRiS0k2S1pFaHdGS05rNFRfemNWd3pOQWxRb3ZaN0IxUzBrVEE5QXQ4aktFX1ZwSFVCRThYNDB3R0Jjd3ZHUTVsQjhpeG9ISDJuM1pyNWFySDVjcEVOVVZhekhoZzZKQ2JhM2l4dGREc2x2ZmYtTGRHLUdkNnBFTkxOd0drN0xyejBmenFpWmlLbWJCSmdxelo1NGFnLmNpLVhyRGFLOEQ0UmQtTDVtNE1zVEE.Pk7zRnjRLrfFsrQshcHRdpeGljVMATv8NM5n2c6p52r-Errw0x5rPXkC_Q6Kvy_7ndAcsrAa_xcuuOQK5azQAg; _efmdata=p%2Fk7aci7jG%2BavPpIW3wFYhD96QaGhxJnqaquZi1NbCMsrvVzHFtlVGp9%2FPrK7cv%2FnkXLNssgHPSSrkY5ONmeaKU%2BZBVmJqjEIWWBciCLtcM%3D; _exid=bWFrU7VjX%2BanYMBJ7li9g5cl8jH8f3muq7mZFbWtjPTMf2qwZF8lAI1jAziJ2Vz%2BU3xu5GmxF6m329cZAAY99Q%3D%3D' \
        //         -H 'Pragma: no-cache' \
        //         -H 'Referer: https://portal-wconfig.58corp.com/?blackbox=tdfpeyJ2IjoiWCtnWmJlWDErQzRsWjJGL05oTGJDWklmM2JsM09QRXgxSEF2MnAwS0lWbE4yMjNPcDNrOGs1Z3B0WDlDTlpybyIsIm9zIjozLCJ0IjoiM1dQVjE2ODExNzkyNTcwOTJSZnAyNWFCY2MifQ' \
        //         -H 'Sec-Fetch-Dest: empty' \
        //         -H 'Sec-Fetch-Mode: cors' \
        //         -H 'Sec-Fetch-Site: same-origin' \
        //         -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36' \
        //         -H 'sec-ch-ua: "Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114"' \
        //         -H 'sec-ch-ua-mobile: ?0' \
        //         -H 'sec-ch-ua-platform: "macOS"' \
        //         --compressed

        String url = "https://portal-wconfig.58corp.com/guest/item/list";
        Map<String, String> params = new HashMap<>();
        params.put("env", "ONLINE");
        params.put("clusterName", cluster);
        params.put("groupName", group);
        params.put("namespaceName", namespace);
        return JacksonUtil.fromJson(super.get(url, params), NamespaceContentResponse.class);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Map<String, Object> commitConfig(String cluster, String group, String namespace, String key, String value) {
        // https://portal-wconfig.58corp.com/owner/item?env=ONLINE&clusterName=zhaopin_web_instructorapi&groupName=sandbox&namespaceName=position_commerce_advise_raw_item_config
        String url = "https://portal-wconfig.58corp.com/owner/item";
        ImmutableMap<String, String> params = ImmutableMap.of(
                "env", "ONLINE",
                "clusterName", cluster,
                "groupName", group,
                "namespaceName", namespace
        );
        Map<String, Object> commitDataMap = new HashMap<>();
        commitDataMap.put("itemKey", key);
        commitDataMap.put("itemValue", value);
        commitDataMap.put("comment", "ideaplugin:" + getOaName());
        commitDataMap.put("lineNum", 0);

        // {"code":200,"message":"OK"}
        return JacksonUtil.ofJsonMap(super.put(url, params, commitDataMap), HashMap.class, String.class, Object.class);
    }

    public Map<String, Object> postCommitConfig(String cluster, String group, String namespace, String key, String value) {
        // https://portal-wconfig.58corp.com/owner/item?env=ONLINE&clusterName=zhaopin_web_instructorapi&groupName=sandbox&namespaceName=position_commerce_advise_raw_item_config
        String url = "https://portal-wconfig.58corp.com/owner/item";
        ImmutableMap<String, String> params = ImmutableMap.of(
                "env", "ONLINE",
                "clusterName", cluster,
                "groupName", group,
                "namespaceName", namespace
        );
        Map<String, Object> commitDataMap = new HashMap<>();
        commitDataMap.put("itemKey", key);
        commitDataMap.put("itemValue", value);
        commitDataMap.put("comment", "ideaplugin:" + getOaName());
        commitDataMap.put("lineNum", 0);

        // {"code":200,"message":"OK"}
        return JacksonUtil.ofJsonMap(super.post(url, params, commitDataMap), HashMap.class, String.class, Object.class);
    }

    public void addConfig(String cluster, String group, String namespace, String key, String value) {
        // https://portal-wconfig.58corp.com/owner/item?env=ONLINE&clusterName=zhaopin_web_instructorapi&groupName=sandbox&namespaceName=position_commerce_advise_static_config
        String url = "https://portal-wconfig.58corp.com/owner/item";
        ImmutableMap<String, String> params = ImmutableMap.of(
                "env", "ONLINE",
                "clusterName", cluster,
                "groupName", group,
                "namespaceName", namespace
        );
        Map<String, Object> commitDataMap = new HashMap<>();
        commitDataMap.put("itemKey", key);
        commitDataMap.put("itemValue", value);
        commitDataMap.put("comment", "ideaplugin:" + getOaName());
        commitDataMap.put("lineNum", "0");
        super.post(url, params, commitDataMap);
    }


    public Map<String, Object> releaseMaster(String cluster, String group, String namespace) {
        // https://portal-wconfig.58corp.com/owner/release/master?env=ONLINE&clusterName=zhaopin_web_instructorapi&groupName=sandbox&namespaceName=position_commerce_advise_raw_item_config&releaseName=&comment=a
        String url = "https://portal-wconfig.58corp.com/owner/release/master";
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("env", "ONLINE");
        paramsMap.put("clusterName", cluster);
        paramsMap.put("groupName", group);
        paramsMap.put("namespaceName", namespace);
        paramsMap.put("releaseName", "");
        paramsMap.put("comment", "wconfig-helper-plugin:" + getOaName());

        // {"code":200,"message":"OK"}
        return JacksonUtil.ofJsonMap(super.post(url, paramsMap, null), HashMap.class, String.class, Object.class);
    }

    public Map<String, Object> grayRelease(String cluster, String group, String namespace, String branchName) {
        // curl 'https://portal-wconfig.58corp.com/owner/release/grayscale?env=ONLINE&clusterName=zhaopin_web_instructorapi&groupName=sandbox&namespaceName=position_commerce_advise_raw_item_config&branchName=20230701153343-9f7206b1de56a7dc&releaseName=&comment=a' \
        //         -X 'POST' \
        //         -H 'Accept: application/json, text/plain, */*' \
        //         -H 'Accept-Language: zh-CN,zh;q=0.9' \
        //         -H 'Cache-Control: no-cache' \
        //         -H 'Connection: keep-alive' \
        //         -H 'Content-Length: 0' \
        //         -H 'Cookie: ec=5A2whgeS-1680574542987-d18a5a021b7bb-173435817; _bu=20230403110150143b1e59; 58tj_uuid=58572d04-2055-4810-a7e1-21050e369227; wmda_uuid=0dbc649e747c3225181a2158492c5ef0; wmda_new_uuid=1; wmda_visited_projects=%3B18101072869043; new_uv=52; sso_ticket=ST-197430-wWP5YUnk4aAhSyb6NrBb-passport-58corp-com; ishare_sso_username=842489FD39F555DE3AFF25E2257A582DCDA68951F9774499; dunCookie=8ad2858279ac7b18564b708f7be32829ab745524706f6038798760a5088baf67c22dc82ce5d3764d; TGC=eyJhbGciOiJIUzUxMiJ9.ZXlKaGJHY2lPaUprYVhJaUxDSmxibU1pT2lKQk1USTRRMEpETFVoVE1qVTJJbjAuLmhFVlNsaDRONUpJLXVGZlUzanpIWncuOHRiS0k2S1pFaHdGS05rNFRfemNWd3pOQWxRb3ZaN0IxUzBrVEE5QXQ4aktFX1ZwSFVCRThYNDB3R0Jjd3ZHUTVsQjhpeG9ISDJuM1pyNWFySDVjcEVOVVZhekhoZzZKQ2JhM2l4dGREc2x2ZmYtTGRHLUdkNnBFTkxOd0drN0xyejBmenFpWmlLbWJCSmdxelo1NGFnLmNpLVhyRGFLOEQ0UmQtTDVtNE1zVEE.Pk7zRnjRLrfFsrQshcHRdpeGljVMATv8NM5n2c6p52r-Errw0x5rPXkC_Q6Kvy_7ndAcsrAa_xcuuOQK5azQAg; _efmdata=p%2Fk7aci7jG%2BavPpIW3wFYhD96QaGhxJnqaquZi1NbCMsrvVzHFtlVGp9%2FPrK7cv%2FnkXLNssgHPSSrkY5ONmeaKU%2BZBVmJqjEIWWBciCLtcM%3D; _exid=bWFrU7VjX%2BanYMBJ7li9g5cl8jH8f3muq7mZFbWtjPTMf2qwZF8lAI1jAziJ2Vz%2BU3xu5GmxF6m329cZAAY99Q%3D%3D' \
        //         -H 'Origin: https://portal-wconfig.58corp.com' \
        //         -H 'Pragma: no-cache' \
        //         -H 'Referer: https://portal-wconfig.58corp.com/' \
        //         -H 'Sec-Fetch-Dest: empty' \
        //         -H 'Sec-Fetch-Mode: cors' \
        //         -H 'Sec-Fetch-Site: same-origin' \
        //         -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36' \
        //         -H 'sec-ch-ua: "Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114"' \
        //         -H 'sec-ch-ua-mobile: ?0' \
        //         -H 'sec-ch-ua-platform: "macOS"' \
        //         --compressed

        String url = "https://portal-wconfig.58corp.com/owner/release/grayscale";
        Map<String, String> paramsMap = new LinkedHashMap<>();
        paramsMap.put("env", "ONLINE");
        paramsMap.put("clusterName", cluster);
        paramsMap.put("groupName", group);
        paramsMap.put("namespaceName", namespace);
        paramsMap.put("branchName", branchName);
        paramsMap.put("releaseName", "");
        paramsMap.put("comment", "wconfig-helper-plugin:" + getOaName());

        String res = super.post(url, paramsMap, null);
        log.info("[grayRelease response] %s" + res);
        return JacksonUtil.ofJsonMap(res, HashMap.class, String.class, Object.class);
    }

    public CreateGrayResponse creatGray(String cluster, String group, String namespace, List<String> ips) {
        // curl 'https://portal-wconfig.58corp.com/owner/grayscale?env=ONLINE&clusterName=zhaopin_web_instructorapi&groupName=sandbox&namespaceName=position_commerce_advise_raw_item_config&name=gray-2023-07-03+10:19:52' \
        // -H 'Accept: application/json, text/plain, */*' \
        // -H 'Accept-Language: zh-CN,zh;q=0.9' \
        // -H 'Cache-Control: no-cache' \
        // -H 'Connection: keep-alive' \
        // -H 'Content-Type: application/json;charset=UTF-8' \
        // -H 'Cookie: ec=5A2whgeS-1680574542987-d18a5a021b7bb-173435817; _bu=20230403110150143b1e59; 58tj_uuid=58572d04-2055-4810-a7e1-21050e369227; wmda_uuid=0dbc649e747c3225181a2158492c5ef0; wmda_new_uuid=1; wmda_visited_projects=%3B18101072869043; new_uv=52; TGC=eyJhbGciOiJIUzUxMiJ9.ZXlKaGJHY2lPaUprYVhJaUxDSmxibU1pT2lKQk1USTRRMEpETFVoVE1qVTJJbjAuLmhFVlNsaDRONUpJLXVGZlUzanpIWncuOHRiS0k2S1pFaHdGS05rNFRfemNWd3pOQWxRb3ZaN0IxUzBrVEE5QXQ4aktFX1ZwSFVCRThYNDB3R0Jjd3ZHUTVsQjhpeG9ISDJuM1pyNWFySDVjcEVOVVZhekhoZzZKQ2JhM2l4dGREc2x2ZmYtTGRHLUdkNnBFTkxOd0drN0xyejBmenFpWmlLbWJCSmdxelo1NGFnLmNpLVhyRGFLOEQ0UmQtTDVtNE1zVEE.Pk7zRnjRLrfFsrQshcHRdpeGljVMATv8NM5n2c6p52r-Errw0x5rPXkC_Q6Kvy_7ndAcsrAa_xcuuOQK5azQAg; _efmdata=p%2Fk7aci7jG%2BavPpIW3wFYhD96QaGhxJnqaquZi1NbCMsrvVzHFtlVGp9%2FPrK7cv%2FnkXLNssgHPSSrkY5ONmeaKU%2BZBVmJqjEIWWBciCLtcM%3D; _exid=bWFrU7VjX%2BanYMBJ7li9g5cl8jH8f3muq7mZFbWtjPTMf2qwZF8lAI1jAziJ2Vz%2BU3xu5GmxF6m329cZAAY99Q%3D%3D; dunCookie=48f4d9d33c57861648de681b8f6d893a5c37552feb777059f2afbd237f8251039a6a40e5df42beed; sso_ticket=ST-457769-JIjerutctjcZnoqHifcw-passport-58corp-com' \
        // -H 'Origin: https://portal-wconfig.58corp.com' \
        // -H 'Pragma: no-cache' \
        // -H 'Referer: https://portal-wconfig.58corp.com/' \
        // -H 'Sec-Fetch-Dest: empty' \
        // -H 'Sec-Fetch-Mode: cors' \
        // -H 'Sec-Fetch-Site: same-origin' \
        // -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36' \
        // -H 'sec-ch-ua: "Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114"' \
        // -H 'sec-ch-ua-mobile: ?0' \
        // -H 'sec-ch-ua-platform: "macOS"' \
        // --data-raw '{"comment":"","ips":["2.2.2.2"]}' \
        // --compressed
        String url = "https://portal-wconfig.58corp.com/owner/grayscale";
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("env", "ONLINE");
        paramsMap.put("clusterName", cluster);
        paramsMap.put("groupName", group);
        paramsMap.put("namespaceName", namespace);
        paramsMap.put("name", "gray-" + new SimpleDateFormat("yyyy-MM-dd+HH:mm:ss").format(new Date()));

        Map<String, Object> body = new HashMap<>();
        body.put("comment", "");
        body.put("ips", ips);

        return JacksonUtil.fromJson(super.post(url, paramsMap, body), CreateGrayResponse.class);
    }

    public CreateNamespaceResponse createNamespace(String cluster, String group, String namespace, String comment, String format) {
        // curl 'https://portal-wconfig.58corp.com/owner/namespace?clusterName=zhaopin_web_instructorapi&env=ONLINE' \
        // -H 'Accept: application/json, text/plain, */*' \
        // -H 'Accept-Language: zh-CN,zh;q=0.9' \
        // -H 'Cache-Control: no-cache' \
        // -H 'Connection: keep-alive' \
        // -H 'Content-Type: application/json;charset=UTF-8' \
        // -H 'Cookie: ec=5A2whgeS-1680574542987-d18a5a021b7bb-173435817; _bu=20230403110150143b1e59; 58tj_uuid=58572d04-2055-4810-a7e1-21050e369227; wmda_uuid=0dbc649e747c3225181a2158492c5ef0; wmda_new_uuid=1; wmda_visited_projects=%3B18101072869043; new_uv=52; TGC=eyJhbGciOiJIUzUxMiJ9.ZXlKaGJHY2lPaUprYVhJaUxDSmxibU1pT2lKQk1USTRRMEpETFVoVE1qVTJJbjAuLmhFVlNsaDRONUpJLXVGZlUzanpIWncuOHRiS0k2S1pFaHdGS05rNFRfemNWd3pOQWxRb3ZaN0IxUzBrVEE5QXQ4aktFX1ZwSFVCRThYNDB3R0Jjd3ZHUTVsQjhpeG9ISDJuM1pyNWFySDVjcEVOVVZhekhoZzZKQ2JhM2l4dGREc2x2ZmYtTGRHLUdkNnBFTkxOd0drN0xyejBmenFpWmlLbWJCSmdxelo1NGFnLmNpLVhyRGFLOEQ0UmQtTDVtNE1zVEE.Pk7zRnjRLrfFsrQshcHRdpeGljVMATv8NM5n2c6p52r-Errw0x5rPXkC_Q6Kvy_7ndAcsrAa_xcuuOQK5azQAg; _efmdata=p%2Fk7aci7jG%2BavPpIW3wFYhD96QaGhxJnqaquZi1NbCMsrvVzHFtlVGp9%2FPrK7cv%2FnkXLNssgHPSSrkY5ONmeaKU%2BZBVmJqjEIWWBciCLtcM%3D; _exid=bWFrU7VjX%2BanYMBJ7li9g5cl8jH8f3muq7mZFbWtjPTMf2qwZF8lAI1jAziJ2Vz%2BU3xu5GmxF6m329cZAAY99Q%3D%3D; dunCookie=48f4d9d33c57861648de681b8f6d893a5c37552feb777059f2afbd237f8251039a6a40e5df42beed; sso_ticket=ST-457769-JIjerutctjcZnoqHifcw-passport-58corp-com' \
        // -H 'Origin: https://portal-wconfig.58corp.com' \
        // -H 'Pragma: no-cache' \
        // -H 'Referer: https://portal-wconfig.58corp.com/' \
        // -H 'Sec-Fetch-Dest: empty' \
        // -H 'Sec-Fetch-Mode: cors' \
        // -H 'Sec-Fetch-Site: same-origin' \
        // -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36' \
        // -H 'sec-ch-ua: "Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114"' \
        // -H 'sec-ch-ua-mobile: ?0' \
        // -H 'sec-ch-ua-platform: "macOS"' \
        // --data-raw '{"name":"lupeng_test1","clusterName":"zhaopin_web_instructorapi","shared":false,"comment":"","associated":false,"format":"txt","groups":["sandbox"]}' \
        // --compressed

        String url = "https://portal-wconfig.58corp.com/owner/namespace";
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("env", "ONLINE");
        paramsMap.put("clusterName", cluster);

        Map<String, Object> body = new HashMap<>();
        body.put("name", namespace);
        body.put("clusterName", cluster);
        body.put("shared", false);
        body.put("comment", comment);
        body.put("associated", false);
        body.put("format", format);
        body.put("groups", ImmutableList.of(group));
        return JacksonUtil.fromJson(super.post(url, paramsMap, body), CreateNamespaceResponse.class);
    }

    public void delete(String cluster, String id) {
        // https://portal-wconfig.58corp.com/owner/item?env=ONLINE&clusterName=zhaopin_web_instructorapi&itemId=1670305908713697281
        String url = "https://portal-wconfig.58corp.com/owner/item";
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("env", "ONLINE");
        paramsMap.put("clusterName", cluster);
        paramsMap.put("itemId", id);
        super.delete(url, paramsMap);
    }

    public GrayIpListResponse getGrayIpList(String cluster, String group, String namespace) {
        // curl 'https://portal-wconfig.58corp.com/owner/grayscale/list?env=ONLINE&clusterName=zpadbiz&groupName=default_group&namespaceName=%2Fzpadbiz%2Fnewcate%2Fexclusive%2Fposition%2Fresourcelimit' \
        // -H 'Accept: application/json, text/plain, */*' \
        // -H 'Accept-Language: zh-CN,zh;q=0.9' \
        // -H 'Cache-Control: no-cache' \
        // -H 'Connection: keep-alive' \
        // -H 'Cookie: ec=5A2whgeS-1680574542987-d18a5a021b7bb-173435817; _bu=20230403110150143b1e59; 58tj_uuid=58572d04-2055-4810-a7e1-21050e369227; wmda_uuid=0dbc649e747c3225181a2158492c5ef0; wmda_new_uuid=1; wmda_visited_projects=%3B18101072869043; new_uv=52; sso_ticket=ST-197430-wWP5YUnk4aAhSyb6NrBb-passport-58corp-com; ishare_sso_username=842489FD39F555DE3AFF25E2257A582DCDA68951F9774499; dunCookie=8ad2858279ac7b18564b708f7be32829ab745524706f6038798760a5088baf67c22dc82ce5d3764d; TGC=eyJhbGciOiJIUzUxMiJ9.ZXlKaGJHY2lPaUprYVhJaUxDSmxibU1pT2lKQk1USTRRMEpETFVoVE1qVTJJbjAuLmhFVlNsaDRONUpJLXVGZlUzanpIWncuOHRiS0k2S1pFaHdGS05rNFRfemNWd3pOQWxRb3ZaN0IxUzBrVEE5QXQ4aktFX1ZwSFVCRThYNDB3R0Jjd3ZHUTVsQjhpeG9ISDJuM1pyNWFySDVjcEVOVVZhekhoZzZKQ2JhM2l4dGREc2x2ZmYtTGRHLUdkNnBFTkxOd0drN0xyejBmenFpWmlLbWJCSmdxelo1NGFnLmNpLVhyRGFLOEQ0UmQtTDVtNE1zVEE.Pk7zRnjRLrfFsrQshcHRdpeGljVMATv8NM5n2c6p52r-Errw0x5rPXkC_Q6Kvy_7ndAcsrAa_xcuuOQK5azQAg; _efmdata=p%2Fk7aci7jG%2BavPpIW3wFYhD96QaGhxJnqaquZi1NbCMsrvVzHFtlVGp9%2FPrK7cv%2FnkXLNssgHPSSrkY5ONmeaKU%2BZBVmJqjEIWWBciCLtcM%3D; _exid=bWFrU7VjX%2BanYMBJ7li9g5cl8jH8f3muq7mZFbWtjPTMf2qwZF8lAI1jAziJ2Vz%2BU3xu5GmxF6m329cZAAY99Q%3D%3D' \
        // -H 'Pragma: no-cache' \
        // -H 'Referer: https://portal-wconfig.58corp.com/?blackbox=tdfpeyJ2IjoiWCtnWmJlWDErQzRsWjJGL05oTGJDWklmM2JsM09QRXgxSEF2MnAwS0lWbE4yMjNPcDNrOGs1Z3B0WDlDTlpybyIsIm9zIjozLCJ0IjoiM1dQVjE2ODExNzkyNTcwOTJSZnAyNWFCY2MifQ' \
        // -H 'Sec-Fetch-Dest: empty' \
        // -H 'Sec-Fetch-Mode: cors' \
        // -H 'Sec-Fetch-Site: same-origin' \
        // -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36' \
        // -H 'sec-ch-ua: "Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114"' \
        // -H 'sec-ch-ua-mobile: ?0' \
        // -H 'sec-ch-ua-platform: "macOS"' \
        // --compressed
        String url = "https://portal-wconfig.58corp.com/owner/grayscale/list";
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("env", "ONLINE");
        paramsMap.put("clusterName", cluster);
        paramsMap.put("groupName", group);
        paramsMap.put("namespaceName", namespace);

        return JacksonUtil.fromJson(super.get(url, paramsMap), GrayIpListResponse.class);
    }
}
