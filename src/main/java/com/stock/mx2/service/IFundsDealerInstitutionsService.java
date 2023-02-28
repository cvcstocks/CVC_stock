package com.stock.mx2.service;

import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.FundsDealerInstitutions;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 配資券商機構
 * @author lr
 * @date 2020/07/24
 */
public interface IFundsDealerInstitutionsService {

    /**
     * 新增
     */
    int insert(FundsDealerInstitutions model);

    /**
     * 更新
     */
    int update(FundsDealerInstitutions model);

    /**
     * 配資券商機構-保存
     */
    ServerResponse save(FundsDealerInstitutions model);

    /**
     * 配資券商機構-列表查詢
     */
    ServerResponse<PageInfo> getList(int pageNum, int pageSize, String keyword, Integer status, HttpServletRequest request);

    /**
     * 配資券商機構-查詢詳情
     */
    ServerResponse getDetail(int id);

}
