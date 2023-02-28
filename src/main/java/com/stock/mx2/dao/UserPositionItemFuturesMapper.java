package com.stock.mx2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.UserPositionItem;
import com.stock.mx2.pojo.UserPositionItemFutures;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface UserPositionItemFuturesMapper  extends BaseMapper<UserPositionItemFutures> {
    List<UserPositionItemFutures> listByAgent(@Param("positionType") Integer paramInteger1, @Param("state") Integer paramInteger2, @Param("userId") Integer paramInteger3, @Param("searchId") Integer paramInteger4, @Param("positionSn") String paramString, @Param("beginTime") Date paramDate1, @Param("endTime") Date paramDate2, @Param("buyType") Integer buyType, @Param("sort") String sort , @Param("sortColmn") String sortColmn);

}
