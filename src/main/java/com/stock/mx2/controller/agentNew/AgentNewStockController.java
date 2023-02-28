 package com.stock.mx2.controller.agentNew;

 import com.stock.mx2.common.ServerResponse;

 import com.stock.mx2.pojo.Stock;
 import com.stock.mx2.service.IStockService;

 import javax.servlet.http.HttpServletRequest;

 import org.slf4j.Logger;

 import org.slf4j.LoggerFactory;

 import org.springframework.beans.factory.annotation.Autowired;

 import org.springframework.stereotype.Controller;

 import org.springframework.web.bind.annotation.RequestMapping;

 import org.springframework.web.bind.annotation.RequestParam;

 import org.springframework.web.bind.annotation.ResponseBody;


 @Controller
 @RequestMapping({"/agentNew/stock/"})
 public class AgentNewStockController {
     private static final Logger log = LoggerFactory.getLogger(AgentNewStockController.class);

     @Autowired
     IStockService iStockService;

     //查詢產品管理 所以股票信息及模糊查詢
     @RequestMapping({"list.do"})
     @ResponseBody
     public ServerResponse list(@RequestParam(value = "showState", required = false) Integer showState, @RequestParam(value = "riseWhite", required = false) Integer riseWhite, @RequestParam(value = "lockState", required = false) Integer lockState, @RequestParam(value = "code", required = false) String code, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "stockPlate", required = false) String stockPlate, @RequestParam(value = "stockType", required = false) String stockType, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, HttpServletRequest request) {
         return this.iStockService.listByAdmin(showState, lockState, code, name, stockPlate, stockType,riseWhite, pageNum, pageSize, request);
     }




 }
