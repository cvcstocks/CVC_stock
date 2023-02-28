 package com.stock.mx2.controller.protol;


 import com.stock.mx2.common.ServerResponse;
 import com.stock.mx2.service.ISiteMessageService;
 import com.stock.mx2.service.IUserCashDetailService;
 import javax.servlet.http.HttpServletRequest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;

 @Controller
 @RequestMapping({"/user/cash/"})
 public class UserCashDetailController {
     @Autowired
     IUserCashDetailService iUserCashDetailService;
     @Autowired
     ISiteMessageService iSiteMessageService;

     //查詢所有資產明細
     @RequestMapping({"list.do"})
     @ResponseBody
     public ServerResponse list(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "positionId", required = false) Integer positionId) {
         return this.iUserCashDetailService.findUserCashDetailList(positionId, request, pageNum, pageSize);
     }

     //查詢所有資產明細
     @RequestMapping({"futList.do"})
     @ResponseBody
     public ServerResponse futList(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "positionId", required = false) Integer positionId) {
         return this.iUserCashDetailService.findUserFutCashDetailList(positionId, request, pageNum, pageSize);
     }

     //查詢用戶站內消息
     @RequestMapping({"getMessagelist.do"})
     @ResponseBody
     public ServerResponse getMessagelist(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "userId", required = false) Integer userId) {
         return this.iSiteMessageService.getSiteMessageByUserIdList(pageNum, pageSize, userId,request);
     }

     //用戶站內消息狀態變已讀
     @RequestMapping({"updateMessageStatus.do"})
     @ResponseBody
     public ServerResponse updateMessageStatus(HttpServletRequest request) {
         this.iSiteMessageService.updateMessageStatus(request);
         return ServerResponse.createBySuccess("查看成功");
     }

     //查詢用戶未讀消息數
     @RequestMapping({"getUnreadCount.do"})
     @ResponseBody
     public ServerResponse getUnreadCount(HttpServletRequest request) {
         int k = this.iSiteMessageService.getUnreadCount(request);
         return ServerResponse.createBySuccess(k);
     }

 }
