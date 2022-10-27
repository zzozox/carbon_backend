package com.carbon.gate.filter;

import cn.hutool.json.JSONUtil;
import com.carbon.domain.auth.api.LoginCheckApi;
import com.carbon.domain.common.ApiCode;
import com.carbon.domain.common.ApiResult;
import com.carbon.common.utils.HttpContextUtils;
import com.carbon.common.utils.JwtUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * 自定义过滤器
 * @author : Li Jun
 * @since : 2021-06-12 08:52
 **/
@Slf4j
@Component
public class MyZuulFilter extends ZuulFilter {

    @Autowired
    private LoginCheckApi loginCheckApi;

    /**
     * 定义过滤器类型
     *      pre     ：   在执行路由请求之前执行
     *      routing ：   在路由请求是调用
     *      post    ：   在routing和error过滤器之后执行
     *      error   ：   处理请求出现异常的时候执行
     * @return  返回过滤器类型
     */
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }


    /**
     * 定义过滤器的优先级    ：   数字越小,优先级越高
     * @return  过滤器的优先级
     */
    @Override
    public int filterOrder() {
        return 0;
    }


    /**
     * 判断过滤器是否需要执行
     * @return  过滤器是否需要执行
     */
    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String url = request.getRequestURI();
        log.info("请求的url:  {}",request.getRequestURL());
        //log.info("Should_Filter：URI={}",url);
        ArrayList<String> notFilterUrls = new ArrayList<>();
        notFilterUrls.add("/authCenter/auth/");
        notFilterUrls.add("/assets/carbonInformation/getRandomList");
        notFilterUrls.add("/assets/carbonInformation/add");
        notFilterUrls.add("/system/feishu");
        notFilterUrls.add("/system/feishu/approval/callback");
        notFilterUrls.add("/system/sysAccount/renew/email");
        notFilterUrls.add("/system/carbonH5Article/getPageList");
        notFilterUrls.add("/system/weTaskFissionReward/activeComplete");
        notFilterUrls.add("/assets/exchangeAccount/uploadCredential");
        notFilterUrls.add("/assets/es/");
        notFilterUrls.add("/assets/change/");
        notFilterUrls.add("/cmall/");
        notFilterUrls.add("/bmall/");
//        notFilterUrls.add("/system/");
//        notFilterUrls.add("/pad/");

        //swagger 文档
        notFilterUrls.add("/system/v2/api-docs");
        notFilterUrls.add("/assets/v2/api-docs");
        notFilterUrls.add("/trade/v2/api-docs");
        notFilterUrls.add("/authCenter/v2/api-docs");
        notFilterUrls.add("/workbench/v2/api-docs");
        notFilterUrls.add("/cmall/v2/api-docs");
        notFilterUrls.add("/bmall/v2/api-docs");
        for (String notFilterUrl : notFilterUrls) {
            if (url.startsWith(notFilterUrl)){
                //log.info("Not_Filter_Url:{}",url);
                return false;
            }
        }

        return true;
    }

    /**
     * 过滤器中负责的具体业务逻辑
     * @return  返回NULL,继续向后执行
     * @throws ZuulException 异常
     */
    @Override
    public Object run() throws ZuulException {

        //获取Zuul提供的请求上下文对象
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        // option请求，直接放行
        if (request.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return null;
        }


        //log.info("Filter_Url:{}",request.getRequestURI());

        //鉴权,会先验证登录然后判断权限
        ApiResult<Boolean> permissionCheck = loginCheckApi.checkPermission(request.getRequestURI());
        log.info("permissionCheck: {}", JSONUtil.toJsonStr(permissionCheck));
        if(permissionCheck.getCode() != ApiCode.SUCCESS.getCode()){
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(HttpStatus.OK.value());
            HttpContextUtils.printJSON(ctx.getResponse(),permissionCheck);
            return null;
        }

        //校验通过，可以考虑把用户信息放入上下文，继续向后执行
        return null;
    }
}
