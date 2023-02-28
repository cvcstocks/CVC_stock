package com.stock.mx2.service;

import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.SiteSpread;

import java.math.BigDecimal;

public interface ISiteSpreadService {
    /**
     * 新增
     */
    int insert(SiteSpread siteSpread);

    /**
     * 刪除
     */
    int delete(int id);

    /**
     * 更新
     */
    int update(SiteSpread siteSpread);

    /**
     * 根據主鍵 id 查詢
     */
    SiteSpread load(int id);

    /**
     * 分頁查詢
     */
    ServerResponse<PageInfo> pageList(int pageNum, int pageSize, String typeName);

    /**
     * 查詢點差費率
     * @author lr
     * @date 2020/07/01
     * applies：漲跌幅
     * turnover：成交額
     * code:股票代碼
     * unitprice：股票單價
     **/
    SiteSpread findSpreadRateOne(BigDecimal applies, BigDecimal turnover, String code, BigDecimal unitprice);
}
