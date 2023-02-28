package com.stock.mx2.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stock.mx2.common.ServerResponse;

import com.stock.mx2.dao.UserBankMapper;

import com.stock.mx2.pojo.User;

import com.stock.mx2.pojo.UserBank;

import com.stock.mx2.service.IUserBankService;

import com.stock.mx2.service.IUserService;


import com.stock.mx2.vo.user.UserBankInfoVO;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

 @Service("iUserBankService")
 public class UserBankServiceImpl implements IUserBankService {

   @Autowired
   UserBankMapper userBankMapper;

   @Autowired
   IUserService iUserService;

   public UserBank findUserBankByUserId(Integer userId) {

     List<UserBank> userBankList = this.userBankMapper.selectList(new QueryWrapper<UserBank>().eq("user_id", userId));
     if (userBankList == null || userBankList.size()==0) {
     return null;
     }
     return userBankList.get(0);

     }

   public ServerResponse addBank(UserBank bank, HttpServletRequest request) {

     User user = this.iUserService.getCurrentUser(request);
     if (user == null ){
       return ServerResponse.createBySuccessMsg("請先登錄");
     }

     List<UserBank> userBankList = this.userBankMapper.selectList(new QueryWrapper<UserBank>().eq("user_id", user.getId()));

     if (userBankList != null && userBankList.size()>0) {
       return ServerResponse.createByErrorMsg("銀行信息已經存在，不要重複添加");

     }
     UserBank userBank = new UserBank();

     userBank.setUserId(user.getId());

     userBank.setBankName(bank.getBankName());

     userBank.setImgOne(bank.getImgOne());
     userBank.setImgTwo(bank.getImgTwo());


     userBank.setUserName(bank.getUserName());

     userBank.setBankNo(bank.getBankNo());

     userBank.setBankAddress(bank.getBankAddress());

     userBank.setBankImg(bank.getBankImg());

     userBank.setBankPhone(bank.getBankPhone());

     userBank.setAddTime(new Date());

     int insertCount = this.userBankMapper.insert(userBank);

     if (insertCount > 0) {
       return ServerResponse.createBySuccess("添加銀行卡成功");

     }

     return ServerResponse.createByErrorMsg("添加銀行卡失敗");

   }

   public ServerResponse updateBank(UserBank bank, HttpServletRequest request) {

     User user = this.iUserService.getCurrentUser(request);

     if (user == null ){
       return ServerResponse.createBySuccessMsg("請先登錄");
     }


     List<UserBank> userBankList = this.userBankMapper.selectList(new QueryWrapper<UserBank>().eq("user_id", user.getId()));

     if (userBankList == null || userBankList.size()==0) {
       return ServerResponse.createByErrorMsg("修改失敗，找不到銀行");
     }
     UserBank dbBank = userBankList.get(0);
     if (dbBank == null) {


     }

     dbBank.setBankName(bank.getBankName());

     dbBank.setBankNo(bank.getBankNo());

     dbBank.setBankAddress(bank.getBankAddress());

     dbBank.setBankImg(bank.getBankImg());
     dbBank.setUserName(bank.getUserName());
     dbBank.setImgOne(bank.getImgOne());
     dbBank.setImgTwo(bank.getImgTwo());

     dbBank.setBankPhone(bank.getBankPhone());

     int updateCount = this.userBankMapper.updateById(dbBank);

     if (updateCount > 0) {

       return ServerResponse.createBySuccess("修改銀行卡成功");

     }

     return ServerResponse.createByErrorMsg("修改銀行卡失敗");
   }

   public ServerResponse getBankInfo(HttpServletRequest request) {

     User user = this.iUserService.getCurrentUser(request);
     if (user == null ){
       return ServerResponse.createBySuccessMsg("請先登錄");
     }
     List<UserBank> userBankList = this.userBankMapper.selectList(new QueryWrapper<UserBank>().eq("user_id", user.getId()));

     if (userBankList == null || userBankList.size()==0) {

       return ServerResponse.createByErrorMsg("未添加銀行信息");

     }
     UserBank dbBank = userBankList.get(0);


     UserBankInfoVO userBankInfoVO = new UserBankInfoVO();

     userBankInfoVO.setRealName(dbBank.getUserName());

     userBankInfoVO.setBankName(dbBank.getBankName());


     userBankInfoVO.setBankAddress(dbBank.getBankAddress());

     userBankInfoVO.setBankNo(dbBank.getBankNo());
     userBankInfoVO.setImgOne(dbBank.getImgOne());
     userBankInfoVO.setImgTwo(dbBank.getImgTwo());

     return ServerResponse.createBySuccess(userBankInfoVO);

   }

   public ServerResponse updateBankByAdmin(UserBank userBank) {

     if (userBank.getId() == null) {

       return ServerResponse.createByErrorMsg("修改id必傳");

     }

     int updateCount = this.userBankMapper.updateById(userBank);

     if (updateCount > 0) {

       return ServerResponse.createBySuccessMsg("修改成功");

     }

     return ServerResponse.createByErrorMsg("修改失敗");

   }

   public ServerResponse getBank(Integer userId) {
     List<UserBank> userBankList = this.userBankMapper.selectList(new QueryWrapper<UserBank>().eq("user_id", userId));

     if (userBankList == null || userBankList.size()==0) {

           return ServerResponse.createBySuccess(null);
         }
         UserBank dbBank = userBankList.get(0);
        return ServerResponse.createBySuccess(dbBank);

     }
 }
