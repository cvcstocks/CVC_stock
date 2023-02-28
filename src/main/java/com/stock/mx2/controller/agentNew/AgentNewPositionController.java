package com.stock.mx2.controller.agentNew;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.dao.AgentUserMapper;
import com.stock.mx2.pojo.AgentUser;
import com.stock.mx2.pojo.UserWithdraw;
import com.stock.mx2.service.IAgentUserService;
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

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;


@Controller
@RequestMapping({"/agentNew/position/"})
public class AgentNewPositionController  extends AgentBaseController{
    private static final Logger log = LoggerFactory.getLogger(AgentNewPositionController.class);

    @Autowired
    IUserPositionService iUserPositionService;

    @Autowired
    private AgentUserMapper agentUserMapper;

    @Autowired
    private IAgentUserService agentUserService;

    //分頁查詢持倉管理 融資持倉單信息/融資平倉單信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(Integer sum,String sort,String sortColmn,Integer buyType,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "positionSn", required = false) String positionSn, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, HttpServletRequest request) {
        ServerResponse<Integer> searchData = super.getSearchId(agentId, request);
        if (searchData.isSuccess()){
            agentId = searchData.getData();
        }else {
            return searchData;
        }
        if (sum == null){
            sum = 0;
        }
        return this.iUserPositionService.listByAdmin(sum,sort,sortColmn,agentId, positionType, state, userId, positionSn, beginTime, endTime, pageNum, pageSize,buyType);
    }


    //分頁查詢持倉管理  匯總
    @RequestMapping({"sum.do"})
    @ResponseBody
    public ServerResponse sum(String sort,String sortColmn,Integer buyType,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "positionType", required = false) Integer positionType, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "positionSn", required = false) String positionSn, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "12") int pageSize, HttpServletRequest request,HttpServletResponse response) {

        ServerResponse<Integer> searchData = super.getSearchId(agentId, request);
        if (searchData.isSuccess()){
            agentId = searchData.getData();
        }else {
            return searchData;

        }
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

        ServerResponse<Integer> searchData = super.getSearchId(agentId, request);
        if (searchData.isSuccess()){
            agentId = searchData.getData();
        }else {
            return ;
        }


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


}

