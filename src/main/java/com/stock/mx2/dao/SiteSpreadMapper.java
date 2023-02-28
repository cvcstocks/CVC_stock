package com.stock.mx2.dao;

import com.stock.mx2.pojo.SiteSpread;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * site_spread
 * @author lr
 * @date 2020/07/01
 */
public interface SiteSpreadMapper {

    /**
     * [新增]
     * @author lr
     * @date 2020/07/01
     **/
    int insert(SiteSpread siteSpread);

    /**
     * [刪除]
     * @author lr
     * @date 2020/07/01
     **/
    int delete(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/07/01
     **/
    int update(SiteSpread siteSpread);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/07/01
     **/
    SiteSpread load(int id);

    /**
     * [查詢] 分页查詢
     * @author lr
     * @date 2020/07/01
     **/
    List<SiteSpread> pageList(@Param("pageNum") int pageNum,@Param("pageSize")  int pageSize,@Param("typeName") String typeName);

    /**
     * 查詢點差費率
     * @author lr
     * @date 2020/07/01
     * applies：漲跌幅
     * turnover：成交額
     * code:股票代碼
     * unitprice：股票單價
     **/
    SiteSpread findSpreadRateOne(@Param("applies") BigDecimal applies,@Param("turnover") BigDecimal turnover,@Param("code") String code,@Param("unitprice") BigDecimal unitprice);

}
