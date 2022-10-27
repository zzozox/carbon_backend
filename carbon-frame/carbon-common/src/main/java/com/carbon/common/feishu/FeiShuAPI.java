package com.carbon.common.feishu;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.InputStreamResource;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.carbon.common.entity.AddResponse;
import com.carbon.common.entity.Folder;
import com.carbon.common.entity.Message;
import com.carbon.common.entity.Test;
import com.carbon.common.enums.ApprovalCodeEnum;
import com.carbon.common.exception.CommonBizException;
import com.carbon.domain.common.ApiResult;
import com.carbon.domain.mq.entity.AddTradingAccountApproval;
import com.carbon.domain.mq.entity.AssetUploadApproval;
import com.carbon.domain.mq.entity.ProjectApproval;
import com.carbon.domain.mq.entity.QuotaApproval;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bae
 * @version 1.0
 * @date 2022/4/29 10:27
 */
@Slf4j
public class FeiShuAPI {

    private static String app_id="cli_a3c8c08a7fb05013";

    private static String app_secret="b7MktQdNpROIqi8sIeyNPWLfcK366Lp6";
    /**
     * 获取企业自建应用app_id与app_secret
     * @return
     */
    public static Map<String,String> getEnterpriseInformation(){
        Map map=new HashMap();
        map.put("app_id",app_id);
        map.put("app_secret",app_secret);
        return map;
    }

    /**
     * 获取租户应用app_id与app_secret
     * @return
     */
    public static Map<String,String> getTenantInformation(){
        Map map=new HashMap();
        map.put("app_id","cli_a3c8c08a7fb05013");
        map.put("app_secret","b7MktQdNpROIqi8sIeyNPWLfcK366Lp6");
        return map;
    }



    /**
     * 获取tenant_access_token
     * @return
     */
    public static String getTenantToken(){
        Map map=new HashMap();
        map.put("app_id",app_id);
        map.put("app_secret",app_secret);
        String url="https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";
        String tokenStr = HttpUtil.post(url, map);
        JSONObject object= JSON.parseObject(tokenStr);
        String token = object.getString("tenant_access_token");
        return "Bearer "+token;
    }

    /**
     * 获取app信息
     * @return
     */
    public static Map<String,String> getAppInfo(String app){
        Map map=null;
        if("1".equals(app)){
            map=getEnterpriseInformation();
        }else if("2".equals(app)){
            map=getTenantInformation();
        }
        return map;
    }

    /**
     * 获取app_access_token
     * @return
     */
    public static String getAppToken(String app){
        Map map =getAppInfo(app);
        String url="https://open.feishu.cn/open-apis/auth/v3/app_access_token/internal";
        String tokenStr = HttpUtil.post(url, map);
        JSONObject object= JSON.parseObject(tokenStr);
        String token = object.getString("app_access_token");
        return "Bearer "+token;
    }

