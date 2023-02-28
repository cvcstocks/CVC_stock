package com.stock.mx2.controller.agentNew;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.AgentUserMapper;
import com.stock.mx2.pojo.AgentUser;
import com.stock.mx2.pojo.UserRecharge;
import com.stock.mx2.pojo.UserWithdraw;
import com.stock.mx2.service.IAgentUserService;
import com.stock.mx2.service.IUserWithdrawService;

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
import java.util.List;

@Controller
@RequestMapping({"/agentNew/withdraw/"})
public class AgentNewWithDrawController {
    private static final Logger log = LoggerFactory.getLogger(AgentNewWithDrawController.class);

    @Autowired
    IAgentUserService iAgentUserService;

    @Autowired
    private AgentUserMapper agentUserMapper;
    @Autowired
    IUserWithdrawService iUserWithdrawService;

    //分頁查詢資金管理 提現列表信息及模糊查詢
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, @RequestParam(value = "applyBeginTime", required = false) String applyBeginTime, @RequestParam(value = "applyEndTime", required = false) String applyEndTime,HttpServletRequest request) {



        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);

        if (currentAgent == null){
            return    ServerResponse.createByError("請先登錄",null);
        }
        if (agentId != null) {

            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            if (agentUser ==null){
                return     ServerResponse.createByError("請先登錄",null);
            }
            if (agentUser.getParentId() != currentAgent.getId()) {

                return ServerResponse.createByErrorMsg("不能查詢非下級代理記錄");

            }

        }
        Integer searchId = null;

        if (agentId == null) {

            searchId = currentAgent.getId();

        } else {

            searchId = agentId;

        }
        return this.iUserWithdrawService.listByAdmin(phone,searchId, userId, realName, state, beginTime, endTime, request, pageNum, pageSize,applyBeginTime,applyEndTime);
    }


    //分頁查詢資金管理 充值列表信息及模糊查詢
    @RequestMapping({"export.do"})
    @ResponseBody
    public void export(@RequestParam(value = "phone", required = false) String phone,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime,@RequestParam(value = "applyBeginTime", required = false) String applyBeginTime, @RequestParam(value = "applyEndTime", required = false) String applyEndTime, HttpServletRequest request,HttpServletResponse response) {


        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);

        if (currentAgent == null){
            return   ;
        }
        if (agentId != null) {

            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            if (agentUser ==null){
                return  ;
            }
            if (agentUser.getParentId() != currentAgent.getId()) {

                return ;

            }

        }
        Integer searchId = null;

        if (agentId == null) {

            searchId = currentAgent.getId();

        } else {

            searchId = agentId;

        }


        List<UserWithdraw> userRechargeList = this.iUserWithdrawService.exportByAdmin(phone,searchId, userId, realName, state, beginTime, endTime, request,applyBeginTime,applyEndTime);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("提现导出","提现数据"),
                UserWithdraw.class, userRechargeList);
        try {
            ServletOutputStream outputStream = response.getOutputStream();

            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //分頁查詢資金管理 充值列表信息及模糊查詢
    @RequestMapping({"sum.do"})
    @ResponseBody
    public ServerResponse sum(@RequestParam(value = "phone", required = false) String phone,@RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "userId", required = false) Integer userId, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "state", required = false) Integer state, @RequestParam(value = "beginTime", required = false) String beginTime, @RequestParam(value = "endTime", required = false) String endTime, @RequestParam(value = "applyBeginTime", required = false) String applyBeginTime, @RequestParam(value = "applyEndTime", required = false) String applyEndTime, HttpServletRequest request,HttpServletResponse response) {


        AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);

        if (currentAgent == null){
            return    ServerResponse.createByError("請先登錄",null);
        }
        if (agentId != null) {

            AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);
            if (agentUser ==null){
                return     ServerResponse.createByError("請先登錄",null);
            }
            if (agentUser.getParentId() != currentAgent.getId()) {

                return ServerResponse.createByErrorMsg("不能查詢非下級代理記錄");

            }

        }
        Integer searchId = null;

        if (agentId == null) {

            searchId = currentAgent.getId();

        } else {

            searchId = agentId;

        }


       UserWithdraw userRecharge = this.iUserWithdrawService.sumByAdmin(phone,searchId, userId, realName, state, beginTime, endTime, request,applyBeginTime,applyEndTime);
     return ServerResponse.createBySuccess(userRecharge);
    }


}

