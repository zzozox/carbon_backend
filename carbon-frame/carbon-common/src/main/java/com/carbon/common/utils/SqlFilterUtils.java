package com.carbon.common.utils;

import cn.hutool.core.util.StrUtil;
import com.carbon.common.exception.CommonBizException;

/**
 * SQL过滤
 *
 * @author Li Jun
 * @since 2021-06-11
 **/
public class SqlFilterUtils {

    /**
     * SQL注入过滤
     * @param str  待验证的字符串
     */
    public static String sqlInject(String str){
        if(StrUtil.isBlank(str)){
            return null;
        }
        //去掉'|"|;|\字符
        str = StrUtil.replace(str, "'", "");
        str = StrUtil.replace(str, "\"", "");
        str = StrUtil.replace(str, ";", "");
        str = StrUtil.replace(str, "\\", "");
        //转换成小写
        str = str.toLowerCase();
        //非法字符
        String[] keywords = {"master", "truncate", "insert", "select", "delete", "update", "declare", "alter", "drop"};
        //判断是否包含非法字符
        for(String keyword : keywords){
            if(str.contains(keyword)){
                throw new CommonBizException("包含非法字符");
            }
        }
        return str;
    }
}
