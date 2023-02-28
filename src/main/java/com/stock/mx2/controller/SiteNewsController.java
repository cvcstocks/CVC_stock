package com.stock.mx2.controller;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteNews;
import com.stock.mx2.service.ISiteNewsService;
import com.stock.mx2.service.IUserPositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/api/news/"})
public class SiteNewsController {
    private static final Logger log = LoggerFactory.getLogger(SiteNewsController.class);
    @Autowired
    ISiteNewsService iSiteNewsService;

    @Autowired
    IUserPositionService iUserPositionService;

    //新聞資訊-列表查詢
    @RequestMapping({"getNewsList.do"})
    @ResponseBody
    public ServerResponse getNewsList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "15") int pageSize, @RequestParam(value = "type", defaultValue = "0") Integer type, @RequestParam(value = "sort", defaultValue = "time1") String sort, @RequestParam(value = "keyword", required = false) String keyword, HttpServletRequest request) {
        return this.iSiteNewsService.getList(pageNum, pageSize, type, sort, keyword, request);
    }


    //新聞資訊-列表查詢
    @RequestMapping({"getNewsById.do"})
    @ResponseBody
    public ServerResponse getNewsById(String id) {
        return this.iSiteNewsService.getNewsById(id);
    }

    //新聞資訊-詳情
    @RequestMapping({"getDetail.do"})
    @ResponseBody
    public ServerResponse getDetail(int id) {
        return this.iSiteNewsService.getDetail(id);
    }

    //新聞資訊-修改新聞瀏覽量
    @RequestMapping({"updateViews.do"})
    @ResponseBody
    public ServerResponse updateViews(Integer id) {
        return this.iSiteNewsService.updateViews(id);
    }

    //新聞資訊-列表查詢
    @RequestMapping({"getTopNews.do"})
    @ResponseBody
    public ServerResponse getTopNewsList(@RequestParam(value = "pageSize", defaultValue = "15") int pageSize) {
        return this.iSiteNewsService.getTopNewsList(pageSize);
    }

    //新聞資訊-列表查詢
    @RequestMapping({"getPositionTop.do"})
    @ResponseBody
    public ServerResponse findPositionTopList(@RequestParam(value = "pageSize", defaultValue = "15") int pageSize) {
        return this.iUserPositionService.findPositionTopList(pageSize);
    }

}
