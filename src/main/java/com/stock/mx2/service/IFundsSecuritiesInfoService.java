package com.stock.mx2.service;

import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.FundsSecuritiesInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 配資證券信息
 * @author lr
 * @date 2020/07/24
 */
public interface IFundsSecuritiesInfoService {

    /**
     * 新增
     */
    int insert(FundsSecuritiesInfo model);

    /**
     * 更新
     */
    int update(FundsSecuritiesInfo model);

    /**
     * 配資券商機構-保存
     */
    ServerResponse save(FundsSecuritiesInfo model);

    /**
     * 配資券商機構-列表查詢
     */
    ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, HttpServletRequest request);

    /**
     * 配資券商機構-查詢詳情
     */
    ServerResponse getDetail(int id);

    /**
     * [查詢] 查詢可用的证券信息
     * @author lr
     * @date 2020/07/24
     **/
    ServerResponse<PageInfo> getEnabledList();

}
