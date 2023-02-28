package com.stock.mx2.controller.backend;


import com.github.pagehelper.PageInfo;

import com.google.common.collect.Maps;

import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.pojo.SiteAdmin;

import com.stock.mx2.service.IFileUploadService;

import com.stock.mx2.service.ISiteAdminService;

import com.stock.mx2.utils.PropertiesUtil;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping({"/admin/"})
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);


    @Autowired
    ISiteAdminService iSiteAdminService;


    @Autowired
    IFileUploadService iFileUploadService;

    //分頁查詢管理設置 所有管理列表數據及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "adminName", required = false) String adminName, @RequestParam(value = "adminPhone", required = false) String adminPhone, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, HttpServletRequest request) {
        return this.iSiteAdminService.listByAdmin(adminName, adminPhone, request, pageNum, pageSize);
    }

    //修改管理員狀態 鎖定管理員/解鎖管理員
    @RequestMapping({"updateLock.do"})
    @ResponseBody
    public ServerResponse updateLock(Integer adminId) {
        return this.iSiteAdminService.updateLock(adminId);
    }

    //管理設置 添加管理員
    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(SiteAdmin siteAdmin) {
        return this.iSiteAdminService.add(siteAdmin);
    }

    //管理設置 修改管理員密碼
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(SiteAdmin siteAdmin) {
        return this.iSiteAdminService.update(siteAdmin);
    }

    //查詢首頁 資金情況、持倉情況、盈虧信息、提現情況、股票信息、代理信息
    @RequestMapping({"count.do"})
    @ResponseBody
    public ServerResponse count() {
        return this.iSiteAdminService.count();
    }

    //處理圖片上傳
    @RequestMapping({"upload.do"})
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request) {

        String path = request.getSession().getServletContext().getRealPath("upload");

        ServerResponse serverResponse = this.iFileUploadService.upload(file, path);

        if (serverResponse.isSuccess()) {

            String targetFileName = serverResponse.getData().toString();

            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;


            Map fileMap = Maps.newHashMap();

            fileMap.put("uri", targetFileName);

            fileMap.put("url", url);


            return ServerResponse.createBySuccess(fileMap);

        }

        return serverResponse;

    }

    //刪除管理員
    @RequestMapping({"deleteAdmin.do"})
    @ResponseBody
    public ServerResponse deleteAdmin(Integer adminId) {
        return this.iSiteAdminService.deleteAdmin(adminId);
    }
}
