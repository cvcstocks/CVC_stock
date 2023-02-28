package com.stock.mx2.controller.backend;


import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.service.*;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequestMapping({"/admin/log/"})
public class AdminLogsController {
    private static final Logger log = LoggerFactory.getLogger(AdminLogsController.class);

    @Autowired
    ISiteLoginLogService iSiteLoginLogService;

    @Autowired
    ISiteTaskLogService iSiteTaskLogService;

    @Autowired
    ISiteSmsLogService iSiteSmsLogService;

    @Autowired
    ISiteAmtTransLogService iSiteAmtTransLogService;

    @Autowired
    ISiteMessageService iSiteMessageService;

    //分頁查詢日誌管理 所有定時任務信息及模糊查詢
    @RequestMapping({"taskList.do"})
    @ResponseBody
    public ServerResponse taskList(@RequestParam(value = "keywork", required = false) String keywork,@RequestParam(value = "taskType", required = false) String taskType, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return this.iSiteTaskLogService.taskList(keywork,taskType, pageNum, pageSize);
    }

    //分頁查詢日誌管理 所有登陸日誌信息
    @RequestMapping({"loginList.do"})
    @ResponseBody
    public ServerResponse loginList(@RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return this.iSiteLoginLogService.loginList(userId, pageNum, pageSize);
    }

    //分頁查詢日誌管理 所有短信日誌信息
    @RequestMapping({"smsList.do"})
    @ResponseBody
    public ServerResponse smsList(@RequestParam(value = "phoneNum", required = false) String phoneNum, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return this.iSiteSmsLogService.smsList(phoneNum, pageNum, pageSize);
    }

    //分頁查詢日誌管理 所有資金互轉記錄信息及模糊查詢
    @RequestMapping({"transList.do"})
    @ResponseBody
    public ServerResponse transList(@RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return this.iSiteAmtTransLogService.transList(userId, realName, pageNum, pageSize);
    }

    //分頁查詢站內消息
    @RequestMapping({"messageList.do"})
    @ResponseBody
    public ServerResponse messageList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, HttpServletRequest request) {
        return this.iSiteMessageService.getSiteMessageByUserIdList(pageNum, pageSize, 999, request);
    }

    //刪除登陸日誌
    @RequestMapping({"delLogin.do"})
    @ResponseBody
    public ServerResponse del(Integer id, HttpServletRequest request) {
        return this.iSiteLoginLogService.del(id, request);
    }

    //刪除短信發送日誌
    @RequestMapping({"delSms.do"})
    @ResponseBody
    public ServerResponse delSms(Integer id, HttpServletRequest request) {
        return this.iSiteSmsLogService.del(id, request);
    }

    //刪除定時任務
    @RequestMapping({"delTask.do"})
    @ResponseBody
    public ServerResponse delTask(Integer id, HttpServletRequest request) {
        return this.iSiteTaskLogService.del(id, request);
    }

    //刪除定時任務
    @RequestMapping({"delMessageList.do"})
    @ResponseBody
    public ServerResponse delMessageList(Integer id, HttpServletRequest request) {
        return this.iSiteMessageService.del(id, request);
    }




}

