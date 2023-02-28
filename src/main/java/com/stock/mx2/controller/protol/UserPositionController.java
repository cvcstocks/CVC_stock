 package com.stock.mx2.controller.protol;

 import com.stock.mx2.common.ServerResponse;
 import com.stock.mx2.service.IUserPositionService;
 import javax.servlet.http.HttpServletRequest;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;

 @Controller
 @RequestMapping({"/user/position/"})
 public class UserPositionController {
     private static final Logger log = LoggerFactory.getLogger(UserPositionController.class);

     @Autowired
     IUserPositionService iUserPositionService;



     //查詢所有融資平倉/持倉信息 --- 此接口棄用
     @RequestMapping({"list.do"})
     @ResponseBody
     public ServerResponse list(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "stockCode", required = false) String stockCode, @RequestParam(value = "stockSpell", required = false) String stockSpell) {
         return this.iUserPositionService.findMyPositionByCodeAndSpellV1(stockCode, stockSpell, state, request, pageNum, pageSize);
     }



     //查詢所有融資平倉/持倉信息
     @RequestMapping({"listV2.do"})
     @ResponseBody
     public ServerResponse newListV2(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "stockCode", required = false) String stockCode, @RequestParam(value = "stockSpell", required = false) String stockSpell) {
         return this.iUserPositionService.findMyPositionByCodeAndSpellV2(stockCode, stockSpell, state, request, pageNum, pageSize);
     }
     //根據股票代碼查詢用戶最早入倉股票
     @RequestMapping({"findUserPositionByCode.do"})
     @ResponseBody
     public ServerResponse findUserPositionByCode(HttpServletRequest request, @RequestParam(value = "stockCode", required = false) String stockCode) {
         return this.iUserPositionService.findUserPositionByCode(request, stockCode);
     }


     //查詢所有自選股票信息及模糊查詢
     @RequestMapping({"sum.do"})
     @ResponseBody
     public ServerResponse sum(HttpServletRequest request) {
         return this.iUserPositionService.findMyPositionByCodeAndSpellSum(request);
     }
 }