    /**
     * 刷新user_access_token
     * @return
     */
    public static JSONObject refreshUserToken(String refreshToken,String app){
        String appToken = getAppToken(app);
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("grant_type", "refresh_token");
        param.put("refresh_token", refreshToken);
        String json=param.toString();

        String url="https://open.feishu.cn/open-apis/authen/v1/refresh_access_token";
        String result = HttpRequest.post(url).header(Header.AUTHORIZATION, appToken).body(json).execute().body();
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));
        return data;
    }

    /**
     * 获取用户信息
     * @param code  登录预授权码 code
     */
    public static JSONObject getUser(String code,String app){
        String appToken = getAppToken(app);
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("code", code);
        param.put("grant_type", "authorization_code");
        String json=param.toString();

        String url="https://open.feishu.cn/open-apis/authen/v1/access_token";
        String result = HttpRequest.post(url).header(Header.AUTHORIZATION, appToken).body(json).execute().body();
//        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, appToken).body(json).execute().body();
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));

        return data;
    }


    /**
     * 获取 云文档鉴权所需信息
     * window.webComponent.config({
     *   openId,    // 当前登录用户的open id，要确保与生成 signature 使用的 user_access_token 相对应，使用 app_access_token 时此项不填。注意：仅云文档组件可使用app_access_token
     *   signature, // 签名
     *   appId,     // 应用 appId
     *   timestamp, // 时间戳（毫秒）
     *   nonceStr,  // 随机字符串
     *   url,       // 第3步参与加密计算的url
     *   jsApiList, // 指定要使用的组件列表，请根据对应组件的开发文档填写。如云文档组件，填写['DocsComponent']
     *   locale,    // 指定组件的国际化语言：en-US-英文、zh-CN-中文、ja-JP-日文
     * }).then(res=>{
     *   // 可以在这里进行组件动态渲染
     * })
     * @param  code  登录预授权码 code
     * @return
     */
    public static Message getTextAuthenticationInformation(String code, String app, String refreshToken){
        JSONObject data=null;
        if(refreshToken==null||"".equals(refreshToken)){
            data = getUser(code,app);
        }else {
            data=refreshUserToken(refreshToken,app);
        }
        Message res=new Message();

        if(data==null){
            return null;
        }
        res.setRefreshToken(data.getString("refresh_token"));

        //获取user_token
        String userToken = "Bearer "+data.getString("access_token");

        //获取open_id
        res.setOpenId(data.getString("open_id"));

        //获取jsapi_ticket
        String requestUrl="https://open.feishu.cn/open-apis/jssdk/ticket/get";
        String result = HttpUtil.createRequest(Method.POST, requestUrl).header(Header.AUTHORIZATION, userToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        data = JSONObject.parseObject(object.getString("data"));
        String jsapi_ticket = data.getString("ticket");


        String noncestr= RandomUtil.randomString(16);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String url="http://saas.xcarbon.cc/";

        String string1="jsapi_ticket="+jsapi_ticket+
                "&noncestr="+noncestr+"&timestamp="+timestamp+"&url="+url;

        String signature = SecureUtil.sha1(string1);

        res.setSignature(signature);
        Map<String, String> appInfo = getAppInfo(app);
        res.setAppId(appInfo.get("app_id"));
        res.setTimestamp(timestamp);
        res.setNonceStr(noncestr);
        res.setUrl(url);
        res.setJsApiList("DocsComponent");
        res.setLocale("zh");

        return res;
    }


    /**
     * 获取 电子表格鉴权所需信息
     * window.webComponent.config({
     *   openId,    // 当前登录用户的open id，要确保与生成 signature 使用的 user_access_token 相对应，使用 app_access_token 时此项不填。注意：仅云文档组件可使用app_access_token
     *   signature, // 签名
     *   appId,     // 应用 appId
     *   timestamp, // 时间戳（毫秒）
     *   nonceStr,  // 随机字符串
     *   url,       // 第3步参与加密计算的url
     *   jsApiList, // 指定要使用的组件列表，请根据对应组件的开发文档填写。如云文档组件，填写['DocsComponent']
     *   locale,    // 指定组件的国际化语言：en-US-英文、zh-CN-中文、ja-JP-日文
     * }).then(res=>{
     *   // 可以在这里进行组件动态渲染
     * })
     *
     * @param code   用于获取user_token
     * @param app    判定需获取文档在哪个应用
     * @param url    鉴权后重定向url
     * @param refreshToken    刷新user_token
     * @return
     */
    public static Message getFormAuthenticationInformation(String code, String app, String url, String refreshToken){
        JSONObject data=null;
        if(refreshToken==null||"".equals(refreshToken)){
            data = getUser(code,app);
        }else {
            data=refreshUserToken(refreshToken,app);
        }
        Message res=new Message();

        if(data==null){
            return null;
        }
        res.setRefreshToken(data.getString("refresh_token"));

        //获取user_token
        String userToken = "Bearer "+data.getString("access_token");

        //获取open_id
        res.setOpenId(data.getString("open_id"));

        //获取jsapi_ticket
        String requestUrl="https://open.feishu.cn/open-apis/jssdk/ticket/get";
        String result = HttpUtil.createRequest(Method.POST, requestUrl).header(Header.AUTHORIZATION, userToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        data = JSONObject.parseObject(object.getString("data"));
        String jsapi_ticket = data.getString("ticket");


        String noncestr= RandomUtil.randomString(16);
        String timestamp = String.valueOf(System.currentTimeMillis());
//        String url="http://saas.xcarbon.cc/sys/business";

        String string1="jsapi_ticket="+jsapi_ticket+
                "&noncestr="+noncestr+"&timestamp="+timestamp+"&url="+url;

        String signature = SecureUtil.sha1(string1);

        res.setSignature(signature);
        Map<String, String> appInfo = getAppInfo(app);
        res.setAppId(appInfo.get("app_id"));
        res.setTimestamp(timestamp);
        res.setNonceStr(noncestr);
        res.setUrl(url);
        res.setJsApiList("DocsComponent");
        res.setLocale("zh");

        return res;
    }


    /**
     * 传入 code或refreshToken 为该用户创建文件夹 并返回链接
     * @param folderName
     * @param refreshToken
     * @return
     */
    public static Folder createFolder(String app, String folderName, String code, String refreshToken){
        JSONObject data=null;
        if(refreshToken==null||"".equals(refreshToken)){
            data = getUser(code,app);
        }else {
            data=refreshUserToken(refreshToken,app);
        }
        Folder folder=new Folder();
        folder.setRefreshToken(data.getString("refresh_token"));
        String user_token = "Bearer "+data.getString("access_token");
        String requestUrl="https://open.feishu.cn/open-apis/drive/v1/files/create_folder";
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("name", folderName);
        param.put("folder_token", "fldcnyA6xON7eMKD6REo0RPYFwc");
        String json=param.toString();
        String result = HttpRequest.post(requestUrl).header(Header.AUTHORIZATION, user_token).body(json).execute().body();
        JSONObject object= JSON.parseObject(result);
        data = JSONObject.parseObject(object.getString("data"));
        folder.setUrl(data.getString("url"));
        return folder;
    }



    /**
     * 根据文档编号返回文档token
     * @param templateNum
     * @return
     */
    public static String getTextToken(String templateNum){
        if("0420000001".equals(templateNum)){
            return "doccnN9C9AChwRryhlXW32yI0nf";
        }else if("0420000002".equals(templateNum)){
            return "doccnsNgbESPsVgINde5qmTqwBe";
        }else if("0420000003".equals(templateNum)){
            return "doccn1rf9EqaXZcJw57zAlxDZKg";
        }else if("0420000004".equals(templateNum)){
            return "shtcnwqyEqtaFuSch4ZsE4E6RMS";
        }
        return null;
    }



    /**
     * 新增空白文章
     *
     */
    public static AddResponse addArticle(){
        String tenantToken = getTenantToken();
        String url="https://open.feishu.cn/open-apis/doc/v2/create";
//        String result = HttpRequest.post(url).header(Header.AUTHORIZATION, tenantToken).execute().body();
        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));

        String objToken = data.getString("objToken");
        modifyPermissions(tenantToken,objToken);
        AddResponse response = new AddResponse();
        response.setObjToken(objToken);
        response.setUrl(data.getString("url"));
        return response;
    }

    /**
     * 修改文档权限
     * @param tenantToken
     * @param fileToken  文档token
     */
    public static void modifyPermissions(String tenantToken,String fileToken){
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("external_access", true);
        param.put("security_entity", "anyone_can_edit");
        param.put("comment_entity", "anyone_can_edit");
        param.put("share_entity", "anyone");
        param.put("link_share_entity", "anyone_editable");
        param.put("invite_external", true);
        String json=param.toString();

        String url="https://open.feishu.cn/open-apis/drive/v1/permissions/"+fileToken+"/public?type=doc";
//        String result = HttpRequest.patch(url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
        HttpUtil.createRequest(Method.PATCH, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
    }

    /**
     * 获取元空间token
     * test
     */
    public static String getMyMetaSpaceToken(){
        String tenantToken = getTenantToken();
        String url = "https://open.feishu.cn/open-apis/drive/explorer/v2/folder/:folderToken/meta";
        String result = HttpUtil.createRequest(Method.GET,url).header(Header.AUTHORIZATION, tenantToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));

        String rootFolderToken = data.getString("token");
        return rootFolderToken;
    }

    /**
     * 获取空间中文件清单
     * @return
     */
    public static String getFileUnderFolder(){
        String tenantToken = getTenantToken();
        String myMetaSpaceToken = getMyMetaSpaceToken();

        String url = "https://open.feishu.cn/open-apis/drive/explorer/v2/folder/"+myMetaSpaceToken+"/children";
        String result = HttpUtil.createRequest(Method.GET, url).header(Header.AUTHORIZATION, tenantToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));

