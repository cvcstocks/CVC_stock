package com.stock.mx2.service;


import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.User;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public interface IUserService {

  User getUserById(Integer id);
  ServerResponse reg(String paramString1, String paramString2, String paramString3, String paramString4, HttpServletRequest paramHttpServletRequest);

  ServerResponse login(String paramString1, String paramString2, HttpServletRequest paramHttpServletRequest);

  User getCurrentUser(HttpServletRequest paramHttpServletRequest);

  User getCurrentRefreshUser(HttpServletRequest paramHttpServletRequest);

  ServerResponse addOption(String paramString, HttpServletRequest paramHttpServletRequest);

  ServerResponse delOption(String paramString, HttpServletRequest paramHttpServletRequest);

  ServerResponse isOption(String paramString, HttpServletRequest paramHttpServletRequest);

  ServerResponse getUserInfo(HttpServletRequest paramHttpServletRequest);

  ServerResponse updatePwd(String paramString1, String paramString2, HttpServletRequest paramHttpServletRequest);

  ServerResponse checkPhone(String paramString);

  ServerResponse updatePwd(String paramString1, String paramString2, String paramString3);

  ServerResponse update(User paramUser);

  ServerResponse auth(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, HttpServletRequest paramHttpServletRequest);

  ServerResponse transAmt(BigDecimal paramInteger1, Integer paramInteger2, HttpServletRequest paramHttpServletRequest);

//  void ForceSellTask();
  void ForceSellOneStockTask();
  void ForceSellMessageTask();
//
//  void ForceSellIndexTask();
//  void ForceSellIndexsMessageTask();
//
//  void ForceSellFuturesTask();
//  void ForceSellFuturesMessageTask();

//  void qh1();


  ServerResponse listByAgent(String paramString1, String paramString2, Integer paramInteger1, Integer paramInteger2, int paramInt1, int paramInt2, HttpServletRequest paramHttpServletRequest);

  ServerResponse addSimulatedAccount(Integer paramInteger1, String paramString1, String paramString2, String paramString3, Integer paramInteger2, HttpServletRequest paramHttpServletRequest);

  ServerResponse listByAdmin(Integer id,Integer isActive,String paramString1, String paramString2, Integer paramInteger1, Integer paramInteger2, int paramInt1, int paramInt2, HttpServletRequest paramHttpServletRequest,Integer userWalletLevel);

  ServerResponse findByUserId(Integer paramInteger);

  ServerResponse updateLock(Integer paramInteger);

  ServerResponse updateAmt(Integer paramInteger1, BigDecimal paramInteger2, Integer paramInteger3);

  ServerResponse delete(Integer paramInteger, HttpServletRequest paramHttpServletRequest);

  int CountUserSize(Integer paramInteger);

  BigDecimal CountUserAmt(Integer paramInteger);

  BigDecimal CountEnableAmt(Integer paramInteger);

  ServerResponse authByAdmin(Integer paramInteger1, Integer paramInteger2, String paramString);

  ServerResponse findIdWithPwd(String phone);

  ServerResponse updateWithPwd(String paramString1, String paramString2);

  void updateUserAmt(Double amt, Integer user_id);


  List<User> listByAdminExport(Integer id,Integer isActive,String realName, String phone, Integer agentId, Integer accountType, HttpServletRequest request);

    User listByAdminSum(Integer id,Integer isActive,String realName, String phone, Integer agentId, Integer accountType, HttpServletRequest request);

  User getUserByPhone(String phone);

  int addEnableBalance(BigDecimal amount, Integer id);


  void ForceSellOneStockTaskFutures(BigDecimal trqPrice,BigDecimal trxPrice) ;

  void ForceSellMessageTaskFutures();

  ServerResponse updateRiseWith(Integer userId);

  ServerResponse openLever(String level, HttpServletRequest request);

  ServerResponse setKey(String key,HttpServletRequest request);
}
