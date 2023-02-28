package com.stock.mx2.controller.protol;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.stock.mx2.annotation.SameUrlData;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.User;
import com.stock.mx2.pojo.UserRecharge;
import com.stock.mx2.pojo.UserWithdraw;
import com.stock.mx2.service.IUserService;
import com.stock.mx2.service.IUserWithdrawService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;


@Controller
@RequestMapping({"/user/withdraw/"})
public class UserWithdrawController {
    private static final Logger log = LoggerFactory.getLogger(UserWithdrawController.class);

    @Autowired
    IUserWithdrawService iUserWithdrawService;

    @Autowired
    IUserService iUserService;

    //分頁查詢所有提現記錄
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "8") int pageSize, @RequestParam(value = "withStatus", required = false) String withStatus, HttpServletRequest request) {
        return this.iUserWithdrawService.findUserWithList(withStatus, request, pageNum, pageSize);
    }



    //用戶提現
    @RequestMapping({"outMoney.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse outMoney(String amt, HttpServletRequest request) {
        ServerResponse serverResponse = null;
        User user = this.iUserService.getCurrentRefreshUser(request);
        if (user == null){
            return ServerResponse.createBySuccessMsg("請先登錄");
        }
        try {
            serverResponse = this.iUserWithdrawService.outMoney(amt, user.getWithPwd(), request);
        } catch (Exception e) {
            log.error("出金異常 e = {}", e);
            serverResponse = ServerResponse.createByErrorMsg("出金異常，請稍後再試");
        }
        return serverResponse;
    }

    @RequestMapping({"cancel.do"})
    @ResponseBody
    @SameUrlData
    public ServerResponse userCancel(Integer withId) {
        return this.iUserWithdrawService.userCancel(withId);
    }
}
