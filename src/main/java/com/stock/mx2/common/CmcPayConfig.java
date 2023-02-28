package com.stock.mx2.common;

import com.stock.mx2.utils.PropertiesUtil;

/**
 * Created by xieyuxing on 2017/9/18.
 */
public class CmcPayConfig {

    //商戶後台密匙
    public static final String KEY="1ee6fa81e2471cacdc14714f704bf5ec11";
    //商戶後台appid
    public static final String UID="LXHKVG11";
    //同步返回地址
    public static final String RETURN_URL= PropertiesUtil.getProperty("website.domain.url") + "/homes/#/rechargelist";
    //異步回調地址
    public static final String NOTIFY_URL= PropertiesUtil.getProperty("website.domain.url") + "/api/pay/juhenewpayNotify.do";
    //接口服務器地址
    public static final String URL="https://cz.lbzjgp.com/";
    // 商戶網頁的編碼方式【請根據實際情況自行配置】
    public static final String CHARSET="utf-8";
    public static final String TOKEN="ABCDEFG";


    /*H5配置*/
    //商戶後台appid
    public static final String H5UID="318543172911";
    //商戶後台密匙
    public static final String H5KEY="0aa9ac8194025b7721648cdf541e4e0b11";
    //public static final String H5URL="https://open.yunmianqian.com/api/pay";
    public static final String H5URL="http://yunpay.waa.cn/";
    //異步回調地址
    public static final String H5NOTIFY_URL="http://www.huijuwang888.com/api/pay/juheh5payNotify.do";

}
