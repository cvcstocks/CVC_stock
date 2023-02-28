package com.stock.mx2.service;

import java.math.BigDecimal;

public interface UserLevelWalletService {


    //进行用户等级更新
    int updateUserLevel(Integer userId);

    int updateUserEnableAmtByStockLose(Integer userId, BigDecimal loseAmt, BigDecimal bjAmount);


    int updateUserEnableAmtByStockBuy(Integer userId, BigDecimal bjAmount);

}
