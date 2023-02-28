package com.stock.mx2.dao;

import com.stock.mx2.pojo.FundsAppend;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 配資追加申請
 * @author lr
 * @date 2020/08/01
 */
@Mapper
@Repository
public interface FundsAppendMapper {

    /**
     * [新增]
     * @author lr
     * @date 2020/08/01
     **/
    int insert(FundsAppend fundsAppend);

    /**
     * [刪除]
     * @author lr
     * @date 2020/08/01
     **/
    int delete(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/08/01
     **/
    int update(FundsAppend fundsAppend);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/08/01
     **/
    FundsAppend load(int id);


    /**
     * [查詢] 分页查詢
     * @author lr
     * @date 2020/07/24
     **/
    List<FundsAppend> pageList(@Param("pageNum") int pageNum,@Param("pageSize") int pageSize,@Param("keyword") String keyword,@Param("status") Integer status,@Param("userId") Integer userId,@Param("appendType") Integer appendType);


    /**
     * [查詢] 分页查詢 count
     * @author lr
     * @date 2020/08/01
     **/
    int pageListCount(int offset,int pagesize);

    /**
     * [查詢] 根据子账户查詢是否申请终止
     * @author lr
     * @date 2020/08/01
     **/
    int isAppendEnd(@Param("applyId") Integer applyId);

}
