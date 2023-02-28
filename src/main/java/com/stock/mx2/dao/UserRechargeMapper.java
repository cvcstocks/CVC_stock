package com.stock.mx2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.mx2.pojo.UserRecharge;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserRechargeMapper extends BaseMapper<UserRecharge> {
  int deleteByPrimaryKey(Integer paramInteger);


  int insertSelective(UserRecharge paramUserRecharge);

  UserRecharge selectByPrimaryKey(Integer paramInteger);

  int updateByPrimaryKeySelective(UserRecharge paramUserRecharge);

  int updateByPrimaryKey(UserRecharge paramUserRecharge);

  int checkInMoney(@Param("status") int paramInt, @Param("userId") Integer paramInteger);

  UserRecharge findUserRechargeByOrderSn(String paramString);

  List findUserChargeList(@Param("uid") Integer paramInteger, @Param("payChannel") String paramString1, @Param("orderStatus") String paramString2);

  List listByAdmin(@Param("phone") String phone, @Param("agentId") Integer paramInteger1, @Param("userId") Integer paramInteger2, @Param("realName") String paramString, @Param("state") Integer paramInteger3, @Param("begin_time") Date paramDate1, @Param("end_time") Date paramDate2, @Param("applyBeginTime") String applyBeginTime, @Param("applyEndTime") String applyEndTime);

  int deleteByUserId(@Param("userId") Integer paramInteger);

  List listByAgent(@Param("searchId") Integer paramInteger1, @Param("realName") String paramString1, @Param("payChannel") String paramString2, @Param("state") Integer paramInteger2);

  BigDecimal CountChargeSumAmt(Integer paramInteger);

  BigDecimal CountTotalRechargeAmountByTime(Integer paramInteger);

  BigDecimal selectRechargeAmountByUserId(Integer userId);

}
