package com.stock.mx2.controller.backend;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.github.pagehelper.PageInfo;

import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.pojo.UserCashDetail;
import com.stock.mx2.pojo.UserRecharge;
import com.stock.mx2.service.IUserRechargeService;

import javax.servlet.ServletOutputStream;
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
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping({"/admin/recharge/"})
public class AdminRechargeController {

    private static final Logger log = LoggerFactory.getLogger(AdminRechargeController.class);
    @Autowired

    IUserRechargeService iUserRechargeService;

    //分頁查詢資金管理 充值列表信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "phone", required = false) String phone,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,@RequestParam(value = "applyBeginTime", required = false) String applyBeginTime, @RequestParam(value = "applyEndTime", required = false) String applyEndTime, HttpServletRequest request) {
        return this.iUserRechargeService.listByAdmin(phone,agentId, userId, realName, state, beginTime, endTime, request, pageNum, pageSize,applyBeginTime,applyEndTime);
    }


    //分頁查詢資金管理 充值列表信息及模糊查詢
    @RequestMapping({"export.do"})
    @ResponseBody
    public void export(@RequestParam(value = "phone", required = false) String phone,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime,@RequestParam(value = "applyBeginTime", required = false) String applyBeginTime, @RequestParam(value = "applyEndTime", required = false) String applyEndTime, HttpServletRequest request, HttpServletResponse response) {
        List<UserRecharge> userRechargeList = this.iUserRechargeService.exportByAdmin(phone,agentId, userId, realName, state, beginTime, endTime, request,applyBeginTime,applyEndTime);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("储值导出","储值数据"),
                UserRecharge.class, userRechargeList);
        try {
            ServletOutputStream outputStream = response.getOutputStream();

            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //分頁查詢資金管理 所有資金記錄信息及模糊查詢
    @RequestMapping({"sum.do"})
    @ResponseBody
    public ServerResponse sum(@RequestParam(value = "phone", required = false) String phone,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime,@RequestParam(value = "applyBeginTime", required = false) String applyBeginTime, @RequestParam(value = "applyEndTime", required = false) String applyEndTime, HttpServletRequest request, HttpServletResponse response) {

        UserRecharge recharge = this.iUserRechargeService.sumByAdmin(phone,agentId, userId, realName, state, beginTime, endTime, request,applyBeginTime ,applyEndTime);
        return ServerResponse.createBySuccess(recharge);

    }
    //修改資金管理 充值列表訂單狀態
    @RequestMapping({"updateState.do"})
    @ResponseBody
    public ServerResponse updateState(@RequestParam(value = "amount", required = false) BigDecimal amount, @RequestParam(value = "chargeId", required = false) Integer chargeId, @RequestParam(value = "state", required = false) Integer state) {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserRechargeService.updateState(chargeId, state,amount);
        } catch (Exception e) {
            log.error("admin修改充值訂單狀態出錯 ，異常 = {}", e);
        }
        return serverResponse;
    }


    //創建資金管理 充值訂單
    @RequestMapping({"createOrder.do"})
    @ResponseBody
    public ServerResponse createOrder(@RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "amt", required = false) Integer amt, @RequestParam(value = "payChannel", required = false) String payChannel) {
        return this.iUserRechargeService.createOrder(userId, state, amt, payChannel);
    }

    //刪除資金管理 充值列表訂單信息
    @RequestMapping({"del.do"})
    @ResponseBody
    public ServerResponse del(@RequestParam("cId") Integer cId) {
        return this.iUserRechargeService.del(cId);
    }
}
