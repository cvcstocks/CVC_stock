package com.stock.mx2.service;

import com.github.pagehelper.PageInfo;
import com.stock.mx2.common.ServerResponse;
import com.stock.mx2.pojo.FundsLever;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * funds_lever
 * @author lr
 * @date 2020/07/23
 */
public interface IFundsLeverService {

    /**
     * 新增
     */
    int insert(FundsLever fundsLever);

    /**
     * 刪除
     */
    int delete(int id);

    /**
     * 更新
     */
    int update(FundsLever fundsLever);

    /*查詢配資槓桿列表*/
    ServerResponse<PageInfo> getFundsLeverList(int pageNum, int pageSize, HttpServletRequest request);

    /**
     * 配資槓桿列表保存
     */
    ServerResponse saveFundsLever(FundsLever fundsLever);

    /**
     * [查詢] 查詢配资类型杠杆
     * @author lr
     * @date 2020/07/23
     **/
    ServerResponse getFundsTypeList(Integer cycleType);

    /**
     * 配資槓桿-查詢槓桿費率
     */
    ServerResponse getLeverRateInfo(Integer cycleType, Integer lever);

}
