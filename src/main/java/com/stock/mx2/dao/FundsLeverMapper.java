package com.stock.mx2.dao;

import com.stock.mx2.pojo.FundsLever;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * funds_lever
 * @author lr
 * @date 2020/07/23
 */
@Mapper
@Repository
public interface FundsLeverMapper {

    /**
     * [新增]
     * @author lr
     * @date 2020/07/23
     **/
    int insert(FundsLever fundsLever);

    /**
     * [刪除]
     * @author lr
     * @date 2020/07/23
     **/
    int delete(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/07/23
     **/
    int update(FundsLever fundsLever);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/07/23
     **/
    FundsLever load(int id);

    /**
     * [查詢] 分页查詢
     * @author lr
     * @date 2020/07/23
     **/
    List<FundsLever> pageList(int pageNum, int pageSize);

    /**
     * [查詢] 分页查詢 count
     * @author lr
     * @date 2020/07/23
     **/
    int pageListCount(int offset,int pagesize);

    /**
     * [查詢] 查詢配资类型杠杆
     * @author lr
     * @date 2020/07/23
     **/
    List<FundsLever> getFundsTypeList(@Param("cycleType") Integer cycleType);

    /**
     * [查詢] 查詢杠杆费率
     * @author lr
     * @date 2020/07/23
     **/
    FundsLever getLeverRateInfo(@Param("cycleType") Integer cycleType,@Param("lever") Integer lever);

}
