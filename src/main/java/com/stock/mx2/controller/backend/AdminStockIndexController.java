package com.stock.mx2.controller.backend;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.StockIndex;
import com.stock.mx2.service.IStockIndexService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping({"/admin/index/"})
public class AdminStockIndexController {
    private static final Logger log = LoggerFactory.getLogger(AdminStockIndexController.class);

    @Autowired
    IStockIndexService iStockIndexService;

    //分頁查詢產品管理 所以指數信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "homeShow", required = false) Integer homeShow, @RequestParam(value = "listShow", required = false) Integer listShow, @RequestParam(value = "transState", required = false) Integer transState, @RequestParam(value = "indexCode", required = false) String indexCode, @RequestParam(value = "indexName", required = false) String indexName, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, HttpServletRequest request) {
        return this.iStockIndexService.listByAdmin(homeShow, listShow, transState, indexCode, indexName, pageNum, pageSize, request);
    }

    //修改產品管理 指數信息
    @RequestMapping({"updateIndex.do"})
    @ResponseBody
    public ServerResponse updateIndex(StockIndex stockIndex) {
        return this.iStockIndexService.updateIndex(stockIndex);
    }

    //添加產品管理 指數信息
    @RequestMapping({"addIndex.do"})
    @ResponseBody
    public ServerResponse addIndex(StockIndex stockIndex) {
        return this.iStockIndexService.addIndex(stockIndex);
    }
}
