package com.stock.mx2.dao;

import com.stock.mx2.pojo.FundsSecuritiesInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 配資證券信息
 * @author lr
 * @date 2020/07/24
 */
@Mapper
@Repository
public interface FundsSecuritiesInfoMapper {

    /**
     * [新增]
     * @author lr
     * @date 2020/07/24
     **/
    int insert(FundsSecuritiesInfo fundsSecuritiesInfo);

    /**
     * [刪除]
     * @author lr
     * @date 2020/07/24
     **/
    int delete(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/07/24
     **/
    int update(FundsSecuritiesInfo fundsSecuritiesInfo);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/07/24
     **/
    FundsSecuritiesInfo load(int id);

    /**
     * [查詢] 分页查詢
     * @author lr
     * @date 2020/07/24
     **/
    List<FundsSecuritiesInfo> pageList(@Param("pageNum") int pageNum,@Param("pageSize") int pageSize,@Param("keyword") String keyword);

    /**
     * [查詢] 分页查詢 count
     * @author lr
     * @date 2020/07/24
     **/
    int pageListCount(int offset,int pagesize);

    /**
     * [查詢] 查詢可用的证券信息
     * @author lr
     * @date 2020/07/24
     **/
    List<FundsSecuritiesInfo> getEnabledList();

}
