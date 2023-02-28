package com.stock.mx2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.UserPosition;
import com.stock.mx2.pojo.UserPositionFutures;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public interface UserPositionFuturesMapper extends BaseMapper<UserPositionFutures> {
    List<UserPositionFutures> findPositionByStockCodeAndTimes(@Param("minuteTimes") Date paramTimes, @Param("stockCode") String stockCode,@Param("userId") Integer userId);

    Integer findPositionNumByTimes(@Param("beginDate")Date beginDate, @Param("userId")Integer userId);

    List<Integer> findDistinctUserIdListAndRz();


    List<UserPositionFutures> findPositionByUserIdAndSellIdIsNullWhereRzAndAll();


    List<UserPositionFutures> findPositionByUserIdAndSellIdIsNullWhereRz(Integer userId);
    List<UserPositionFutures> listByAgent(@Param("positionType") Integer paramInteger1, @Param("state") Integer paramInteger2, @Param("userId") Integer paramInteger3, @Param("searchId") Integer paramInteger4, @Param("positionSn") String paramString, @Param("beginTime") Date paramDate1, @Param("endTime") Date paramDate2, @Param("buyType") Integer buyType, @Param("sort") String sort , @Param("sortColmn") String sortColmn);

    List<UserPositionFutures>  listByAgentNb(@Param("positionType") Integer paramInteger1, @Param("state") Integer paramInteger2, @Param("userId") Integer paramInteger3, @Param("searchId") Integer paramInteger4, @Param("positionSn") String paramString, @Param("beginTime") Date paramDate1, @Param("endTime") Date paramDate2, @Param("buyType") Integer buyType, @Param("sort") String sort , @Param("sortColmn") String sortColmn);

    List<UserPositionFutures> findMyPositionByCodeAndSpell(@Param("uid") Integer paramInteger1, @Param("stockCode") String paramString1,@Param("state") Integer paramInteger2);

    List<UserPositionFutures> findPositionByUserIdAndSellIdIsNull(@Param("userId") Integer userId);

}