//        data.getString("")
        return null;
    }

    /**
     * 获取云文档版本
     * @return
     */
    public static String getTextReversion(String fileToken){
        String tenantToken = getTenantToken();

        String url="https://open.feishu.cn/open-apis/doc/v2/"+fileToken+"/content";
        String result = HttpUtil.createRequest(Method.GET, url).header(Header.AUTHORIZATION, tenantToken).execute().body();
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));

        return data.getString("revision");
    }

    /**
     * 修改文章内容
     * @param fileToken
     * @param text
     * @param replaceText
     */
    public static void updateFileText(String fileToken,String text,String replaceText){
        String tenantToken = getTenantToken();

        if (StrUtil.isBlank(fileToken)) {
            fileToken="doccnN9C9AChwRryhlXW32yI0nf";
        }

        String url="https://open.feishu.cn/open-apis/doc/v2/"+fileToken+"/batch_update";

        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("Revision", getTextReversion(fileToken));
        JSONArray array=new JSONArray();
        String requests="{\"requestType\":\"ReplaceAllTextRequestType\",\"replaceAllTextRequest\":{\"containsText\":{\"text\":\""+text+"\",\"matchCase\":false},\"replaceText\": \""+replaceText+"\"}}";
        array.add(requests);
        param.put("Requests", array);

        String json=param.toString();

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

    }

    /**
     * 修改文章内容(测试)
     * @param fileToken
     * @param text
     * @param replaceText
     */
    public static Test testUpdateFileText(String fileToken, String text, String replaceText){
        String tenantToken = getTenantToken();

        if (StrUtil.isBlank(fileToken)) {
            fileToken="doccnN9C9AChwRryhlXW32yI0nf";
        }

        String url="https://open.feishu.cn/open-apis/doc/v2/"+fileToken+"/batch_update";

        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("Revision", getTextReversion(fileToken));
        JSONArray array=new JSONArray();
        String requests="{\"requestType\":\"ReplaceAllTextRequestType\",\"replaceAllTextRequest\":{\"containsText\":{\"text\":\""+text+"\",\"matchCase\":false},\"replaceText\": \""+replaceText+"\"}}";
        array.add(requests);
        param.put("Requests", array);

        String json=param.toString();

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        return new Test(fileToken,text,replaceText);
    }

    /**
     * 根据文本编号和位置修改指定区域内容
     * @param templateNum
     * @param position
     * @param replaceText
     */
    public static void updateFileByNum(String templateNum,String position,String replaceText){
        String tenantToken = getTenantToken();

        String fileToken = getTextToken(templateNum);
        String text = getTextByTemplateAndPosition(templateNum, position);

        String url="https://open.feishu.cn/open-apis/doc/v2/"+fileToken+"/batch_update";

        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("Revision", getTextReversion(fileToken));
        JSONArray array=new JSONArray();
        String requests="{\"requestType\":\"ReplaceAllTextRequestType\",\"replaceAllTextRequest\":{\"containsText\":{\"text\":\""+text+"\",\"matchCase\":false},\"replaceText\": \""+replaceText+"\"}}";
        array.add(requests);
        param.put("Requests", array);

        String json=param.toString();

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

    }

    /**
     * 根据文档原内容修改指定区域内容
     */
    public static void updateFile(String fileToken, String text,String replaceText){
        String tenantToken = getTenantToken();


        String url="https://open.feishu.cn/open-apis/doc/v2/"+fileToken+"/batch_update";

        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("Revision", getTextReversion(fileToken));
        JSONArray array=new JSONArray();
        String requests="{\"requestType\":\"ReplaceAllTextRequestType\",\"replaceAllTextRequest\":{\"containsText\":{\"text\":\""+text+"\",\"matchCase\":false},\"replaceText\": \""+replaceText+"\"}}";
        array.add(requests);
        param.put("Requests", array);

        String json=param.toString();

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

    }

    /**
     * 根据文章编号和位置获取初始内容
     * @param templateNum
     * @param position
     * @return
     */
    public static String getTextByTemplateAndPosition(String templateNum,String position){
        if("0420000001".equals(templateNum)){
            switch (position){
                case "0430000001":
                    return "初始项目名称";
                case "0430000002":
                    return "初始项目类别";
            }
        }else if("0420000002".equals(templateNum)){
            switch (position){
                case "0440000001":
                    return "初始准线";
                case "0440000002":
                    return "初始排放";
            }
        }else if("0420000003".equals(templateNum)){
            switch (position){
                case "0450000001":
                    return "初始单位";
                case "0450000002":
                    return "初始描述";
            }
        }
        return null;
    }


    /**
     * 更新指定工作表
     * @param spreadsheetToken
     * @param sheetId
     * @param idField     唯一确定一条数据的字段内容
     * @param data        数据
     * @param rightRange  工作表右边界字母
     */
    public static void updateForm(String spreadsheetToken,String sheetId,String idField,Object data,String rightRange){
        String tenantToken = getTenantToken();
        String updateContent = appendString(data);
        String s = searchForm(spreadsheetToken, sheetId, idField,rightRange);
        String url=null;
        String location1=null;
        String location2=null;
        if(s==null){ //没有此纪录  则在工作表中追加数据
            url="https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/"+spreadsheetToken+"/values_append";
            location1="1";
            location2="5000";
        }else {  //若有此纪录 则修改
            url="https://open.feishu.cn/open-apis/sheets/v2/spreadsheets/"+spreadsheetToken+"/values";
            location1 = location2 = new StringBuilder(s).deleteCharAt(0).toString();
        }
        String json="{\n" +
                "    \"valueRange\":{\n" +
                "        \"range\": \""+sheetId+"!A"+location1+":"+rightRange+location2+"\",\n" +
                "        \"values\": [\n" +
                "        [\n" +
                            updateContent +
                "        ]\n" +
                "        ]\n" +
                "    }\n" +
                "}";

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
    }


    /**
     * 查找指定内容在工作表中的位置
     * @param spreadsheetToken  表格唯一标识
     * @param sheetId  工作表唯一标识
     * @param content  查找内容
     * @return  返回指定内容在工作表的坐标
     */
    public static String searchForm(String spreadsheetToken,String sheetId,String content,String rightRange){
        String tenantToken = getTenantToken();
        String url="https://open.feishu.cn/open-apis/sheets/v3/spreadsheets/"+spreadsheetToken+"/sheets/"+sheetId+"/find";

        String json="{\n" +
                "    \"find\": \""+content+"\",\n" +
                "    \"find_condition\": {\n" +
                "        \"include_formulas\": false,\n" +
                "        \"match_case\": false,\n" +
                "        \"match_entire_cell\": true,\n" +
                "        \"range\": \""+sheetId+"!A1:"+rightRange+"200\",\n" +
                "        \"search_by_regex\": false\n" +
                "    }\n" +
                "}\n";
        log.info("searchForm-param:{}",json);

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
        log.info("searchForm-result:{}",result);
        JSONObject object= JSON.parseObject(result);

        if (object.getInteger("code") != 0){
            return null;
        }

        JSONObject data = JSONObject.parseObject(object.getString("data"));
        JSONObject find_result = JSONObject.parseObject(data.getString("find_result"));
        com.alibaba.fastjson.JSONArray matched_cells = find_result.getJSONArray("matched_cells");
//        System.out.println(matched_cells.get(0));
        if(matched_cells.size()==0)
        {
            return null;
        }
        return (String) matched_cells.get(0);
    }

    /**
     * 将对象中所有属性拼接为字符串返回
     * @param o
     * @return
     */
    public static String appendString(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        StringBuilder sb=new StringBuilder();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if(field.get(o)!=null){
                    sb.append("\"");
                    sb.append(field.get(o).toString());
                    sb.append("\"");
                    sb.append(",");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }


    /**
     * 立项审批请求
     * @param project  项目立项审批表单MQ实体类
     */
    public static String projectApproval(ProjectApproval project){
        log.info("----------发送项目立项审批！！！",project);
        String tenantToken = getTenantToken();
        String url="https://www.feishu.cn/approval/openapi/v2/instance/create";
//        project.setMethodologyName("方法学");
        String form="[{\"id\":\"legalOwnerName\", \"type\": \"input\", \"value\":\""+project.getLegalPersonName()
                +"\"},{\"id\":\"legalOwnerContactNumber\", \"type\": \"input\", \"value\":\""+project.getLegalPersonPhone()
                +"\"},{\"id\":\"OwnerName\", \"type\": \"input\", \"value\":\""+project.getOwnerName()
                +"\"},{\"id\":\"projectName\", \"type\": \"input\", \"value\":\""+project.getProjectName()
                +"\"},{\"id\":\"methodologyName\", \"type\": \"input\", \"value\":\""+project.getCarbonMethodology()
                +"\"},{\"id\":\"nation\", \"type\": \"input\", \"value\":\""+project.getCountry()
                +"\"},{\"id\":\"province\", \"type\": \"input\", \"value\":\""+project.getProvince()
                +"\"},{\"id\":\"city\", \"type\": \"input\", \"value\":\""+project.getCity()
                +"\"},{\"id\":\"projectLocation\", \"type\": \"input\", \"value\":\""+project.getAddress()
                +"\"},{\"id\":\"developAgencies\", \"type\": \"input\", \"value\":\""+project.getDevelopAgencies()
                +"\"},{\"id\":\"developPrincipal\", \"type\": \"input\", \"value\":\""+project.getPrincipalName()
                +"\"},{\"id\":\"developPrincipalContactNumber\", \"type\": \"input\", \"value\":\""+project.getPrincipalPhone()
                +"\"},{\"id\":\"projectInformation\", \"type\": \"input\", \"value\":\""+project.getProjectIntroduction()+"\"}]";
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("approval_code", ApprovalCodeEnum.PROJECT_INITIATION_APPROVAL.getCode());
        param.put("open_id","ou_9e7dda80fd70dab01b88811ef01132d9");
        param.put("form", form);

        String json=param.toString();
        log.info("projectApproval-param:{}",json);

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        log.info("projectApproval-result:{}",result);
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));
        return data.getString("instance_code");
    }


    /**
     * 资产上传审批请求
     * @param asset  资产上传审批表单MQ实体类
     */
    public static String assetUploadApproval(AssetUploadApproval asset){
        String tenantToken = getTenantToken();

        String url="https://www.feishu.cn/approval/openapi/v2/instance/create";
        String form="[{\"id\":\"userName\", \"type\": \"input\", \"value\":\""+asset.getUserName()
                +"\"},{\"id\":\"agenciesName\", \"type\": \"input\", \"value\":\""+asset.getAgenciesName()
                +"\"},{\"id\":\"contactNumber\", \"type\": \"input\", \"value\":\""+asset.getContactNumber()
                +"\"},{\"id\":\"assetType\", \"type\": \"input\", \"value\":\""+asset.getAssetType()
                +"\"},{\"id\":\"primaryMarketHoldingInstitutions\", \"type\": \"input\", \"value\":\""+asset.getPrimaryMarketHoldingInstitutions()+
                "\"},{\"id\":\"shareholding\", \"type\": \"input\", \"value\":\""+asset.getShareholding()
                +"\"},{\"id\":\"proofOfPosition\", \"type\": \"input\", \"value\":\""+asset.getProofOfPosition()
                +"\"},{\"id\":\"issuingAgency\", \"type\": \"input\", \"value\":\""+asset.getIssuingAgency()+"\"}]";
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("approval_code",ApprovalCodeEnum.ASSETS_APPROVAL.getCode());
        param.put("open_id","ou_9e7dda80fd70dab01b88811ef01132d9");
        param.put("form", form);

        String json=param.toString();
        log.info("assetUploadApproval-param:{}",json);

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        log.info("assetUploadApproval-result:{}",result);
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));
        return data.getString("instance_code");
    }

    /**
     * 资产上传审批请求
     * @param quota  配额审批表单MQ实体类
     */
    public static String quotaApproval(QuotaApproval quota){
        String tenantToken = getTenantToken();

        String url="https://www.feishu.cn/approval/openapi/v2/instance/create";
        String form="[{\"id\":\"userName\", \"type\": \"input\", \"value\":\""+quota.getUserName()
                +"\"},{\"id\":\"agenciesName\", \"type\": \"input\", \"value\":\""+quota.getAgenciesName()
                +"\"},{\"id\":\"contactNumber\", \"type\": \"input\", \"value\":\""+quota.getContactNumber()
                +"\"},{\"id\":\"assetType\", \"type\": \"input\", \"value\":\""+quota.getAssetType()
                +"\"},{\"id\":\"primaryMarketHoldingInstitutions\", \"type\": \"input\", \"value\":\""+quota.getPrimaryMarketHoldingInstitutions()+
                "\"},{\"id\":\"shareholding\", \"type\": \"input\", \"value\":\""+quota.getShareholding()
                +"\"},{\"id\":\"proofOfPosition\", \"type\": \"input\", \"value\":\""+quota.getProofOfPosition()
                +"\"},{\"id\":\"issuingAgency\", \"type\": \"input\", \"value\":\""+quota.getIssuingAgency()+"\"}]";
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("approval_code",ApprovalCodeEnum.QUOTA_APPROVAL.getCode());
        param.put("open_id","ou_9e7dda80fd70dab01b88811ef01132d9");
        param.put("form", form);

        String json=param.toString();
        log.info("quotaApproval-param:{}",json);

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        log.info("quotaApproval-result:{}",result);
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));
        return data.getString("instance_code");
    }

    /**
     * 添加交易账户审批请求
     * @param account  添加交易账户审批表单MQ实体类
     */
    public static String addTradingAccountApproval(AddTradingAccountApproval account){
        String tenantToken = getTenantToken();

        String url="https://www.feishu.cn/approval/openapi/v2/instance/create";
        String form="[{\"id\":\"userName\", \"type\": \"input\", \"value\":\""+account.getUserName()+"\"}," +
                "{\"id\":\"agenciesName\", \"type\": \"input\", \"value\":\""+account.getAgenciesName()+"\"}," +
                "{\"id\":\"contactNumber\", \"type\": \"input\", \"value\":\""+account.getContactNumber()+"\"}," +
                "{\"id\":\"exchangeName\", \"type\": \"input\", \"value\":\""+account.getExchangeName()+"\"}," +
                "{\"id\":\"tradeAccount\", \"type\": \"input\", \"value\":\""+account.getTradeAccount()+"\"}," +
                "{\"id\":\"widget16583974324200001\", \"type\": \"input\", \"value\":\""+account.getRemark()+"\"}," +
                "{\"id\":\"widget16587490813080001\", \"type\":\"input\", \"value\":\""+account.getAccountProof()+"\"}]";
        cn.hutool.json.JSONObject param = JSONUtil.createObj();
        param.put("approval_code", ApprovalCodeEnum.TRADE_ACCOUNT_APPROVAL.getCode());
        param.put("open_id","ou_9e7dda80fd70dab01b88811ef01132d9");
        param.put("form", form);

        String json=param.toString();
        log.info("addTradingAccountApproval-param:{}",json);

        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        log.info("addTradingAccountApproval-result:{}",result);
        JSONObject object= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object.getString("data"));
        return data.getString("instance_code");
    }




    /**
     * 为没有PPD文档的项目生成对应PDD文档
     * @param projectId      项目ID
     * @param projectName    项目名称
     * @return
     */
