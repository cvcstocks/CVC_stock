package com.stock.mx2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.Newlist;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface NewlistMapper extends BaseMapper<Newlist> {
    BigDecimal selectSubPriceByCodeAndPhone(@Param("code") String code, @Param("phone") String phone);

    BigDecimal selectDeepPurchase(@Param("phone") String phone);

    BigDecimal selectDeepProfitAndLoss(@Param("phone") String phone);

    BigDecimal selectDeep(@Param("phone") String phone);
}
