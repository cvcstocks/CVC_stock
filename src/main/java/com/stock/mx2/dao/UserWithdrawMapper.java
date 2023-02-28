package com.stock.mx2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.UserWithdraw;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserWithdrawMapper extends BaseMapper<UserWithdraw> {
  int deleteByPrimaryKey(Integer paramInteger);


  int insertSelective(UserWithdraw paramUserWithdraw);

  UserWithdraw selectByPrimaryKey(Integer paramInteger);

  int updateByPrimaryKeySelective(UserWithdraw paramUserWithdraw);

  int updateByPrimaryKey(UserWithdraw paramUserWithdraw);

  List findUserWithList(@Param("uid") Integer paramInteger, @Param("withStatus") String paramString);

  List listByAgent(@Param("searchId") Integer paramInteger1, @Param("realName") String paramString, @Param("state") Integer paramInteger2);

  List listByAdmin(@Param("phone")String phone, @Param("agentId") Integer paramInteger1, @Param("userId") Integer paramInteger2, @Param("realName") String paramString1, @Param("state") Integer paramInteger3, @Param("beginTime") String paramString2, @Param("endTime") String paramString3, @Param("applyBeginTime") String applyBeginTime, @Param("applyEndTime") String applyEndTime);

  BigDecimal CountSpWithSumAmtByState(Integer paramInteger);

  BigDecimal CountSpWithSumAmTodaytByState(Integer paramInteger);

  int deleteByUserId(@Param("userId") Integer paramInteger);
}
