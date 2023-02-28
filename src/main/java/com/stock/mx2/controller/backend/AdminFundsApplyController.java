package com.stock.mx2.controller.backend;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.FundsAppend;
import com.stock.mx2.pojo.FundsApply;
import com.stock.mx2.service.IFundsAppendService;
import com.stock.mx2.service.IFundsApplyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/admin/funds/apply/"})
public class AdminFundsApplyController {
    private static final Logger log = LoggerFactory.getLogger(AdminAgentController.class);
    @Autowired
    IFundsApplyService iFundsApplyService;

    @Autowired
    IFundsAppendService iFundsAppendService;

    //配資申請-列表查詢
    @RequestMapping({"getApplyList.do"})
    @ResponseBody
    public ServerResponse getApplyList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, @RequestParam(value = "keyword", defaultValue = "") String keyword, @RequestParam(value = "status", required = false) Integer status, HttpServletRequest request) {
        return ServerResponse.createBySuccess(this.iFundsApplyService.getList(pageNum, pageSize, keyword, status, request));
    }

    //配資申請-保存
    @RequestMapping({"saveApply.do"})
    @ResponseBody
    public ServerResponse saveApply(FundsApply model) {
        return ServerResponse.createBySuccess(this.iFundsApplyService.save(model));
    }

    //配資申請-審核
    @RequestMapping({"auditApply.do"})
    @ResponseBody
    public ServerResponse auditApply(FundsApply fundsApply, HttpServletRequest request) throws Exception  {
        return this.iFundsApplyService.audit(fundsApply, request);
    }

    //配資追加申請-查詢用戶追加列表
    @RequestMapping({"getAppendList.do"})
    @ResponseBody
    public ServerResponse getAppendList(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "appendType", required = false) Integer appendType) {
        return this.iFundsAppendService.getList(pageNum, pageSize, keyword, status, userId, appendType, request);
    }

    //配資追加申請-查詢詳情
    @RequestMapping({"getAppendDetail.do"})
    @ResponseBody
    public ServerResponse getAppendDetail(Integer id) {
        return this.iFundsAppendService.getDetail(id);
    }

    //配資追加申請-保存
    @RequestMapping({"saveAppendApply.do"})
    @ResponseBody
    public ServerResponse saveAppendApply(FundsAppend fundsApply, HttpServletRequest request) throws Exception {
        return this.iFundsAppendService.save(fundsApply, request);
    }


}