//    public static String addProjectFile(String projectId,String projectName){
//        String tenantToken = getTenantToken();
//
//        String url="https://open.feishu.cn/open-apis/drive/explorer/v2/file/copy/files/doccnX6eWrZtQtCvvGLCNtZVRlh";
//        cn.hutool.json.JSONObject param = JSONUtil.createObj();
//        param.put("type", "doc");
//        param.put("dstFolderToken","fldcnSjlFG6i10m4fNip8zPIcSe");
//        param.put("dstName", "PDD文档模板");
//        param.put("commentNeeded", "false");
//
//        String json=param.toString();
//
//        String result = HttpUtil.createRequest(Method.POST, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();
//        JSONObject object= JSON.parseObject(result);
//        JSONObject data = JSONObject.parseObject(object.getString("data"));
//        String fileToken = data.getString("token");
//        modifyPermissions(tenantToken,fileToken);
//        System.out.println("初始化项目------");
//        updateFile(fileToken,"初始项目名称",projectName);
//        return fileToken;
//    }

    /*阿帕奇HttpClient测试*/
    public static String addProjectFile(String projectId,String projectName){
        String tenantToken = getTenantToken();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String url="https://open.feishu.cn/open-apis/drive/explorer/v2/file/copy/files/doccnX6eWrZtQtCvvGLCNtZVRlh";
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            uriBuilder.addParameter("type", "doc");
            uriBuilder.addParameter("dstFolderToken", "fldcnSjlFG6i10m4fNip8zPIcSe");
            uriBuilder.addParameter("dstName", "PDD文档模板");
            uriBuilder.addParameter("commentNeeded", "false");
            HttpPost httpPost=new HttpPost(uriBuilder.build());
            httpPost.setHeader("Authorization",tenantToken);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            String result= EntityUtils.toString(responseEntity);
            JSONObject object= JSON.parseObject(result);
            JSONObject data = JSONObject.parseObject(object.getString("data"));
            String fileToken = data.getString("token");
            modifyPermissions(tenantToken,fileToken);
            System.out.println("初始化项目------");
            updateFile(fileToken,"初始项目名称",projectName);
            return fileToken;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 上传文件到飞书root文件夹
     * @param file 文件
     * @param fileName 文件名
     * @return 文件token
     */
    public static ApiResult uploadFile(MultipartFile file, String fileName) {
        String url="https://open.feishu.cn/open-apis/drive/v1/files/upload_all";
        try {

            InputStreamResource isr = new InputStreamResource(file.getInputStream(),file.getOriginalFilename());
            String result = HttpUtil.createRequest(Method.POST, url)
                    .header(Header.AUTHORIZATION,getTenantToken())
                    .contentType("multipart/form-data")
                    .form("file_name", fileName)
                    .form("size", file.getSize())
                    .form("file", isr)
                    .form("parent_type", "explorer")
                    .form("parent_node", "fldcn1GFDTAYWVE2WgRsfIExhZg")
                    .execute().body();


            return JSONUtil.toBean(result, ApiResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    /**
//     * 返回当前用户的root文件夹路径
//     * @return rootUrl 文件夹路径
//     */
//    public static String jumpRootFolder(){
//        String tenantToken = getTenantToken();
//        String url = "https://open.feishu.cn/open-apis/drive/explorer/v2/root_folder/meta";
//        String result = HttpUtil.createRequest(Method.GET, url).header(Header.AUTHORIZATION, tenantToken).execute().body();
//        JSONObject object= JSON.parseObject(result);
//        JSONObject data = JSONObject.parseObject(object.getString("data"));
//        //获取到云空间的文件夹token
//        String folder_token = data.getString("token");
//        String rootUrl = "https://open.feishu.cn/drive/folder/"+folder_token;
//        return rootUrl;
//    }

}
