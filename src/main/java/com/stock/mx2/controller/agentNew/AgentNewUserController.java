package com.stock.mx2.controller.agentNew;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.dao.AgentUserMapper;
import com.stock.mx2.pojo.User;
import com.stock.mx2.pojo.UserBank;
import com.stock.mx2.pojo.UserWithdraw;
import com.stock.mx2.service.IAgentUserService;
import com.stock.mx2.service.IUserBankService;
import com.stock.mx2.service.IUserService;

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
@RequestMapping({"/agentNew/user/"})
public class AgentNewUserController  extends AgentBaseController{
    private static final Logger log = LoggerFactory.getLogger(AgentNewUserController.class);

    @Autowired
    IUserService iUserService;

    @Autowired
    IUserBankService iUserBankService;

    @Autowired
    IAgentUserService iAgentUserService;

    @Autowired
    private AgentUserMapper agentUserMapper;
    //分頁查詢所有用戶列表信息 及模糊查詢用戶信息
    @RequestMapping({"list.do"})
    @ResponseBody
    public ServerResponse list(@RequestParam(value = "id",required = false)Integer id,@RequestParam(value = "isActive",required = false)Integer isActive, @RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "accountType", required = false) Integer accountType, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize, HttpServletRequest request) {

        ServerResponse<Integer> searchData = super.getSearchId(agentId, request);
        if (searchData.isSuccess()){
            agentId = searchData.getData();
        }else {
            return searchData;
        }

        return this.iUserService.listByAdmin(id,isActive,realName, phone, agentId, accountType, pageNum, pageSize, request,null);
    }


    //分頁查詢資金管理 充值列表信息及模糊查詢
    @RequestMapping({"export.do"})
    @ResponseBody
    public void export(@RequestParam(value = "id",required = false)Integer id,@RequestParam(value = "isActive",required = false)Integer isActive,@RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "accountType", required = false) Integer accountType, HttpServletRequest request, HttpServletResponse response) {

        ServerResponse<Integer> searchId = super.getSearchId(agentId, request);
        if (searchId.isSuccess()){
            agentId = searchId.getData();
        }else {
            return ;
        }

        List<User> userRechargeList = this.iUserService.listByAdminExport(id,isActive,realName, phone, agentId, accountType, request);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("用户导出","用户数据"),
                User.class, userRechargeList);
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
    public ServerResponse sum(@RequestParam(value = "id",required = false)Integer id,@RequestParam(value = "isActive",required = false)Integer isActive,@RequestParam(value = "realName", required = false) String realName, @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam(value = "accountType", required = false) Integer accountType, HttpServletRequest request, HttpServletResponse response) {

        ServerResponse<Integer> searchId = super.getSearchId(agentId, request);
        if (searchId.isSuccess()){
            agentId = searchId.getData();
        }else {
            return searchId;
        }
     User user = this.iUserService.listByAdminSum(id,isActive,realName, phone, agentId, accountType, request);

       return ServerResponse.createBySuccess(user);
    }

    //查詢用戶信息是否存在
    @RequestMapping({"detail.do"})
    @ResponseBody
    public ServerResponse detail(Integer userId) {



        return this.iUserService.findByUserId(userId);
    }

    @RequestMapping({"updateLock.do"})
    @ResponseBody
    public ServerResponse updateLock(Integer userId) {
        return this.iUserService.updateLock(userId);
    }


    //修改用戶列表 用戶信息
    @RequestMapping({"update.do"})
    @ResponseBody
    public ServerResponse update(User user) {
        return this.iUserService.update(user);
    }

    //修改用戶列表 銀行卡信息
    @RequestMapping({"updateBank.do"})
    @ResponseBody
    public ServerResponse updateBank(UserBank userBank) {
        return this.iUserBankService.updateBankByAdmin(userBank);
    }

    //添加用戶列表 用戶信息
    @RequestMapping({"addSimulatedAccount.do"})
    @ResponseBody
    public ServerResponse addSimulatedAccount(HttpServletRequest request, @RequestParam(value = "agentId", required = false) Integer agentId, @RequestParam("phone") String phone, @RequestParam("amt") String amt, @RequestParam("accountType") Integer accountType, @RequestParam("pwd") String pwd) {
        return this.iUserService.addSimulatedAccount(agentId, phone, pwd, amt, accountType, request);
    }

    @RequestMapping({"authByAdmin.do"})
    @ResponseBody
    public ServerResponse authByAdmin(Integer userId, Integer state, String authMsg) {
        return this.iUserService.authByAdmin(userId, state, authMsg);
    }

    //查看指定 用戶列表的用戶信息
    @RequestMapping({"getBank.do"})
    @ResponseBody
    public ServerResponse getBank(Integer userId) {
        return this.iUserBankService.getBank(userId);
    }

    //刪除用戶列表 用戶信息
    @RequestMapping({"delete.do"})
    @ResponseBody
    public ServerResponse delete(Integer userId, HttpServletRequest request) {
        return this.iUserService.delete(userId, request);
    }
}
