package com.stock.mx2.controller.backend;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteArticle;
import com.stock.mx2.service.ISiteArticleService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping({"/admin/art/"})
public class AdminSiteArticleController {
    private static final Logger log = LoggerFactory.getLogger(AdminSiteArticleController.class);

    @Autowired
    ISiteArticleService iSiteArticleService;

    //查詢系統基本設置 所有公告設置信息
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "artTitle", required = false) String artTitle, @RequestParam(value = "artType", required = false) String artType, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, HttpServletRequest request) {
        return this.iSiteArticleService.listByAdmin(artTitle, artType, pageNum, pageSize);
    }

    //添加系統基本設置 公共信息
    @RequestMapping({"add.do"})
    @ResponseBody
    public ServerResponse add(SiteArticle siteArticle) {
        return this.iSiteArticleService.add(siteArticle);
    }

    //修改系統基本設置 公共信息
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(SiteArticle siteArticle) {
        return this.iSiteArticleService.update(siteArticle);
    }

    //查看指定系統基本設置 公共信息
    @RequestMapping({"detail.do"})
    @ResponseBody
    public ServerResponse detail(Integer artId) {
        return this.iSiteArticleService.detail(artId);
    }

    //刪除系統公告
    @RequestMapping({"delArt.do"})
    @ResponseBody
    public ServerResponse del(Integer artId) {
        return this.iSiteArticleService.del(artId);
    }
}
