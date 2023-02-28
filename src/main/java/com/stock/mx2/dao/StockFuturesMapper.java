package com.stock.mx2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.StockFutures;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface StockFuturesMapper  extends BaseMapper<StockFutures> {
  int deleteByPrimaryKey(Integer paramInteger);


  int insertSelective(StockFutures paramStockFutures);

  StockFutures selectByPrimaryKey(Integer paramInteger);

  int updateByPrimaryKeySelective(StockFutures paramStockFutures);

  int updateByPrimaryKey(StockFutures paramStockFutures);

  List listByAdmin(@Param("fuName") String paramString1, @Param("fuCode") String paramString2);

  StockFutures selectFuturesByName(String paramString);

  StockFutures selectFuturesByCode(String paramString);

  List queryHome();

  List queryList();
}
