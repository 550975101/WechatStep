package com.wechat.controller;


import com.wechat.service.WechatStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CoreController {

    @Autowired
    private WechatStep wechatStep;

    @RequestMapping("/{account}/{step}")
    public String executeTask(@PathVariable String account, @PathVariable String step) {
        if (account != null && !"".equals(account) && step != null && !"".equals(step)) {
            return wechatStep.coreApp(account.trim(), step.trim());
        } else {
            return "参数不能为空";
        }
    }
}
