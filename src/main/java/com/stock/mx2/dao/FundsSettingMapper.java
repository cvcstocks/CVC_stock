package com.stock.mx2.dao;
import com.stock.mx2.pojo.FundsSetting;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * funds_setting
 * @author lr
 * @date 2020/07/23
 */
@Mapper
@Repository
public interface FundsSettingMapper {

    /**
     * [新增]
     * @author lr
     * @date 2020/07/23
     **/
    int insert(FundsSetting fundsSetting);

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
    int update(FundsSetting fundsSetting);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/07/23
     **/
    FundsSetting load(int id);

    /**
     * [查詢] 分页查詢
     * @author lr
     * @date 2020/07/23
     **/
    List<FundsSetting> pageList(int offset, int pagesize);

    /**
     * [查詢] 分页查詢 count
     * @author lr
     * @date 2020/07/23
     **/
    int pageListCount(int offset,int pagesize);

    /*查詢所有數據*/
    List findAllFundsSetting();

}
