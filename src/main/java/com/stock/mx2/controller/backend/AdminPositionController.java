package com.stock.mx2.controller.backend;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.pojo.UserPosition;
import com.stock.mx2.service.IUserPositionService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stock.mx2.vo.position.AdminPositionVO;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping({"/admin/position/"})
public class AdminPositionController {
    private static final Logger log = LoggerFactory.getLogger(AdminPositionController.class);

    @Autowired
    IUserPositionService iUserPositionService;

    //分頁查詢持倉管理 融資持倉單信息/融資平倉單信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(Integer sum,String sort,String sortColmn,Integer buyType,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "positionSn", required = false) String positionSn, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, HttpServletRequest request) {
        if (sum == null){
            sum = 0;
        }
        return this.iUserPositionService.listByAdmin(sum,sort,sortColmn,agentId, positionType, state, userId, positionSn, beginTime, endTime, pageNum, pageSize,buyType);
    }

    //分頁查詢持倉管理 融資持倉單信息/融資平倉單信息及模糊查詢
    @RequestMapping({"listNb.do"})
    @ResponseBody
    public ServerResponse listNb(Integer sum,String sort,String sortColmn,Integer buyType,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "positionSn", required = false) String positionSn, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, HttpServletRequest request) {
        if (sum == null){
            sum = 0;
        }
        return this.iUserPositionService.listByAdminNb(sum,sort,sortColmn,agentId, positionType, state, userId, positionSn, beginTime, endTime, pageNum, pageSize,buyType);
    }
    //分頁查詢持倉管理  匯總
    @RequestMapping({"sum.do"})
    @ResponseBody
    public ServerResponse sum(String sort, String sortColmn, Integer buyType, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "positionSn", required = false) String positionSn, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, HttpServletRequest request, HttpServletResponse response) {


        AdminPositionVO positionVO = new AdminPositionVO();
        List<AdminPositionVO> positionVOList =   this.iUserPositionService.listByAdminExport(sort,sortColmn,agentId, positionType, state, userId, positionSn, beginTime, endTime, pageNum, pageSize,buyType);
        BigDecimal profitAndLose = BigDecimal.ZERO;

        BigDecimal allProfitAndLose = BigDecimal.ZERO;
        for (AdminPositionVO adminPositionVO : positionVOList) {
            profitAndLose = profitAndLose.add(adminPositionVO.getProfitAndLose());
            allProfitAndLose = allProfitAndLose.add(adminPositionVO.getAllProfitAndLose());

        }
        positionVO.setAllProfitAndLose(allProfitAndLose);
        positionVO.setProfitAndLose(profitAndLose);

        return ServerResponse.createBySuccess(positionVO);
    }



    //分頁查詢資金管理 充值列表信息及模糊查詢
    @RequestMapping({"export.do"})
    @ResponseBody
    public void export(String sort,String sortColmn,Integer buyType,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "positionSn", required = false) String positionSn, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, HttpServletRequest request,HttpServletResponse response) {



        List<AdminPositionVO> positionVOList =   this.iUserPositionService.listByAdminExport(sort,sortColmn,agentId, positionType, state, userId, positionSn, beginTime, endTime, pageNum, pageSize,buyType);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("持仓导出数据","持仓导出数据"),
                AdminPositionVO.class, positionVOList);
        try {
            ServletOutputStream outputStream = response.getOutputStream();

            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //持倉管理 強制平倉操作
    @RequestMapping({"sell.do"})
    @ResponseBody
    public ServerResponse sell(String positionSn) throws Exception {
        ServerResponse serverResponse = null;
        try {
            serverResponse = this.iUserPositionService.sell(positionSn, 0,0,BigDecimal.ZERO);
        } catch (Exception e) {
            log.error("強制平倉 異常信息 = {}", e);
        }
        return serverResponse;
    }

    //鎖倉/解倉操作
    @RequestMapping({"lock.do"})
    @ResponseBody
    public ServerResponse lock(@RequestParam("positionId") Integer positionId, @RequestParam("state") Integer state, @RequestParam(value = "lockMsg", required = false) String lockMsg) {
        return this.iUserPositionService.lock(positionId, state, lockMsg);
    }

    @RequestMapping({"del.do"})
    @ResponseBody
    public ServerResponse del(@RequestParam("positionId") Integer positionId) {
        return this.iUserPositionService.del(positionId);
    }

    //創建持倉單
    @RequestMapping({"create.do"})
    @ResponseBody
    public ServerResponse create(@RequestParam("userId") Integer userId, @RequestParam("stockCode") String stockCode, @RequestParam("buyPrice") String buyPrice, @RequestParam("buyTime") String buyTime, @RequestParam("buyNum") Integer buyNum, @RequestParam("buyType") Integer buyType, @RequestParam("lever") BigDecimal lever) {
        return this.iUserPositionService.create(userId, stockCode, buyPrice, buyTime, buyNum, buyType, lever);
    }

    @RequestMapping({"updatePrice.do"})
    @ResponseBody
    public ServerResponse updatePrice(String positionSn,BigDecimal buyOrderPrice,Long buyOrderTime) {
        return this.iUserPositionService.updatePrice(positionSn, buyOrderPrice,new Date(buyOrderTime));
    }

}

