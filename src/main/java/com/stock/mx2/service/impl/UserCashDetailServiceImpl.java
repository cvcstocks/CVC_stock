package com.stock.mx2.service.impl;


import com.github.pagehelper.PageHelper;

import com.github.pagehelper.PageInfo;

import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.dao.AgentUserMapper;

import com.stock.mx2.dao.UserCashDetailMapper;

import com.stock.mx2.pojo.AgentUser;

import com.stock.mx2.pojo.User;

import com.stock.mx2.pojo.UserCashDetail;

import com.stock.mx2.service.IAgentUserService;

import com.stock.mx2.service.IUserCashDetailService;

import com.stock.mx2.service.IUserService;


import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

 @Service("iUserCashDetailService")
 public class UserCashDetailServiceImpl implements IUserCashDetailService {

   @Autowired
   UserCashDetailMapper userCashDetailMapper;

   @Autowired
   IUserService iUserService;

   @Autowired
   IAgentUserService iAgentUserService;

   @Autowired
   AgentUserMapper agentUserMapper;



   public ServerResponse<PageInfo> findUserCashDetailList(Integer positionId, HttpServletRequest request, int pageNum, int pageSize) {

     PageHelper.startPage(pageNum, pageSize);



     User user = this.iUserService.getCurrentUser(request);
     if (user == null ){
       return ServerResponse.createBySuccessMsg("請先登錄");
     }


     List<UserCashDetail> userCashDetails = this.userCashDetailMapper.findUserCashDetailList(user.getId(), positionId);



     PageInfo pageInfo = new PageInfo(userCashDetails);



     return ServerResponse.createBySuccess(pageInfo);

   }

   public ServerResponse<PageInfo> listByAgent(Integer userId, String userName, Integer agentId, Integer positionId, HttpServletRequest request, int pageNum, int pageSize) {

     AgentUser currentAgent = this.iAgentUserService.getCurrentAgent(request);
     if (currentAgent ==null){
       return    ServerResponse.createByError("請先登錄",null);
     }


     if (agentId != null) {

       AgentUser agentUser = this.agentUserMapper.selectByPrimaryKey(agentId);

       if (agentUser.getParentId() != currentAgent.getId()) {

         return ServerResponse.createByErrorMsg("不能查詢非下機代理信息");

       }

     }

     Integer searchId = null;

     if (agentId == null) {

       searchId = currentAgent.getId();

     } else {

       searchId = agentId;

     }

     PageHelper.startPage(pageNum, pageSize);

     List<UserCashDetail> cashDetails = this.userCashDetailMapper.listByAgent(userId, userName, searchId, positionId);

     PageInfo pageInfo = new PageInfo(cashDetails);

     return ServerResponse.createBySuccess(pageInfo);

   }

   public ServerResponse<PageInfo> listByAdmin(String phone,Integer userId, String userName, Integer agentId, Integer positionId, int pageNum, int pageSize) {

     PageHelper.startPage(pageNum, pageSize);

     List<UserCashDetail> cashDetails = this.userCashDetailMapper.listByAdmin(phone,userId, userName, agentId, positionId);

     PageInfo pageInfo = new PageInfo(cashDetails);

     return ServerResponse.createBySuccess(pageInfo);

   }


   public int deleteByUserId(Integer userId) { return this.userCashDetailMapper.deleteByUserId(userId); }

   public ServerResponse delCash(Integer cashId) {
     if (cashId == null) {
       return ServerResponse.createByErrorMsg("刪除id不能為空");
     }
     int updateCount = this.userCashDetailMapper.deleteByPrimaryKey(cashId);
     if (updateCount > 0) {
       return ServerResponse.createBySuccessMsg("刪除成功");
     }
     return ServerResponse.createByErrorMsg("刪除失敗");
   }

   @Override
   public List<UserCashDetail> exportByAdmin(String phone,Integer userId, String userName, Integer agentId, Integer positionId) {
     return       this.userCashDetailMapper.listByAdmin(phone,userId, userName, agentId, positionId);

   }

   @Override
   public UserCashDetail sumByAdmin(String phone,Integer userId, String userName, Integer agentId, Integer positionId) {
     UserCashDetail sumData = new UserCashDetail();
     List<UserCashDetail> list = this.userCashDetailMapper.listByAdmin(phone,userId, userName, agentId, positionId);
     BigDecimal amount = BigDecimal.ZERO;
     for (UserCashDetail cashDetail : list) {
       amount = amount.add(cashDetail.getDeAmt());
     }
     sumData.setDeAmt(amount);

     return sumData;
   }

   @Override
   public ServerResponse findUserFutCashDetailList(Integer positionId, HttpServletRequest request, int pageNum, int pageSize) {

     PageHelper.startPage(pageNum, pageSize);



     User user = this.iUserService.getCurrentUser(request);
     if (user == null ){
       return ServerResponse.createBySuccessMsg("請先登錄");
     }


     List<UserCashDetail> userCashDetails = this.userCashDetailMapper.findUserFutCashDetailList(user.getId(), positionId);



     PageInfo pageInfo = new PageInfo(userCashDetails);



     return ServerResponse.createBySuccess(pageInfo);
   }

 }
