package com.stock.mx2.controller.agentNew;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.UserCashDetail;
import com.stock.mx2.pojo.UserRecharge;
import com.stock.mx2.service.IUserCashDetailService;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@Controller
@RequestMapping({"/agentNew/cash/"})
public class AgentNewCashDetailController extends AgentBaseController{
    private static final Logger log = LoggerFactory.getLogger(AgentNewCashDetailController.class);

    @Autowired
    IUserCashDetailService iUserCashDetailService;

    //分頁查詢資金管理 所有資金記錄信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "phone", required = false)String phone,HttpServletRequest request,@RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "userName", required = false) String userName, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionId", required = false) Integer positionId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        ServerResponse<Integer> searchData = super.getSearchId(agentId, request);
        if (searchData.isSuccess()){
            agentId = searchData.getData();
        }else {
            return searchData;
        }
        return this.iUserCashDetailService.listByAdmin(phone,userId, userName, agentId, positionId, pageNum, pageSize);
    }


    //分頁查詢資金管理 所有資金記錄信息及模糊查詢
    @RequestMapping({"export.do"})
    @ResponseBody
    public void export(@RequestParam(value = "phone", required = false)String phone,@RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "userName", required = false) String userName, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionId", required = false) Integer positionId, HttpServletResponse response,HttpServletRequest request) {

        ServerResponse<Integer> searchData = super.getSearchId(agentId, request);
        if (searchData.isSuccess()){
            agentId = searchData.getData();
        }else {
            return ;
        }


       List<UserCashDetail> cashDetailList = this.iUserCashDetailService.exportByAdmin( phone,userId, userName, agentId, positionId);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("資金明細导出","資金明細数据"),
                UserCashDetail.class, cashDetailList);
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
    public ServerResponse sum(@RequestParam(value = "phone", required = false)String phone,HttpServletRequest request,@RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "userName", required = false) String userName, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionId", required = false) Integer positionId, HttpServletResponse response) {
        ServerResponse<Integer> searchData = super.getSearchId(agentId, request);
        if (searchData.isSuccess()){
            agentId = searchData.getData();
        }else {
            return searchData;
        }
      UserCashDetail cashDetailSum = this.iUserCashDetailService.sumByAdmin(phone,userId, userName, agentId, positionId);
        return ServerResponse.createBySuccess(cashDetailSum);

    }

 }
