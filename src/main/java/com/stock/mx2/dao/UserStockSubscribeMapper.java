package com.stock.mx2.dao;


import com.stock.mx2.pojo.UserStockSubscribe;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 新股申購
 * @author lr
 * @date 2020/09/11
 */
@Mapper
@Repository
public interface UserStockSubscribeMapper {

    /**
     * [新增]
     * @author lr
     * @date 2020/09/11
     **/
    int insert(UserStockSubscribe userStockSubscribe);

    /**
     * [刪除]
     * @author lr
     * @date 2020/09/11
     **/
    int delete(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/09/11
     **/
    int update(UserStockSubscribe userStockSubscribe);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/09/11
     **/
    UserStockSubscribe load(int id);

    /**
     * [查詢] 分页查詢
     * @author lr
     * @date 2020/09/11
     **/
    List<UserStockSubscribe> pageList(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("keyword") String keyword);

    /**
     * [查詢] 分页查詢 count
     * @author lr
     * @date 2020/09/11
     **/
    int pageListCount(int offset, int pagesize);

    /**
     * [查詢] 查詢用户最新新股申购数据
     * @author lr
     * @date 2020/09/11
     **/
    UserStockSubscribe getOneSubscribeByUserId(Integer userId);

}

