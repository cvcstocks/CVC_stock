package com.stock.mx2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.UserPosition;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserPositionMapper extends BaseMapper<UserPosition> {
  int deleteByPrimaryKey(Integer paramInteger);


  int insertSelective(UserPosition paramUserPosition);

  UserPosition selectByPrimaryKey(Integer paramInteger);

  int updateByPrimaryKeySelective(UserPosition paramUserPosition);

  int updateByPrimaryKey(UserPosition paramUserPosition);

  UserPosition findPositionBySn(String paramString);

  List<UserPosition> findMyPositionByCodeAndSpell(@Param("uid") Integer paramInteger1, @Param("stockCode") String paramString1, @Param("stockSpell") String paramString2, @Param("state") Integer paramInteger2);

  List<UserPosition> findPositionByUserIdAndSellIdIsNull(Integer userId);

  List<UserPosition> findPositionByUserIdAndSellIdIsNullWhereRz(Integer userId);
  List<UserPosition> findPositionByUserIdAndSellIdIsNullWhereRzAndAll();

  List<UserPosition> listByAgent(@Param("positionType") Integer paramInteger1, @Param("state") Integer paramInteger2, @Param("userId") Integer paramInteger3, @Param("searchId") Integer paramInteger4, @Param("positionSn") String paramString, @Param("beginTime") Date paramDate1, @Param("endTime") Date paramDate2, @Param("buyType") Integer buyType, @Param("sort") String sort , @Param("sortColmn") String sortColmn);
  List<UserPosition> listByAgentNb(@Param("positionType") Integer paramInteger1, @Param("state") Integer paramInteger2, @Param("userId") Integer paramInteger3, @Param("searchId") Integer paramInteger4, @Param("positionSn") String paramString, @Param("beginTime") Date paramDate1, @Param("endTime") Date paramDate2, @Param("buyType") Integer buyType, @Param("sort") String sort , @Param("sortColmn") String sortColmn);


  List<UserPosition> findAllStayPosition();

  List<Integer> findDistinctUserIdList();

  List<Integer> findDistinctUserIdListAndRz();
  int CountPositionNum(@Param("state") Integer paramInteger1, @Param("accountType") Integer paramInteger2);

  BigDecimal CountPositionProfitAndLose();

  BigDecimal CountPositionAllProfitAndLose();

  int deleteByUserId(@Param("userId") Integer paramInteger);

  List<UserPosition> findPositionByStockCodeAndTimes(@Param("minuteTimes") Date paramDate, @Param("stockCode") String paramString, @Param("userId") Integer paramInteger);

  Integer findPositionNumByTimes(@Param("beginDate") Date paramDate, @Param("userId") Integer paramInteger);

  List<UserPosition> findPositionTopList(@Param("pageSize") Integer pageSize);

  UserPosition findUserPositionByCode(@Param("userId") Integer paramInteger,@Param("stockCode") String stockCode);

    BigDecimal selectUserAllLose(@Param("userId") Integer userId);
}
