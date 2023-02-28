package com.stock.mx2.dao;

import com.stock.mx2.pojo.RealTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RealTimeMapper {
    @Select({"select * from realtime where stockCode=#{stockCode}"})
    List<RealTime> findStock(String paramString);

    /*刪除股票*/
    @Delete({"DELETE FROM realtime WHERE locate('hf_',`stockCode`)=0"})
    int deleteStockCode();

    /*刪除期貨*/
    @Delete({"DELETE FROM realtime WHERE locate('hf_',`stockCode`)=1"})
    int deleteStockFuturesCode();

    @Insert({"<script>", "insert into realtime (time,volumes,price,rates,averagePrice,amounts,stockCode) values ", "<foreach  collection='areaLists' item='realTime' index='index' separator=','>", "(#{realTime.time},#{realTime.volumes},#{realTime.price},#{realTime.rates},#{realTime.averagePrice},#{realTime.amounts},#{realTime.stockCode})", "</foreach>", "</script>"})
    int insertRealTime(@Param("areaLists") List<RealTime> paramList);
}
