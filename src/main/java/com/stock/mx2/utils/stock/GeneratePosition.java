package com.stock.mx2.utils.stock;


import com.stock.mx2.utils.DateTimeUtil;

import java.util.Date;
import java.util.Random;


public class GeneratePosition {
    public static String getPositionId() {
        Random random = new Random();
        Integer number = Integer.valueOf(random.nextInt(90000) + 10000);


        String datestr = DateTimeUtil.dateToStr(new Date(), "yyyyMMdd");
        return datestr + number;
    }
}
