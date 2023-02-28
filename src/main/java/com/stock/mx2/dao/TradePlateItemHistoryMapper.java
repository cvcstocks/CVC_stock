package com.stock.mx2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.TradePlateItemHistory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

public interface TradePlateItemHistoryMapper extends BaseMapper<TradePlateItemHistory> {
    @Select(value = " select json from trade_plate_item_history where time >= #{time} and type = #{type} order by time  limit 1")
    String getTradePlateItemHistory(@Param("time") long time, @Param("type") String type);
    @Delete(value = "DELETE from trade_plate_item_history where time <= #{time} ")
    void deleteHistory(@Param("time") long time);
}
