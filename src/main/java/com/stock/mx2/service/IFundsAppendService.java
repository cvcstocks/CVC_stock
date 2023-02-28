package com.stock.mx2.service;

import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.FundsAppend;

import javax.servlet.http.HttpServletRequest;

/**
 * 配資追加申請
 * @author lr
 * @date 2020/07/24
 */
public interface IFundsAppendService {

    /**
     * 新增
     */
    int insert(FundsAppend model);

    /**
     * 更新
     */
    int update(FundsAppend model);

    /**
     * 配資追加申請-保存
     */
    ServerResponse save(FundsAppend model, HttpServletRequest request);

    /**
     * 配資追加申請-列表查詢
     */
    ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, Integer status, Integer userId, Integer appendType, HttpServletRequest request);

    /**
     * 配資追加申請-查詢詳情
     */
    ServerResponse getDetail(int id);



}
