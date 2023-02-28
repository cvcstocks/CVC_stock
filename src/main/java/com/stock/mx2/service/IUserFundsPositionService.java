package com.stock.mx2.service;


import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.UserFundsPosition;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * 分倉交易
 * @author lr
 * @date 2020/07/24
 */
public interface IUserFundsPositionService {

    /**
     * 新增
     */
    ServerResponse insert(UserFundsPosition model, HttpServletRequest request);

    /**
     * 更新
     */
    int update(UserFundsPosition model);

    /**
     * 分倉交易-保存
     */
    ServerResponse save(UserFundsPosition model);

    /**
     * 分倉交易-列表查詢
     */
    ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, HttpServletRequest request);

    /**
     * 分倉交易-查詢詳情
     */
    ServerResponse getDetail(int id);

    /**
     * 分倉交易-入倉
     */

    //分倉交易-用戶平倉操作
    ServerResponse sellFunds(String paramString, int paramInt) throws Exception;

    /*
    * 分倉交易-查詢所有平倉/持倉信息
    * */
    ServerResponse<PageInfo> findMyPositionByCodeAndSpell(String paramString1, String paramString2, Integer paramInteger, HttpServletRequest paramHttpServletRequest, int paramInt1, int paramInt2);

    ServerResponse findUserFundsPositionByCode(HttpServletRequest request, String fundsCode);

}


