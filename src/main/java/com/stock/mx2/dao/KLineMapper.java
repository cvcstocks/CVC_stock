package com.stock.mx2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.KLine;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KLineMapper extends BaseMapper<KLine> {
    @Select(value = "select time from k_line where symbol = #{symbol} and period = #{period}  order by time  desc limit 1 ")
    Long findMaxTimestamp(@Param("symbol") String s,@Param("period") String period);
    @Select(value = "select * from k_line where period = #{period} AND time < #{time} order by time  desc limit 1 ")
    KLine getKline(@Param("period") String period,@Param("time") long time);




    @Select(value = "select * from k_line where period = #{period} and time >= #{start} AND time < #{time} and time <= #{end} order by time  desc ")
    List<KLine> getKlineList(@Param("period") String period, @Param("start") String start, @Param("end") String end, @Param("time") long time);
}
