package com.stock.mx2.service;

import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.UserCashDetail;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IUserCashDetailService {
  ServerResponse<PageInfo> findUserCashDetailList(Integer paramInteger, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);

  ServerResponse<PageInfo> listByAgent(Integer paramInteger1, String paramString, Integer paramInteger2, Integer paramInteger3, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);

  ServerResponse<PageInfo> listByAdmin(String phone,Integer paramInteger1, String paramString, Integer paramInteger2, Integer paramInteger3, int paramInt1, int paramInt2);

  int deleteByUserId(Integer paramInteger);

  ServerResponse delCash(Integer cashId);

    List<UserCashDetail> exportByAdmin(String phone,Integer userId, String userName, Integer agentId, Integer positionId);


  UserCashDetail sumByAdmin(String phone,Integer userId, String userName, Integer agentId, Integer positionId);

  ServerResponse findUserFutCashDetailList(Integer positionId, HttpServletRequest request, int pageNum, int pageSize);
}
