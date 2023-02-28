package com.stock.mx2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.stock.mx2.dao.UserCashDetailMapper;
import com.stock.mx2.dao.UserMapper;
import com.stock.mx2.dao.UserRechargeMapper;
import com.stock.mx2.pojo.User;
import com.stock.mx2.pojo.UserRecharge;
import com.stock.mx2.service.UserLevelWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class UserLevelWalletServiceImpl implements UserLevelWalletService {



    @Autowired
    private UserCashDetailMapper userCashDetailMapper;

    @Autowired
   private UserRechargeMapper userRechargeMapper;

    @Autowired
    private UserMapper userMapper;
    /**
     * 1种⼦级：未⼊⾦
     * 2过度级：待⼊⾦
     * 3培养级：已⼊⾦
     * 4开发级：⼊⾦⾦额10-100w
     * 5⻩⾦级：⼊⾦⾦额100-200w
     * 每笔交易平仓后计算⼀次｜累计盈利⾦额，等于 =>3万 —
     * 体验结束
     * -⾃动扣除体验⾦
     * -已持仓的⾦额⾃动在交易时间内平仓清空｜此笔强制平仓不
     * 计算亏损
     * -保留所有的盈亏账户资⾦  - 进入过渡期
     *  -  有晋级情况 返回晋级的等级
     */

    @Override
    public int updateUserLevel(Integer userId) {
        User user = userMapper.selectById(userId);
        int level = user.getUserWalletLevel();
        if (user.getUserWalletLevel() == 1){
            BigDecimal bigDecimal = userCashDetailMapper.selectLose(userId,user.getUseKeyTime());
            if (bigDecimal.compareTo(new BigDecimal(30000)) >= 0){
                user.setUserWalletLevel(2);
                userMapper.updateById(user);
                level = 2;
            }else{
                return level;
            }
        }
        if (user.getUserWalletLevel() == 2){
            Long rechargeCount = userRechargeMapper.selectCount(new QueryWrapper<UserRecharge>().eq("order_status", 1).eq("user_id", userId));

            if (rechargeCount > 0){
                user.setUserWalletLevel(3);
                userMapper.updateById(user);
                level = 3;
            }else{
                return level;
            }
        }
        if (user.getUserWalletLevel() == 3){
            BigDecimal bigDecimal = userRechargeMapper.selectRechargeAmountByUserId(userId);
            if (bigDecimal.compareTo(new BigDecimal(100000)) >= 0){
                user.setUserWalletLevel(4);
                userMapper.updateById(user);
                level = 4;
            }else{
                return level;
            }
        }
        if (user.getUserWalletLevel() == 4){
            BigDecimal bigDecimal = userRechargeMapper.selectRechargeAmountByUserId(userId);
            if (bigDecimal.compareTo(new BigDecimal(1000000)) >= 0){
                user.setUserWalletLevel(5);
                userMapper.updateById(user);
                level = 5;
            }else{
                return level;
            }
        }
        return level;
    }


    /**
     *

     * 可提现盈亏账户：仅⽀持种⼦期客户
     * 盈亏账户：仅⽀持种⼦期客户
     * @param userId
     * @param loseAmt 亏损金额
     * @return
     */
    @Override
    public int updateUserEnableAmtByStockLose(Integer userId, BigDecimal loseAmt, BigDecimal bjAmount){



        User user = userMapper.selectById(userId);
        Integer userWalletLevel = user.getUserWalletLevel();
        int nowLevel = this.updateUserLevel(userId);
        boolean levelUpdate = false;

        if (userWalletLevel==1 && nowLevel >= 2 ){
            //     * 体验结束
            //     * -⾃动扣除体验⾦
            //     * -已持仓的⾦额⾃动在交易时间内平仓清空｜此笔强制平仓不计算亏损
            user.setExperienceWallet(BigDecimal.ZERO);
            user.setLaseLevelTime(new Date());
            user.setSmsTimeType(0);
            user.setUserWalletLevel(nowLevel);
            userMapper.updateById(user);
            levelUpdate = true;
        }


        if (userWalletLevel==1){
            BigDecimal withProfitWalletRate = BigDecimal.ZERO;
            if (user.getExperienceWalletLevel()==1){
                 withProfitWalletRate =new BigDecimal("0.05");
            }else if (user.getExperienceWalletLevel()==2){
                 withProfitWalletRate =new BigDecimal("0.03");
            }else if (user.getExperienceWalletLevel()==3){
                 withProfitWalletRate =new BigDecimal("0.01");
            }
            BigDecimal profitWalletRate = BigDecimal.ONE.subtract(withProfitWalletRate);
            if (!levelUpdate){
                user.setExperienceWallet(user.getExperienceWallet().add(bjAmount));
            }

            //     * -每笔交易盈利部分20%在可提现盈账户
            //     * -每笔交易盈利部分80%在盈利账户
            //     *  -每笔交易如亏损，按⽐例优先扣除盈利账户和可提现盈利
            //     * 账户，出现扣款差额时剩余扣款⾦额从体验⾦账户扣除，如
            //     * 盈利账户和可提现盈利账户为空，直接扣除体验⾦账户
            if (loseAmt.compareTo(BigDecimal.ZERO)>0){
                //增加可提现盈账户
                user.setWithProfitWallet(user.getWithProfitWallet().add(loseAmt.multiply(withProfitWalletRate)));
                //增加盈利账户
                user.setProfitWallet(user.getProfitWallet().add(loseAmt.multiply(profitWalletRate)));
            }else{
                if (levelUpdate){
                    return 1;
                }
                loseAmt = BigDecimal.ZERO.subtract(loseAmt);

                //按照比例扣除盈利账户和可提现盈利账户
                user.setWithProfitWallet(user.getWithProfitWallet().subtract(loseAmt.multiply(withProfitWalletRate)));
                user.setProfitWallet(user.getProfitWallet().subtract(loseAmt.multiply(profitWalletRate)));
                //如果盈利账户和可提现盈利账户不足，扣除体验金账户
                if (user.getProfitWallet().compareTo(BigDecimal.ZERO)<0){
                    user.setExperienceWallet(user.getExperienceWallet().add(user.getProfitWallet()));
                    user.setProfitWallet(BigDecimal.ZERO);
                }
                if (user.getWithProfitWallet().compareTo(BigDecimal.ZERO)<0){
                    user.setExperienceWallet(user.getExperienceWallet().add(user.getWithProfitWallet()));
                    user.setWithProfitWallet(BigDecimal.ZERO);
                }

            }
        }else if (userWalletLevel==2){
            BigDecimal withProfitWalletRate = BigDecimal.ZERO;
            if (user.getExperienceWalletLevel()==1){
                withProfitWalletRate =new BigDecimal("0.05");
            }else if (user.getExperienceWalletLevel()==2){
                withProfitWalletRate =new BigDecimal("0.03");
            }else if (user.getExperienceWalletLevel()==3){
                withProfitWalletRate =new BigDecimal("0.01");
            }
            BigDecimal profitWalletRate = BigDecimal.ONE.subtract(withProfitWalletRate);
            if(loseAmt.compareTo(BigDecimal.ZERO)>0) {
                //增加可提现盈账户
                user.setWithProfitWallet(user.getWithProfitWallet().add(loseAmt.multiply(withProfitWalletRate)));
                //增加盈利账户
                user.setProfitWallet(user.getProfitWallet().add(loseAmt.multiply(profitWalletRate)));
            }else{
                loseAmt = BigDecimal.ZERO.subtract(loseAmt);
                //按照比例扣除盈利账户和可提现盈利账户
                user.setWithProfitWallet(user.getWithProfitWallet().subtract(loseAmt.multiply(withProfitWalletRate)));
                user.setProfitWallet(user.getProfitWallet().subtract(loseAmt.multiply(profitWalletRate)));
                //如果盈利账户和可提现盈利账户不足，扣除体验金账户
                if (user.getProfitWallet().compareTo(BigDecimal.ZERO)<0){
                    user.setExperienceWallet(user.getExperienceWallet().add(user.getProfitWallet()));
                    user.setProfitWallet(BigDecimal.ZERO);
                }
                if (user.getWithProfitWallet().compareTo(BigDecimal.ZERO)<0){
                    user.setWithProfitWallet(BigDecimal.ZERO);
                }
            }
         }else{
            user.setEnableAmt(user.getEnableAmt().add(bjAmount).add(loseAmt));
            user.setUserAmt(user.getUserAmt().add(bjAmount).add(loseAmt));
        }
        userMapper.updateById(user);

        return 1;
    }

    @Override
    public int updateUserEnableAmtByStockBuy(Integer userId, BigDecimal bjAmount) {
        User user = userMapper.selectById(userId);

        if (user.getUserWalletLevel()==1){
            user.setExperienceWallet(user.getExperienceWallet().subtract(bjAmount));
            if (user.getExperienceWallet().compareTo(BigDecimal.ZERO)<0){
                user.setProfitWallet(user.getProfitWallet().add(user.getExperienceWallet()));
                user.setExperienceWallet(BigDecimal.ZERO);
                if (user.getProfitWallet().compareTo(BigDecimal.ZERO)<0){
                    user.setWithProfitWallet(user.getWithProfitWallet().add(user.getProfitWallet()));
                    user.setProfitWallet(BigDecimal.ZERO);
                }
            }
        }else{
            user.setEnableAmt(user.getEnableAmt().subtract(bjAmount));
            user.setUserAmt(user.getUserAmt().subtract(bjAmount));
        }
      return   userMapper.updateById(user);
    }
}
