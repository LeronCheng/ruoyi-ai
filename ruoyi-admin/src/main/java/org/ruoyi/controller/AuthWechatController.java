package org.ruoyi.controller;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth/wechat")
public class AuthWechatController {

    @Value("${wechat.cp.appConfigs[0].agentId}")
    private Integer agentId;
    @Value("${wechat.cp.corpId}")
    private String corpId;
    @Value("${wechat.cp.appConfigs[0].token}")
    private String sToken;
    @Value("${wechat.cp.appConfigs[0].aesKey}")
    private String sEncodingAESKey;

    @GetMapping("/code")
    public void wechat(@RequestParam(value = "code") String code, @RequestParam(value = "state")String state){
        log.info("2、企业微信回调Code：{}, state: {}", code, state);
        // 获取 SUITE_ACCESS_TOKEN
        String getSuiteTokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/service/get_suite_token";
;

    }
}
