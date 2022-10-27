package com.carbon.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.carbon.system.vo.StatHomeDataVo;
import com.carbon.system.service.DataPanelService;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataPanelServiceImplTest extends TestCase {

    @Autowired
    DataPanelService dataPanelService;

    @Test
    public void getYesterdayData() {
        StatHomeDataVo yesterdayData = dataPanelService.getHomeData();
        log.info("getYesterdayData:\n===\n{}\n===\n", JSON.toJSONString(yesterdayData,true));
    }
}
