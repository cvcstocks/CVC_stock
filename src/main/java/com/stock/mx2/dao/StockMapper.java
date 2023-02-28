package com.stock.mx2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.Stock;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface StockMapper  extends BaseMapper<Stock> {
  int deleteByPrimaryKey(Integer paramInteger);


  int insertSelective(Stock paramStock);

  Stock selectByPrimaryKey(Integer paramInteger);

  int updateByPrimaryKeySelective(Stock paramStock);

  int updateByPrimaryKey(Stock paramStock);

  List findStockListByKeyWords(@Param("keyWords") String paramString1, @Param("stockPlate") String paramString2, @Param("stockType") String paramString3, @Param("stockTypeInt") String stockTypeInt, @Param("exchange") String exchange, @Param("show") Integer paramInteger);

  List findStockCode(@Param("stockType") String stockType,@Param("stock_num")Integer stock_num,@Param("stock_nums")Integer stock_nums);

  List<Stock> findStockByCode(String paramString);

  Stock findStockByName(String paramString);

  List listByAdmin(@Param("showState") Integer paramInteger1, @Param("lockState") Integer paramInteger2, @Param("code") String paramString1, @Param("name") String paramString2, @Param("stockPlate") String paramString3, @Param("stockType") String paramString4, @Param("riseWhite") Integer riseWhite);

  int CountStockNum();

  int CountShowNum(Integer paramInteger);

  int CountUnLockNum(Integer paramInteger);

  List<Stock> findStockList();




}
