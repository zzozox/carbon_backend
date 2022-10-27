package com.carbon.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carbon.common.entity.AddResponse;
import com.carbon.common.exception.CommonBizException;
import com.carbon.common.feishu.FeiShuAPI;
import com.carbon.common.redis.RedisService;
import com.carbon.domain.common.constant.RedisKeyName;
import com.carbon.system.config.OkHttpCli;
import com.carbon.system.entity.CarbonArticle;
import com.carbon.system.entity.CarbonArticleComment;
import com.carbon.system.mapper.CarbonArticleCommentMapper;
import com.carbon.system.mapper.CarbonArticleMapper;
import com.carbon.system.param.CarbonArticleStatuParam;
import com.carbon.system.service.CarbonArticleService;
import com.carbon.system.param.CarbonArticleQueryParam;
import com.carbon.system.service.FeishuFiletokenService;
import com.carbon.system.util.CommonUtil;
import com.carbon.system.vo.CarbonArticleAddVo;
import com.carbon.system.vo.CarbonArticleQueryVo;
import com.carbon.common.service.BaseServiceImpl;
import com.carbon.common.api.Paging;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.*;
import org.apache.http.client.methods.HttpHead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * 碳文章 服务实现类
 * </p>
 *
 * @author Li Jun
 * @since 2021-08-01
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class CarbonArticleServiceImpl extends BaseServiceImpl<CarbonArticleMapper, CarbonArticle> implements CarbonArticleService {

    @Resource
    private CarbonArticleMapper carbonArticleMapper;
    @Resource
    CarbonArticleCommentMapper commentMapper;

    @Resource
    private RedisService redisService;

    @Resource
    private FeishuFiletokenService feishuFiletokenService;

    @Autowired
    RestTemplate restTemplate;



    @Override
    public CarbonArticleQueryVo getCarbonArticleById(Serializable id) {
        CarbonArticle article = carbonArticleMapper.selectById(id);
        CarbonArticleQueryVo articleQueryVo =new CarbonArticleQueryVo();
        BeanUtil.copyProperties(article,articleQueryVo);
        return articleQueryVo;
    }

    @Override
    public Paging<CarbonArticleQueryVo> getCarbonArticlePageList(CarbonArticleQueryParam param) {
        IPage<CarbonArticleQueryVo> iPage = carbonArticleMapper.getCarbonArticlePageList(getPage(param),param);
        iPage.getRecords().stream().forEach(e->{
            LambdaQueryWrapper<CarbonArticleComment> commentLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commentLambdaQueryWrapper.eq(CarbonArticleComment::getArticleId,e.getId());
            e.setCommentNumber( commentMapper.selectCount(commentLambdaQueryWrapper));
        });
        return new Paging<>(iPage);
    }

    @Override
    public Paging<CarbonArticleQueryVo> getCarbonArticlePageList2(CarbonArticleQueryParam param) {
        //先查redis
        String key = RedisKeyName.CA_ARTICLE_LIST_KEY;
        String result = redisService.get(RedisKeyName.CA_ARTICLE_LIST_KEY);
        //如果不空
        //不存在查数据库
        IPage<CarbonArticleQueryVo> iPage = carbonArticleMapper.getCarbonArticlePageList(getPage(param),param);
        //写入redis
        Paging<CarbonArticleQueryVo> voPaging = new Paging<>(iPage);
        redisService.setEx(key, JSONUtil.toJsonStr(voPaging),1L, TimeUnit.DAYS);//过期1天
        return voPaging;
    }

    @Override
    public Paging<CarbonArticleQueryVo> getCarbonArticlePageList3(CarbonArticleQueryParam param) {
        //根据字段降序（暂时没用，后序再看）
        Page<?> page = getPage(param);
        page.addOrder(OrderItem.desc("updated_time"));
        IPage<CarbonArticleQueryVo> iPage = carbonArticleMapper.getCarbonArticlePageList3(getPage(param),param);
        return new Paging<>(iPage);
    }

    @Override
    public boolean updateArticleStatu(CarbonArticleStatuParam param) {
        //先根据id查询文章
        CarbonArticle article = carbonArticleMapper.selectById(param.getId());

        article.setStatus(param.getStatus());
        carbonArticleMapper.updateById(article);
        //更新完数据库后删除redis缓存
        redisService.delete(Collections.singleton(RedisKeyName.CA_ARTICLE_LIST_KEY));
        return true;
    }

    @Override
    public CarbonArticleAddVo testFeishu() {
        AddResponse addResponse = FeiShuAPI.addArticle();
        String token = addResponse.getObjToken();
        String url = addResponse.getUrl();
        CarbonArticleAddVo vo=new CarbonArticleAddVo();
        vo.setUrl(url);
        vo.setToken(token);
        return vo;
    }



    //这里本应该用上面的飞书接口，但是部署上线后会有调不通的情况，故先写死接口，后期可以用上面的
    @Override
    public CarbonArticleAddVo testFeishu2() {
        String tenantToken = getTenantToken();
        String url="https://open.feishu.cn/open-apis/doc/v2/create";
        String result = HttpUtil.createRequest(Method.POST, url).
                header(Header.AUTHORIZATION, tenantToken).execute().body();
        JSONObject object2= JSON.parseObject(result);
        JSONObject data = JSONObject.parseObject(object2.getString("data"));
        String objToken = data.getString("objToken");
        modifyPermissions(tenantToken,objToken);
        String url2 = data.getString("url");
        CarbonArticleAddVo vo=new CarbonArticleAddVo();
        vo.setUrl(url2);
        vo.setToken(objToken);

        CarbonArticle carbonArticle = new CarbonArticle();
        carbonArticle.setUrl(url2);
        this.save(carbonArticle);

        return vo;
    }

    /**
     * 将文章保存到飞书文档
     */
    @Override
    public CarbonArticleAddVo pushFeishu(CarbonArticle carbonArticle) {
        log.info("===> push carbonArticle:{}",JSONUtil.toJsonStr(carbonArticle));

        String content = carbonArticle.getContent();
        String title = carbonArticle.getTitle();
        // 创建文章需要传的json
        /**
         * {
         *     "title": {
         *     "elements":  [
         *             {   "type": "textRun",
         *                 "textRun": {
         *                     "text": "%s"
         *                 }
         *             }
         *         ]
         *     },
         *     "body": {
         *         "blocks": [
         *             {
         *             "type": "paragraph",
         *             "paragraph": {
         *                 "elements":  [
         *                     {
         *                         "type": "textRun",
         *                         "textRun": {
         *                             "text": "%s"
         *                         }
         *                     }
         *                     ]
         *                 }
         *             }
         *         ]
         *     }
         * }
         *
         */
        String body = "{\"title\":{\"elements\":[{\"type\":\"textRun\",\"textRun\":{\"text\":\"%s\"}}]},\"body\":{\"blocks\":[{\"type\":\"paragraph\",\"paragraph\":{\"elements\":[{\"type\":\"textRun\",\"textRun\":{\"text\":\"%s\"}}]}}]}}";

        String formatBody = String.format(body, title, content);
        String tenantToken = getTenantToken();
        String createUrl="https://open.feishu.cn/open-apis/doc/v2/create";

        // head
        MultiValueMap<String, String> header = new LinkedMultiValueMap();
        header.add(Header.AUTHORIZATION.getValue(), tenantToken);
        header.add(HttpHeaders.CONTENT_TYPE,org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE);
        // requestBody
        Map<String,String> requestBody = new HashMap<>();
        requestBody.put("Content",formatBody);
        try {
            RequestEntity request = RequestEntity
                    .post(new URI(createUrl))
                    .header(Header.AUTHORIZATION.getValue(),tenantToken)
                    .header(HttpHeaders.CONTENT_TYPE,org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(requestBody);

            ResponseEntity<JSONObject> response = restTemplate.exchange(request, JSONObject.class);
            log.info("===> responseBody:{}",response.getBody());
            Integer code = response.getBody().getInteger("code");
            String msg = response.getBody().getString("msg");
            if(code!=0){
                throw new CommonBizException("调用飞书api失败,msg:"+msg);
            }
            JSONObject data = response.getBody().getJSONObject("data");
            String objToken = data.getString("objToken");
            modifyPermissions(tenantToken,objToken);
            String url = data.getString("url");
            log.info("====> objToken:{},url:{}",objToken,url);
            // 将URL填入，存表
            carbonArticle.setUrl(url);
            this.save(carbonArticle);

            // 返回vo
            CarbonArticleAddVo vo=new CarbonArticleAddVo();
            vo.setUrl(url);
            vo.setToken(objToken);
            return vo;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
//        String result = HttpUtil.createRequest(Method.POST, createUrl).
//                header(Header.AUTHORIZATION, tenantToken)
//                .body(requestBody)  // body内容
//                .execute()
//                .body();
//        JSONObject object2= JSON.parseObject(result);
//        JSONObject data = JSONObject.parseObject(object2.getString("data"));
//        String objToken = data.getString("objToken");
//        modifyPermissions(tenantToken,objToken);
//        String url = data.getString("url");
//        // 将URL填入，存表
//        carbonArticle.setUrl(url);
//        this.save(carbonArticle);
//
//        // 返回vo
//        CarbonArticleAddVo vo=new CarbonArticleAddVo();
//        vo.setUrl(url);
//        vo.setToken(objToken);
//        return vo;
    }

    /**
     * 监听content变化更新飞书文档
     */
    @Override
    public CarbonArticleAddVo updateFeishu(CarbonArticle article) {
        return null;
    }

    /*
    飞书文章同步
     */
    @Override
    public void SyncArticle(List<Map<String, Object>> articleUrlList)
    {
        //循环拉取飞书数据到本地数据库
        for(int i=0;i<articleUrlList.size();i++)
        {
            String tenantToken = CarbonArticleServiceImpl.getTenantToken();
            String url="https://open.feishu.cn/open-apis/doc/v2/meta/"+CommonUtil.getArticleToken(articleUrlList.get(i).get("url").toString());
            String result = HttpUtil.createRequest(Method.GET, url).header(Header.AUTHORIZATION, tenantToken)
                    .execute().body();
            System.out.println(result+url+articleUrlList.get(i).get("id"));
            JSONObject object1= JSON.parseObject(result);
            //元数据结果集
            JSONObject data = JSONObject.parseObject(object1.getString("data"));

            String tenantToken2 = CarbonArticleServiceImpl.getTenantToken();
            String contenturl="https://open.feishu.cn/open-apis/doc/v2/"+CommonUtil.getArticleToken(articleUrlList.get(i).get("url").toString())+"/raw_content";
            String result2 = HttpUtil.createRequest(Method.GET, contenturl).header(Header.AUTHORIZATION, tenantToken2)
                    .execute().body();
            JSONObject object2= JSON.parseObject(result2);

            //文章信息结果集
            JSONObject data2 = JSONObject.parseObject(object2.getString("data"));
            Long id=(Long) articleUrlList.get(i).get("id");
            String title=data.getString("title");
            String author=data.getString("edit_user_name");
            String content=data2.getString("content");
            String status="0260000001";
            String category_id="0180000002"; //根据文章标题判断类型
            if(-1!=title.indexOf("产品更新")||-1!=title.indexOf("平台动态")){
                category_id="0180000003"; //平台公告
            } else if (-1!=title.indexOf("《")||-1!=title.indexOf("【")) {
                category_id="0180000001";//行业动态
            } else if (-1!=title.indexOf("?")||-1!=title.indexOf("？")) {
                category_id="0180000004";//常见问题
            } else {
                category_id="0180000002";//行业知识库
            }

            //todo

            String updated_time=CommonUtil.getTime(Long.parseLong(data.getString("edit_time")));
            //排除非更新项
            if(!articleUrlList.get(i).get("updated_time").toString().replace(".0","").equals(updated_time))
            {
                carbonArticleMapper.SyncArticle(id,title,author,content,status,category_id,updated_time);
            }
        }
    }

    /**
     * 获取tenant_access_token
     * @return
     */
    public static String getTenantToken(){
        Map map=new HashMap();
        map.put("app_id","cli_a3c8c08a7fb05013");
        map.put("app_secret","b7MktQdNpROIqi8sIeyNPWLfcK366Lp6");
        String url="https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";
        String tokenStr = HttpUtil.post(url, map);
        JSONObject object= JSON.parseObject(tokenStr);
        String token = object.getString("tenant_access_token");
        return "Bearer "+token;
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



//        String url="https://open.feishu.cn/open-apis/drive/v1/permissions/"+fileToken+"/public?type=doc";
//        HttpUtil.createRequest(Method.PATCH, url).header(Header.AUTHORIZATION, tenantToken).body(json).execute().body();

        //使用okhttp替换httpClient
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {

                    String url="https://open.feishu.cn/open-apis/drive/v1/permissions/"+fileToken+"/public?type=doc";
                    patchUrl(url, tenantToken, json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }
    static void patchUrl(String url, String tenantToken, String json) throws IOException {

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        OkHttpClient client = new OkHttpClient();//创建一个OkHttpClient实例
        Request request=new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("Authorization", tenantToken)
                .build();
        Response response=client.newCall(request).execute();
        String responseData=response.body().string();
        System.out.println(responseData);
    }


}
