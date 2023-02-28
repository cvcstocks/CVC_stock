package com.stock.mx2.dao;

import com.stock.mx2.pojo.UserFundsPosition;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * user_funds_position
 * @author lr
 * @date 2020/07/27
 */
@Mapper
@Repository
public interface UserFundsPositionMapper {

    /**
     * [新增]
     * @author lr
     * @date 2020/07/27
     **/
    int insert(UserFundsPosition userFundsPosition);

    /**
     * [刪除]
     * @author lr
     * @date 2020/07/27
     **/
    int delete(int id);

    /**
     * [更新]
     * @author lr
     * @date 2020/07/27
     **/
    int update(UserFundsPosition userFundsPosition);

    /**
     * [查詢] 根據主鍵 id 查詢
     * @author lr
     * @date 2020/07/27
     **/
    UserFundsPosition load(int id);

    /**
     * [查詢] 分页查詢
     * @author lr
     * @date 2020/07/24
     **/
    List<UserFundsPosition> pageList(@Param("pageNum") int pageNum,@Param("pageSize") int pageSize,@Param("keyword") String keyword);


    /**
     * [查詢] 分页查詢 count
     * @author lr
     * @date 2020/07/27
     **/
    int pageListCount(int offset,int pagesize);

    /*
     * 分倉交易-查詢所有平倉/持倉信息
     * */
    List findMyPositionByCodeAndSpell(@Param("uid") Integer paramInteger1, @Param("stockCode") String paramString1, @Param("stockSpell") String paramString2, @Param("state") Integer paramInteger2);

    /*
    * 根據單號查詢配資信息
    * */
    UserFundsPosition findPositionBySn(String paramString);

    UserFundsPosition findUserFundsPositionByCode(@Param("userId") Integer paramInteger, @Param("fundsCode") String fundsCode);

}
