package com.stock.mx2.service;


import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.UserStockSubscribe;

import javax.servlet.http.HttpServletRequest;

/**
 * 新股申購
 * @author lr
 * @date 2020/07/24
 */
public interface IUserStockSubscribeService {

    /**
     * 新增
     */
    int insert(UserStockSubscribe model);

    /**
     * 更新
     */
    int update(UserStockSubscribe model);

    /**
     * 新股申購-保存
     */
    ServerResponse save(UserStockSubscribe model, HttpServletRequest request);

    /**
     * 發送站內信
     */
    ServerResponse sendMsg(UserStockSubscribe model, HttpServletRequest request);

    /**
     * 新股申購-列表查詢
     */
    ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, HttpServletRequest request);

    /**
     * 新股申購-查詢詳情
     */
    ServerResponse getDetail(int id);

    /**
     * 新股申購-查询用户最新新股申購数据
     */
    ServerResponse getOneSubscribeByUserId(Integer userId, HttpServletRequest request);

    /**
     * 新股申購-用戶提交金額
     */
    ServerResponse userSubmit(UserStockSubscribe model, HttpServletRequest request);

}
