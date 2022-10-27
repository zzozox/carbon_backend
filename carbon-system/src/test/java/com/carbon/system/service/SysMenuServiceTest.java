package com.carbon.system.service;

import com.alibaba.fastjson.JSON;
import com.carbon.system.param.SysMenuQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SysMenuServiceTest {

    @Autowired
    SysMenuService sysMenuService;

    @Test
    public void getSysMenuList() {
        SysMenuQueryParam param = new SysMenuQueryParam();
        log.info("根据账户查询:\n===\n{}\n===\n", JSON.toJSONString(sysMenuService.getSysMenuList(param),true));

        SysMenuQueryParam param2 = new SysMenuQueryParam();
        sysMenuService.getSysMenuList(param);
        log.info("根据角色查询:\n===\n{}\n===\n", JSON.toJSONString(sysMenuService.getSysMenuList(param2),true));
    }
}
