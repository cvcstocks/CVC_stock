package com.stock.mx2.controller.backend;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.FundsDealerInstitutions;
import com.stock.mx2.pojo.FundsSecuritiesInfo;
import com.stock.mx2.pojo.FundsTradingAccount;
import com.stock.mx2.service.IFundsDealerInstitutionsService;
import com.stock.mx2.service.IFundsSecuritiesInfoService;
import com.stock.mx2.service.IFundsTradingAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/admin/funds/dealer"})
public class AdminFundsDealerInstitutionsController {
    private static final Logger log = LoggerFactory.getLogger(AdminAgentController.class);

    @Autowired
    IFundsDealerInstitutionsService iFundsDealerInstitutionsService;

    @Autowired
    IFundsSecuritiesInfoService iFundsSecuritiesInfoService;

    @Autowired
    IFundsTradingAccountService iFundsTradingAccountService;

    //配資券商機構-列表查詢
    @RequestMapping({"getDealerInstitutionsList.do"})
    @ResponseBody
    public ServerResponse getFundsDealerInstitutionsList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, @RequestParam(value = "keyword", defaultValue = "") String keyword, @RequestParam( value = "status",required = false) Integer status, HttpServletRequest request) {
        return ServerResponse.createBySuccess(this.iFundsDealerInstitutionsService.getList(pageNum,pageSize,keyword, status,request));
    }

    //配資券商機構-保存
    @RequestMapping({"saveDealerInstitutions.do"})
    @ResponseBody
    public ServerResponse saveFundsDealerInstitutions(FundsDealerInstitutions fundsDealerInstitutions) {
        return ServerResponse.createBySuccess(this.iFundsDealerInstitutionsService.save(fundsDealerInstitutions));
    }

    //配資證券信息-列表查詢
    @RequestMapping({"getSecuritiesInfoList.do"})
    @ResponseBody
    public ServerResponse getSecuritiesInfoList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, @RequestParam(value = "keyword", defaultValue = "") String keyword, HttpServletRequest request) {
        return ServerResponse.createBySuccess(this.iFundsSecuritiesInfoService.getList(pageNum, pageSize, keyword, request));
    }

    //配資證券信息-保存
    @RequestMapping({"saveSecuritiesInfo.do"})
    @ResponseBody
    public ServerResponse saveSecuritiesInfo(FundsSecuritiesInfo model) {
        return ServerResponse.createBySuccess(this.iFundsSecuritiesInfoService.save(model));
    }

    //配資證券信息-查詢可用的證券信息
    @RequestMapping({"getSecuritiesEnabledList.do"})
    @ResponseBody
    public ServerResponse getSecuritiesEnabledList() {
        return ServerResponse.createBySuccess(this.iFundsSecuritiesInfoService.getEnabledList());
    }

    //配資交易帳戶-列表查詢
    @RequestMapping({"getTradingAccountList.do"})
    @ResponseBody
    public ServerResponse getTradingAccountList(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, @RequestParam(value = "keyword", defaultValue = "") String keyword, @RequestParam(value = "status", required = false) Integer status, HttpServletRequest request) {
        return ServerResponse.createBySuccess(this.iFundsTradingAccountService.getList(pageNum, pageSize, keyword, status, request));
    }

    //配資交易帳戶-保存
    @RequestMapping({"saveTradingAccount.do"})
    @ResponseBody
    public ServerResponse saveTradingAccount(FundsTradingAccount model) {
        return ServerResponse.createBySuccess(this.iFundsTradingAccountService.save(model));
    }

    //配資交易帳戶-查詢最新交易帳戶編號
    @RequestMapping({"getMaxNumber.do"})
    @ResponseBody
    public ServerResponse getMaxNumber() {
        return this.iFundsTradingAccountService.getMaxNumber();
    }

}
