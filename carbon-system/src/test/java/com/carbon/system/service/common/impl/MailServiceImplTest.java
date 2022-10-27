package com.carbon.system.service.common.impl;

import com.carbon.system.service.common.MailService;
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
public class MailServiceImplTest extends TestCase {

    @Autowired
    private MailService mailService;

    @Test
    public void testSimple() {
        mailService.simple("lijuncode@163.com","测试","测试邮件");
    }

    @Test
    public void testHtml() {
    }

    @Test
    public void testAttach() {
    }
}