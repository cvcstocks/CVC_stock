package com.stock.mx2.service;

import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.FundsSetting;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * funds_setting
 * @author lr
 * @date 2020/07/23
 */
public interface IFundsSettingService {

    /**
     * 新增
     */
    int insert(FundsSetting fundsSetting);

    /**
     * 更新
     */
    int update(FundsSetting fundsSetting);

    /**
     * 根據主鍵 id 查詢
     */
    FundsSetting load(int id);

    /**
     * 保存設置
     */
    ServerResponse save(FundsSetting fundsSetting, HttpServletRequest paramHttpServletRequest);

    /*查詢第一條數據*/
    FundsSetting getFundsSetting();

}
