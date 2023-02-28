package com.stock.mx2.controller.protol;


import com.stock.mx2.annotation.SameUrlData;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.FundsAppend;
import com.stock.mx2.pojo.FundsApply;
import com.stock.mx2.pojo.UserFundsPosition;
import com.stock.mx2.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Controller
@RequestMapping({"/user/funds/"})
public class UserFundsController {
    private static final Logger log = LoggerFactory.getLogger(UserFundsController.class);

    @Autowired
    IFundsSettingService iFundsSettingService;

    @Autowired
    IFundsLeverService iFundsLeverService;

    @Autowired
    IFundsApplyService iFundsApplyService;

    @Autowired
    IUserFundsPositionService iUserFundsPositionService;

    @Autowired
    IFundsAppendService iFundsAppendService;

    //分倉配資設置信息查詢
    @RequestMapping({"getFundsSetting.do"})
    @ResponseBody
    public ServerResponse getFundsSetting() {
        return ServerResponse.createBySuccess(this.iFundsSettingService.getFundsSetting());
    }

    //查詢配資類型槓桿
    @RequestMapping({"getFundsTypeList.do"})
    @ResponseBody
    public ServerResponse getFundsTypeList(Integer cycleType) {
        return this.iFundsLeverService.getFundsTypeList(cycleType);
    }

    //配資申請-添加
    @RequestMapping({"addFundsApply.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse addFundsApply(FundsApply fundsApply, HttpServletRequest request) throws Exception {
        return this.iFundsApplyService.insert(fundsApply, request);
    }

    //配資申請-用戶配資列表
    @RequestMapping({"getUserApplyList.do"})
    @ResponseBody
    public ServerResponse getUserApplyList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, @RequestParam(value = "userId", defaultValue = "") int userId, HttpServletRequest request) {
        return this.iFundsApplyService.getUserApplyList(pageNum, pageSize, userId, request);
    }

    //配資申請-用戶操盤中子帳戶
    @RequestMapping({"getUserSubaccount.do"})
    @ResponseBody
    public ServerResponse getUserSubaccount(HttpServletRequest request) {
        return this.iFundsApplyService.getUserEnabledSubaccount(request);
    }

    //分倉交易-用戶平倉操作
    @RequestMapping({"sellFunds.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse sellFunds(HttpServletRequest request, @RequestParam("positionSn") String positionSn) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserFundsPositionService.sellFunds(positionSn, 1);
        } catch (Exception e) {
            log.error("用戶平倉操作 = {}", e);
        }
        return serverResponse;
    }

    //分倉交易-查詢所有平倉/持倉信息
    @RequestMapping({"fundsList.do"})
    @ResponseBody
    public ServerResponse fundsList(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "stockCode", required = false) String stockCode, @RequestParam(value = "stockSpell", required = false) String stockSpell) {
        return this.iUserFundsPositionService.findMyPositionByCodeAndSpell(stockCode, stockSpell, state, request, pageNum, pageSize);
    }

    //查詢子帳戶詳情
    @RequestMapping({"getSubaccountInfo.do"})
    @ResponseBody
    public ServerResponse getSubaccountInfo(Integer id) {
        return this.iFundsApplyService.getDetail(id);
    }

    /**
     * 配資槓桿-查詢槓桿費率
     * cycleType：周期類型：1按天、2按周、3按月
     * lever：槓桿
     */
    @RequestMapping({"getLeverRateInfo.do"})
    @ResponseBody
    public ServerResponse getLeverRateInfo(Integer cycleType, Integer lever) {
        return this.iFundsLeverService.getLeverRateInfo(cycleType, lever);
    }

    //配資追加申請-保存
    @RequestMapping({"appendApply.do"})
    @ResponseBody
    public ServerResponse appendApply(FundsAppend fundsApply, HttpServletRequest request) throws Exception {
        return this.iFundsAppendService.save(fundsApply, request);
    }

    //配資追加申請-查詢用戶追加列表
    @RequestMapping({"getAppendList.do"})
    @ResponseBody
    public ServerResponse getAppendList(HttpServletRequest request, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "status", required = false) Integer status, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "appendType", required = false) Integer appendType) {
        return this.iFundsAppendService.getList(pageNum, pageSize, keyword, status, userId, appendType, request);
    }

    //根據分倉配資代碼查詢用戶最早入倉股票
    @RequestMapping({"findUserFundsPositionByCode.do"})
    @ResponseBody
    public ServerResponse findUserFundsPositionByCode(HttpServletRequest request, @RequestParam(value = "fundsCode", required = false) String fundsCode) {
        return this.iUserFundsPositionService.findUserFundsPositionByCode(request, fundsCode);
    }



}
