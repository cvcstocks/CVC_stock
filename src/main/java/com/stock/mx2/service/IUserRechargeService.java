package com.stock.mx2.service;


import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.UserRecharge;

import java.math.BigDecimal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public interface IUserRechargeService {
  ServerResponse checkInMoney(int paramInt, Integer paramInteger);

  ServerResponse inMoney(String paramString1, String paramString2, HttpServletRequest paramHttpServletRequest);

  ServerResponse findUserRechargeByOrderSn(String paramString);

  ServerResponse chargeSuccess(UserRecharge paramUserRecharge) throws Exception;

  ServerResponse chargeFail(UserRecharge paramUserRecharge) throws Exception;

  ServerResponse chargeCancel(UserRecharge paramUserRecharge) throws Exception;

  ServerResponse<PageInfo> findUserChargeList(String paramString1, String paramString2, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);

  ServerResponse<PageInfo> listByAgent(Integer paramInteger1, String paramString1, String paramString2, Integer paramInteger2, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);

  ServerResponse listByAdmin(String phone,Integer paramInteger1, Integer paramInteger2, String paramString1, Integer paramInteger3, String paramString2, String paramString3, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2,String applyBeginTime,String applyEndTime);

  ServerResponse updateState(Integer paramInteger1, Integer paramInteger2,BigDecimal amount) throws Exception;

  ServerResponse createOrder(Integer paramInteger1, Integer paramInteger2, Integer paramInteger3, String paramString);

  ServerResponse del(Integer paramInteger);

  int deleteByUserId(Integer paramInteger);

  BigDecimal CountChargeSumAmt(Integer paramInteger);

  BigDecimal CountTotalRechargeAmountByTime(Integer paramInteger);

  List<UserRecharge> exportByAdmin(String phone,Integer agentId, Integer userId, String realName, Integer state, String beginTime, String endTime, HttpServletRequest request,String applyBeginTime,String applyEndTime);


    UserRecharge sumByAdmin(String phone,Integer agentId, Integer userId, String realName, Integer state, String beginTime, String endTime, HttpServletRequest request,String applyBeginTime,String applyEndTime);
}
