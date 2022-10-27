/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.carbon.gate.filter;


import com.alibaba.fastjson.JSON;
import com.carbon.common.utils.HttpContextUtils;
import com.carbon.domain.common.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMethod;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 设置允许跨域
 *
 */
@Slf4j
public class CrossDomainFilter implements Filter {

//    @NacosValue(value = "${allowUrls}", autoRefreshed = true)
//    private String allowUrls;

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.addHeader("Access-Control-Allow-Methods", "*");
//        httpServletResponse.addHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With,Authorization,token,Origin,Accept");
        httpServletResponse.addHeader("Access-Control-Allow-Headers", "*");
        httpServletResponse.addHeader("Access-Control-Request-Headers", "*");
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "*");
        httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");

        String method = request.getMethod();
        if (RequestMethod.OPTIONS.name().equals(method)) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            HttpContextUtils.printJSON(response,ApiResult.ok());
            return;
        }
        filterChain.doFilter(servletRequest, httpServletResponse);
    }

    @Override
    public void destroy() {

    }
}
