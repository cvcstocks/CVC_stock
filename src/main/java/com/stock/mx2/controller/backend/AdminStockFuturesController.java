//package com.stock.mx2.controller.backend;
//
//import com.stock.mx2.common.ServerResponse;
//import com.stock.mx2.pojo.StockFutures;
//import com.stock.mx2.service.IStockFuturesService;
//åimport com.stock.mx2.service.IUserFuturesPositionService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//
//@Controller
//@RequestMapping({"/admin/futures/"})
//public class AdminStockFuturesController {
//    private static final Logger log = LoggerFactory.getLogger(AdminStockFuturesController.class);
//
//    @Autowired
//    IStockFuturesService iStockFuturesService;
//
//    @Autowired
//    IUserFuturesPositionService iUserFuturesPositionService;
//
//    @RequestMapping({"list.do"})
//    @ResponseBody
//    public ServerResponse list(@RequestParam(value = "fuName", required = false) String fuName, @RequestParam(value = "fuCode", required = false) String fuCode, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
//        return this.iStockFuturesService.listByAdmin(fuName, fuCode, pageNum, pageSize);
//    }
//
//    //添加期貨產品
//    @RequestMapping({"add.do"})
//    @ResponseBody
//    public ServerResponse add(StockFutures stockFutures) {
//        return this.iStockFuturesService.add(stockFutures);
//    }
//
//    //修改期貨產品
//    @RequestMapping({"update.do"})
//    @ResponseBody
//    public ServerResponse update(StockFutures stockFutures) {
//        return this.iStockFuturesService.update(stockFutures);
//    }
//
//    @RequestMapping({"sell.do"})
//    @ResponseBody
//    public ServerResponse sell(String positionSn) throws Exception {
//        ServerResponse serverResponse = null;
//
//        try {
//            serverResponse = this.iUserFuturesPositionService.sellFutures(positionSn, 0);
//        } catch (Exception e) {
//            log.error("強制平倉 異常信息 = {}", e);
//        }
//        return serverResponse;
//    }
//
//    @RequestMapping({"lock.do"})
//    @ResponseBody
//    public ServerResponse lock(@RequestParam("positionId") Integer positionId, @RequestParam("state") Integer state, @RequestParam(value = "lockMsg", required = false) String lockMsg) {
//        return this.iUserFuturesPositionService.lock(positionId, state, lockMsg);
//    }
//}
